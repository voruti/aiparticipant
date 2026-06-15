# syntax=docker/dockerfile:1@sha256:2780b5c3bab67f1f76c781860de469442999ed1a0d7992a5efdf2cffc0e3d769

FROM eclipse-temurin:25-jdk-noble@sha256:29d2d8af5d12f9ee7aec18f4fb2cd8bc8e6501b748ac62631acd31c867cfa262 AS build
WORKDIR /app
COPY --parents .mvn/ mvnw pom.xml ./
RUN ./mvnw dependency:go-offline -B
COPY src ./src
RUN ./mvnw package -DskipTests

FROM eclipse-temurin:25-jre-noble@sha256:b27ca47660a8fa837e47a8533b9b1a3a430295cf29ca28d91af4fd121572dc29
RUN addgroup --system spring && adduser --system spring --group
USER spring:spring
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "/app/app.jar", "--spring.profiles.active=prod"]
