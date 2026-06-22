FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Copiar el jar pre-empaquetado desde la carpeta target local
COPY target/backend-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

# Iniciar la aplicación
ENTRYPOINT ["java", "-jar", "app.jar"]
