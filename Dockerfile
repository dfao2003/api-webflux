FROM eclipse-temurin:17-jdk-jammy

# Instala dependencias del sistema necesarias para Netty/gRPC nativo
RUN apt-get update && apt-get install -y \
    libssl-dev \
    libapr1 \
    libtcnative-1 \
    && rm -rf /var/lib/apt/lists/*

# Copia y configura tu JAR
WORKDIR /app
COPY target/*.jar app.jar

# Copia el JSON de firebase si es necesario
COPY keyFirebase.json /app/keyFirebase.json
ENV GOOGLE_APPLICATION_CREDENTIALS=/app/keyFirebase.json

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
