FROM maven:3.9.11-eclipse-temurin-21 AS build

WORKDIR /workspace

COPY backend/pom.xml backend/pom.xml
COPY backend/src backend/src
COPY database/migrations database/migrations

RUN mvn -f backend/pom.xml clean package -DskipTests

FROM eclipse-temurin:21-jre

RUN groupadd --system app && useradd --system --gid app app

WORKDIR /app
RUN mkdir -p /app/uploads && chown app:app /app/uploads

COPY --from=build /workspace/backend/target/dsuplementos-api-0.1.0.jar app.jar

USER app

EXPOSE 10000

ENTRYPOINT ["sh", "-c", "exec java -Dserver.port=${PORT:-10000} -jar app.jar"]
