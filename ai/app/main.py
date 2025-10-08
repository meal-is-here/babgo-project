import os
import threading
import time
import joblib
import pandas as pd
import psycopg2
import redis
from fastapi import FastAPI, HTTPException
from pydantic import BaseModel
from typing import List
import subprocess
import json
from dotenv import load_dotenv

load_dotenv()

# ------------------------------
# 경로 설정
# ------------------------------

BASE_DIR = os.path.dirname(os.path.abspath(__file__))

MODEL_PATH = os.path.abspath(os.path.join(BASE_DIR, os.getenv("MODEL_PATH")))
MATRIX_PATH = os.path.abspath(os.path.join(BASE_DIR, os.getenv("MATRIX_PATH")))
TRAIN_SCRIPT_PATH = os.path.abspath(os.path.join(BASE_DIR, os.getenv("TRAIN_SCRIPT_PATH")))

DB_NAME = os.getenv("DB_NAME")
DB_USER = os.getenv("DB_USER")
DB_PASSWORD = os.getenv("DB_PASSWORD")
DB_HOST = os.getenv("DB_HOST")
DB_PORT = int(os.getenv("DB_PORT"))

# ------------------------------
# 전역 변수
# ------------------------------
model, user_store_matrix = None, None

# ------------------------------
# Redis 설정
# ------------------------------
REDIS_HOST = os.getenv("REDIS_HOST")
REDIS_PORT = int(os.getenv("REDIS_PORT"))
REDIS_DB = int(os.getenv("REDIS_DB"))
POPULAR_STORE_KEY = os.getenv("POPULAR_STORE_KEY")
POPULAR_STORE_TTL = int(os.getenv("POPULAR_STORE_TTL"))  # 10분

redis_client = redis.Redis(host=REDIS_HOST, port=REDIS_PORT, db=REDIS_DB, decode_responses=True)

# ------------------------------
# 모델 로드
# ------------------------------
def load_model():
    global model, user_store_matrix
    try:
        model = joblib.load(MODEL_PATH)
        user_store_matrix = joblib.load(MATRIX_PATH)
        print("사전 학습된 모델 로딩 성공!")
    except FileNotFoundError:
        print("모델 파일이 없습니다. train.py를 먼저 실행하세요.")
        model, user_store_matrix = None, None

# ------------------------------
# 주기적 모델 학습
# ------------------------------
def periodic_train(interval_seconds: int = 3600):
    while True:
        print("주기적 학습 시작...")
        try:
            subprocess.run(["python3", TRAIN_SCRIPT_PATH], check=True)
            load_model()
            update_popular_store_cache(limit=5)
            print("모델 갱신 완료!")
        except Exception as e:
            print(f"학습 중 오류 발생: {e}")
        time.sleep(interval_seconds)

# ------------------------------
# 인기 가게 캐시
# ------------------------------
def update_popular_store_cache(limit=5):
    try:
        conn = psycopg2.connect(
            dbname=DB_NAME,
            user=DB_USER,
            password=DB_PASSWORD,
            host=DB_HOST,
            port=DB_PORT
        )
        popular_stores_df = pd.read_sql(f"""
            SELECT store_id, COUNT(*) as order_count
            FROM p_orders
            WHERE order_status IN ('CONFIRMED', 'PAYMENT')
            GROUP BY store_id
            ORDER BY order_count DESC
            LIMIT {limit}
        """, conn)
        conn.close()
        store_list = popular_stores_df['store_id'].tolist()
        redis_client.set(POPULAR_STORE_KEY, json.dumps(store_list), ex=POPULAR_STORE_TTL)
        print(f"Redis 인기 가게 캐시 갱신 완료: {store_list}")
    except Exception as e:
        print(f"인기 가게 캐시 갱신 실패: {e}")

def get_popular_stores(limit=5) -> List[str]:
    cached = redis_client.get(POPULAR_STORE_KEY)
    if cached:
        store_list = json.loads(cached)
        return store_list[:limit]
    # 캐시가 없으면 즉시 갱신 후 반환
    update_popular_store_cache(limit)
    cached = redis_client.get(POPULAR_STORE_KEY)
    if cached:
        return json.loads(cached)[:limit]
    return []

# ------------------------------
# FastAPI 앱 생성
# ------------------------------
app = FastAPI()

# 서버 시작 시 모델 로드 + 인기 가게 캐시 초기화
load_model()
update_popular_store_cache(limit=5)
threading.Thread(target=periodic_train, args=(3600,), daemon=True).start()

# ------------------------------
# 요청/응답 모델
# ------------------------------
class RecommendRequest(BaseModel):
    userId: str

class RecommendResponse(BaseModel):
    storeIds: List[str]

# ------------------------------
# 추천 함수
# ------------------------------
def get_ml_recommendations(user_id: str) -> List[str]:
    if model is None or user_store_matrix is None:
        raise HTTPException(status_code=500, detail="Model not loaded")

    # 사용자 데이터가 없으면 인기 가게 fallback
    if user_id not in user_store_matrix.index:
        return get_popular_stores(limit=5)

    user_ordered_stores = user_store_matrix.loc[user_id]
    ordered_store_ids = user_ordered_stores[user_ordered_stores == 1].index.tolist()

    recommendations_with_distance = []

    for store_id in ordered_store_ids:
        distances, indices = model.kneighbors(user_store_matrix.T.loc[[store_id]], n_neighbors=5)
        for dist, idx in zip(distances.flatten(), indices.flatten()):
            similar_store = user_store_matrix.T.index[idx]
            if similar_store not in ordered_store_ids:
                recommendations_with_distance.append((similar_store, dist))

    # 거리 기준 정렬
    recommendations_with_distance.sort(key=lambda x: x[1])

    # 중복 제거 및 최대 5개
    seen = set()
    final_recommendations = []
    for store, _ in recommendations_with_distance:
        if store not in seen:
            final_recommendations.append(store)
            seen.add(store)
        if len(final_recommendations) >= 5:
            break

    # KNN 추천이 없으면 인기 가게 fallback
    if not final_recommendations:
        return get_popular_stores(limit=5)

    return final_recommendations

# ------------------------------
# FastAPI 엔드포인트
# ------------------------------
@app.post("/recommendations", response_model=RecommendResponse)
def recommend_stores(request: RecommendRequest):
    recommended_ids = get_ml_recommendations(request.userId)
    return RecommendResponse(storeIds=recommended_ids)
