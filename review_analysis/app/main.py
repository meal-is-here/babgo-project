import logging
from fastapi import FastAPI, HTTPException
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel

from app.model import compute_fake_score

logger = logging.getLogger("uvicorn.error")
app = FastAPI(
    title="FakeReviewML",
    description="Simple fake-review scoring service (stub -> replaceable with ML model",
    version="0.1.0",
)

# 필요하면 origin을 한정하세요. 개발 중에는 * 허용해도 무방.
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["POST", "OPTIONS"],
    allow_headers=["*"],
)

class PredictRequest(BaseModel):
    content: str | None = ""

class PredictResponse(BaseModel):
    fake_score: float

@app.post("/predict", response_model=PredictResponse)
async def predict(req: PredictRequest):
    """
    Request:
        POST /predict
        {"content": "리뷰 텍스트"}

    Response:
        {"fake_score: 0.42}
    """
    try:
        content = req.content or ""
        # 모델(또는 규칙)로 점수 계산
        score = compute_fake_score(content)
        # 안정성: ensure float in [0.0, 1.0]
        score = float(score)
        if score < 0.0 :
            score = 0.0
        if score > 1.0:
            score = 1.0
        logger.debug("Predicted fake_score=%s for content=%s", score, (content[:100] + "...") if len(content) > 100 else content)
        return PredictResponse(fake_score=round(score, 4))
    except Exception as e:
        logger.exception("Error in /predict")
        raise HTTPException(status_code=500, detail="Internal error while computing fake score")

# Health check
@app.get("/health")
async def health():
    return {"status": "ok"}

if __name__ == "__main__":
    import uvicorn
    uvicorn.run(
        "main:app",   # 파일명이 main.py라면
        host="0.0.0.0",
        port=8001,
        reload=True   # 개발 중 코드 변경시 자동 재시작
    )
