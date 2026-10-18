FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /build
COPY . .
RUN mvn -B -DskipTests package

FROM eclipse-temurin:21-jre

WORKDIR /app
RUN useradd --system --create-home appuser
COPY --from=build /build/target/*.jar /app/app.jar

USER appuser
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]