FROM maven:3.9-eclipse-temurin-17-alpine AS build
WORKDIR /build
COPY pom.xml .
COPY shared ./shared
COPY services ./services
COPY gateway ./gateway
RUN mvn -pl services/auth-service -am package -DskipTests -q

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /build/services/auth-service/target/*.jar app.jar
ENV JAVA_OPTS="-Xmx384m -XX:+UseContainerSupport"
EXPOSE 8080
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
