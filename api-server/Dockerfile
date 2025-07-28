# 1단계: 빌드 환경 (Maven과 Java 21)
FROM maven:3.9.6-eclipse-temurin-21 AS builder

WORKDIR /app
# pom.xml을 먼저 복사하여 의존성 레이어를 캐싱
COPY pom.xml .
RUN mvn dependency:go-offline

COPY src ./src
# 애플리케이션 빌드 (jar 파일 생성)
RUN mvn package -DskipTests

# 2단계: 실행 환경 (Java 21 런타임)
FROM eclipse-temurin:21-jre
WORKDIR /app
# 빌드 단계에서 생성된 jar 파일을 실행 환경으로 복사
COPY --from=builder /app/target/*.jar app.jar

# 컨테이너 8080 포트 개방
EXPOSE 8080
# 애플리케이션 실행
ENTRYPOINT ["java", "-jar", "app.jar"]