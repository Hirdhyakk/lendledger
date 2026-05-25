# Local / generic build. Render uses per-service files in deploy/docker/*.Dockerfile
FROM maven:3.9-eclipse-temurin-17-alpine AS build
WORKDIR /build
COPY pom.xml .
COPY shared ./shared
COPY services ./services
COPY gateway ./gateway
ARG MODULE=services/auth-service
RUN mvn -pl ${MODULE} -am package -DskipTests -q

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
ARG MODULE
COPY --from=build /build/${MODULE}/target/*.jar app.jar
ENV JAVA_OPTS="-Xmx384m -XX:+UseContainerSupport"
EXPOSE 8080
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
