# 多阶段构建：Maven 构建 + JRE 运行
FROM maven:3.9-eclipse-temurin-17 AS builder
WORKDIR /build
COPY pom.xml .
# 先下载依赖（利用 Docker 缓存层）
RUN mvn dependency:go-offline -q
COPY src ./src
RUN mvn package -DskipTests -q

FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
COPY --from=builder /build/target/*.jar app.jar

# 环境变量（可在 docker-compose 中覆盖）
ENV DB_HOST=mysql
ENV DB_PORT=3306
ENV DB_NAME=library_system
ENV DB_USERNAME=root
ENV DB_PASSWORD=root
ENV REDIS_HOST=redis
ENV REDIS_PORT=6379
ENV JWT_SECRET=production-secret-key-change-me-32bytes!

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=10s --retries=3 \
  CMD curl -f http://localhost:8080/api/auth/captcha || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]
