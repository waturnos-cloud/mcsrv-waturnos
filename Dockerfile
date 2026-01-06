
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/target/waturnos-api-1.0.0.jar app.jar
ENV JAVA_OPTS="-Xmx352m -Xms128m -XX:ReservedCodeCacheSize=64M -XX:+UseSerialGC -Djava.security.egd=file:/dev/./urandom"
EXPOSE 8085
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
