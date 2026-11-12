# Build: mvn clean package -DskipTests   then   docker build -t reclamation-service .
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY target/reclamation-service-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8086
ENTRYPOINT ["java", "-jar", "app.jar"]
