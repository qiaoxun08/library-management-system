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

# 非敏感默认值；敏感信息（DB_PASSWORD/JWT_SECRET/REDIS_PASSWORD）
# 必须由 docker-compose 的 environment 注入，镜像内不保留任何凭据
ENV DB_HOST=mysql
ENV DB_PORT=3306
ENV DB_NAME=library_system
ENV DB_USERNAME=root
ENV REDIS_HOST=redis
ENV REDIS_PORT=6379

# 以非 root 用户运行，降低容器被攻破后的影响面
RUN groupadd -r appuser && useradd -r -g appuser appuser
USER appuser

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=10s --retries=3 \
  CMD curl -f http://localhost:8080/api/auth/captcha || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]
