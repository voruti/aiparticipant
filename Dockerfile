# syntax=docker/dockerfile:1@sha256:ecfaec9ed6d810b56388c508f4121597bfbba70d41a6dfeee4d8cad5f295fc32

FROM eclipse-temurin:25-jdk-noble@sha256:ba0f0e1a33864105e1d454e88105c617ab40f71062db1f306bf58be32a3f8e5e AS build
WORKDIR /app
COPY --parents .mvn/ mvnw pom.xml ./
RUN ./mvnw dependency:go-offline -B
COPY src ./src
RUN ./mvnw package -DskipTests

FROM eclipse-temurin:25-jre-noble@sha256:fbcf915c585659b30eb766ada4d6d7cfc9ec1040bf521e95bf61b10a25af73db
RUN addgroup --system spring && adduser --system spring --group
USER spring:spring
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "/app/app.jar", "--spring.profiles.active=prod"]
