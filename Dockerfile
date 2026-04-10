FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app
# Sistem dilini UTF-8 yapıyoruz ki karakter hatası vermesin
ENV LANG=C.UTF-8
COPY . .
RUN mvn clean package -DskipTests

FROM eclipse-temurin:21-jdk-jammy
WORKDIR /app
COPY --from=build /app/target/backend-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]