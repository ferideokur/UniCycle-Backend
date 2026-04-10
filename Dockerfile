# 1. Aşama: Build
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app

# Karakter hatalarını engellemek için sistem dili
ENV LANG=C.UTF-8

COPY . .

# Testleri hem derlemeyi hem çalıştırmayı atlıyoruz (En garantisi budur)
RUN mvn clean package -Dmaven.test.skip=true

# 2. Aşama: Run
FROM eclipse-temurin:21-jdk-jammy
WORKDIR /app
COPY --from=build /app/target/backend-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]