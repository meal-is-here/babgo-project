# """
# 규칙 기반(fake) 리뷰 판별 모델.
# 나중에 진짜 ML 모델 (KoBERT, GPT 등)로 교체해도 이 함수 인터페이스는 그대로 유지하면 됨.
# """

from transformers import BertTokenizer, BertForSequenceClassification
import torch

# 1) 모델 로드 (FastAPI 서버 시작 시)
tokenizer = BertTokenizer.from_pretrained("monologg/kobert")
model = BertForSequenceClassification.from_pretrained("monologg/kobert")
model.eval()  # 추론 모드

def compute_fake_score(content: str) -> float:
    if not content or not content.strip():
        return 0.5

    inputs = tokenizer(content, return_tensors="pt", truncation=True, padding=True)
    with torch.no_grad():
        outputs = model(**inputs)
        logits = outputs.logits
        prob = torch.softmax(logits, dim=1)  # 클래스 확률
        fake_prob = prob[0][1].item()  # 예: 1번 클래스가 'fake'
    return min(1.0, max(0.0, fake_prob))
#
# import re
#
# def compute_fake_score(content: str) -> float:
#     """
#     간단한 규칙 기반 fake 리뷰 판별 함수.
#     반환값은 0.0 ~ 1.0 범위의 float.
#     """
#
#     if not content or not content.strip():
#         return 0.5 # 내용이 없으면 중간 점수
#
#     content = content.strip()
#     lower = content.lower()
#
#     score = 0.0
#
#     # 1) 너무 짧은 리뷰 -> 의심
#     if len(content) < 5 :
#         score += 0.5
#     elif len(content) < 15:
#         score += 0.3
#
#     # 2) 동일한 단어 반복 -> 자동 생성 가능성
#     words = re.findall(r"\b\w+\b", lower)
#     if words:
#         unique_ratio = len(set(words)) / len(words)
#         if unique_ratio < 0.5 :
#             score += 0.4
#
#     # 3) 과도하게 긍정적인 표현 -> 인위적일 수 있음
#     if any(word in lower for word in ["최고", "강추", "인생맛집", "완벽", "진짜맛있", "너무좋", "대박"]):
#         score += 0.3
#
#     # 4) 부정적 단어 + 높은 평점 패턴 감지 (스프링 측에서 rating 제공 시 확장 가능)
#     if "별로" in lower or "실망" in lower:
#         score += 0.2
#
#     # 5) URL, 해시태그, 이모지 과다 사용
#     if "http" in lower or "#" in lower :
#         score += 0.2
#     if re.findall(r"[😀-🙏]", content):
#         score += 0.1
#
#     # normalize
#     return min(1.0, round(score, 3))