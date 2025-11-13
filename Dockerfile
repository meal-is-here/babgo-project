# 1️⃣ Gradle 빌드용 이미지
FROM gradle:8.7-jdk17 AS build

# 2️⃣ 소스 코드 복사
WORKDIR /app
COPY . .

# 3️⃣ Spring Boot JAR 빌드
RUN gradle clean bootJar --no-daemon

# 4️⃣ 실제 실행용 이미지 (더 가벼움)
FROM openjdk:17-jdk-slim

# 5️⃣ 작업 디렉토리
WORKDIR /app

# 6️⃣ 빌드 결과물 복사
COPY --from=build /app/build/libs/*.jar app.jar

# 7️⃣ 포트 설정 (Spring Boot 기본 8080)
EXPOSE 8080

# 8️⃣ 실행 명령
ENTRYPOINT ["java", "-jar", "app.jar"]
