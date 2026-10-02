# syntax=docker/dockerfile:1@sha256:ecfaec9ed6d810b56388c508f4121597bfbba70d41a6dfeee4d8cad5f295fc32

FROM eclipse-temurin:25-jdk-noble@sha256:735baf2edc6cd6485240144a84fa4db142b9a6f47b4eb4080f31058d200f9813 AS build
WORKDIR /app
COPY --parents .mvn/ mvnw pom.xml ./
RUN ./mvnw dependency:go-offline -B
COPY src ./src
RUN ./mvnw package -DskipTests

FROM eclipse-temurin:25-jre-noble@sha256:398f810215757dc1926390014272579fb0e57c41ef1c8aa4f64ae761613a168b
RUN addgroup --system spring && adduser --system spring --group
USER spring:spring
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "/app/app.jar", "--spring.profiles.active=prod"]
