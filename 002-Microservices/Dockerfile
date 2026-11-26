# One image per service: docker build --build-arg SERVICE=currency-exchange-service .
FROM eclipse-temurin:25-jdk AS build
ARG SERVICE
WORKDIR /src
COPY . .
RUN ./gradlew --no-daemon -q :${SERVICE}:bootJar && cp ${SERVICE}/build/libs/${SERVICE}-*-SNAPSHOT.jar /app.jar

FROM eclipse-temurin:25-jre
COPY --from=build /app.jar /app.jar
ENTRYPOINT ["java", "-jar", "/app.jar"]
