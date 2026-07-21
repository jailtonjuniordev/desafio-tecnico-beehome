FROM eclipse-temurin:21-jdk AS build
WORKDIR /app

COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
RUN chmod +x mvnw

COPY src src
RUN ./mvnw -B -DskipTests package

FROM eclipse-temurin:21-jre
WORKDIR /app

COPY --from=build /app/target/beehome-tecnico-*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
