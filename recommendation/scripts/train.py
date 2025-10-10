import pandas as pd
from sklearn.neighbors import NearestNeighbors
import os
import joblib
import psycopg2
from dotenv import load_dotenv

# ------------------------------
# .env 로드
# ------------------------------
load_dotenv()

DB_NAME = os.getenv("DB_NAME")
DB_USER = os.getenv("DB_USER")
DB_PASSWORD = os.getenv("DB_PASSWORD")
DB_HOST = os.getenv("DB_HOST")
DB_PORT = int(os.getenv("DB_PORT"))

print("모델 학습을 시작합니다...")

conn = psycopg2.connect(
    dbname=DB_NAME,
    user=DB_USER,
    password=DB_PASSWORD,
    host=DB_HOST,
    port=DB_PORT
)

orders_df = pd.read_sql("""
    SELECT user_id, store_id
    FROM p_orders
    -- WHERE order_status IN ('CONFIRMED', 'PAYMENT')
""", conn)

conn.close()

# orders_data = [
#     ("111e8400-e29b-41d4-a716-446655440000", "aaae8400-e29b-41d4-a716-446655440000"), ("222e8400-e29b-41d4-a716-446655440000", "bbbe8400-e29b-41d4-a716-446655440000"), ("333e8400-e29b-41d4-a716-446655440000", "ccce8400-e29b-41d4-a716-446655440000"),
#     ("444e8400-e29b-41d4-a716-446655440000", "ddde8400-e29b-41d4-a716-446655440000"), ("555e8400-e29b-41d4-a716-446655440000", "eeee8400-e29b-41d4-a716-446655440000"), ("666e8400-e29b-41d4-a716-446655440000", "fffe8400-e29b-41d4-a716-446655440000"),
#     ("777e8400-e29b-41d4-a716-446655440000", "fffe8400-e29b-41d4-a716-446655440000"), ("888e8400-e29b-41d4-a716-446655440000", "ggge8400-e29b-41d4-a716-446655440000"), ("999e8400-e29b-41d4-a716-446655440000", "hhhe8400-e29b-41d4-a716-446655440000"),
# ]
# orders_df = pd.DataFrame(orders_data, columns=["user_id", "store_id"])

if orders_df.empty:
    print("주문 데이터가 없습니다. 학습을 건너뜁니다.")
    exit()

user_store_matrix = pd.crosstab(orders_df['user_id'], orders_df['store_id'])
model = NearestNeighbors(metric='cosine', algorithm='brute')
model.fit(user_store_matrix.T)
print("모델 학습 완료!")

model_dir = '../models'
if not os.path.exists(model_dir):
    os.makedirs(model_dir)

joblib.dump(model, os.path.join(model_dir, 'recommend_model.pkl'))
joblib.dump(user_store_matrix, os.path.join(model_dir, 'user_store_matrix.pkl'))
print(f"모델 파일이 '{model_dir}' 폴더에 저장되었습니다.")