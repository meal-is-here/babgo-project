-- [1] 전달받은 Redis 키 정의
-- Redis는 Lua 스크립트를 실행할 때 실제 Redis 키들을 KEYS 배열로 전달합니다.
-- KEYS[1]: ZSET (정렬 기준)
-- KEYS[2]: HASH (가게 데이터 JSON 저장소)
-- KEYS[3]: LIST (정렬된 결과를 저장할 리스트)
-- KEYS[4]: GEO  → 거리 기반 좌표 저장소 (거리 정렬 시에만 사용)
local zsetKey = KEYS[1]
local hashKey = KEYS[2]
local listKey = KEYS[3]
local geoKey  = KEYS[4]

-- =========================================================
-- [2] 전달받은 인자(ARGV) 정의
-- =========================================================
local storeId   = ARGV[1]             -- 가게 ID
local sort      = ARGV[2]             -- 정렬 기준 (LIKES, ORDER_COUNT, RATING, CREATED, DISTANCE)
local delta     = tonumber(ARGV[3])   -- 변경 값 (+1, -1, 4.5 등)
local action    = ARGV[4]             -- 세부 동작 (CREATE, CANCEL, UPDATE)
local oldRating = tonumber(ARGV[5])   -- 평점 수정/삭제 시 기존 평점
local longitude = tonumber(ARGV[6])   -- 거리 계산용 경도
local latitude  = tonumber(ARGV[7])   -- 거리 계산용 위도
local radius    = tonumber(ARGV[8])   -- 거리 반경 (m 또는 km)

-- =========================================================
-- [3] ZSET 점수 갱신
-- =========================================================
if sort == "LIKES" or sort == "ORDER_COUNT" then
  redis.call("ZINCRBY", zsetKey, delta, storeId)
elseif sort == "RATING" or sort == "CREATED" then
  redis.call("ZADD", zsetKey, delta, storeId)
end

-- =========================================================
-- [4] HASH(JSON) 데이터 갱신
-- =========================================================
local json = redis.call("HGET", hashKey, storeId)
if json then
  local data = cjson.decode(json)

  if sort == "LIKES" then
    -- 좋아요 등록/취소
    if action == "CREATE" then
      data.likes = (data.likes or 0) + 1
    elseif action == "CANCEL" then
      data.likes = math.max(0, (data.likes or 0) - 1)
    end

  elseif sort == "ORDER_COUNT" then
    data.orderCount = (data.orderCount or 0) + delta

  elseif sort == "RATING" then
    -- 평점 계산 로직
    data.reviewCount = data.reviewCount or 0
    data.avgRating = data.avgRating or 0.0

    if action == "CREATE" then
      data.avgRating = ((data.avgRating * data.reviewCount) + delta) / (data.reviewCount + 1)
      data.reviewCount = data.reviewCount + 1

    elseif action == "UPDATE" then
      data.avgRating = ((data.avgRating * data.reviewCount) - oldRating + delta) / data.reviewCount

    elseif action == "CANCEL" then
      if data.reviewCount > 1 then
        data.avgRating = ((data.avgRating * data.reviewCount) - oldRating) / (data.reviewCount - 1)
      else
        data.avgRating = 0.0
      end
      data.reviewCount = math.max(0, data.reviewCount - 1)
    end

  -- 반올림
  data.avgRating = math.floor(data.avgRating * 10 + 0.5) / 10


    -- 최신 평점을 ZSET에도 반영
    redis.call("ZADD", zsetKey, data.avgRating, storeId)
  end

  redis.call("HSET", hashKey, storeId, cjson.encode(data))
end

-- =========================================================
-- [5] 거리(DISTANCE) 정렬 처리
-- =========================================================
if sort == "DISTANCE" then
  redis.call("DEL", listKey)

  -- GEOSEARCH: 반경 내 가까운 순으로 가져오기
  local geoResults = redis.call("GEOSEARCH", geoKey,
      "FROMLONLAT", longitude, latitude,
      "BYRADIUS", radius, "m",
      "WITHDIST", "ASC"
  )

  -- 거리 기준으로 정렬된 결과를 LIST에 저장
  for i = 1, #geoResults, 2 do
    local sId = geoResults[i]
    local sJson = redis.call("HGET", hashKey, sId)
    if sJson then
      redis.call("RPUSH", listKey, sJson)
    end
  end

else
  -- =========================================================
  -- [6] 일반 정렬 (좋아요, 주문, 평점, 등록순)
  -- =========================================================
  redis.call("DEL", listKey)
  local rankedStores = redis.call("ZREVRANGE", zsetKey, 0, -1, "WITHSCORES")

  for i = 1, #rankedStores, 2 do
    local sId = rankedStores[i]
    local sJson = redis.call("HGET", hashKey, sId)
    if sJson then
      redis.call("RPUSH", listKey, sJson)
    end
  end
end

-- =========================================================
-- [7] 완료 메시지 반환
-- =========================================================
return "cache updated"