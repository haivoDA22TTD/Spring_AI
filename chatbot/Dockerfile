# ================== BUILD STAGE ==================
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /app

# Copy Maven wrapper + pom.xml trước để cache dependencies (rất quan trọng)
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

# Download dependencies trước (cache layer riêng)
RUN ./mvnw dependency:go-offline -B

# Copy source code
COPY src ./src

# Build ra file JAR (skip test cho nhanh)
RUN ./mvnw clean package -DskipTests -B

# ================== RUNTIME STAGE ==================
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Copy file JAR từ stage builder
COPY --from=builder /app/target/*.jar app.jar

# Tạo user non-root (Render khuyến khích + bảo mật hơn)
RUN addgroup --system spring && adduser --system --ingroup spring spring
USER spring

# Mở port (Render sẽ tự detect, nhưng khai báo cho rõ)
EXPOSE 8080

# Biến môi trường Spring Boot chuẩn
ENV JAVA_OPTS="-Xms512m -Xmx1024m" \
    SPRING_PROFILES_ACTIVE=prod

# Chạy ứng dụng
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -jar /app/app.jar"]
