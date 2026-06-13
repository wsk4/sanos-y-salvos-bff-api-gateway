# ==========================================
# Fase 1: Build (Compilación y Caché)
# ==========================================
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app

# Descargar dependencias en una capa aislada para aprovechar la caché de Docker
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Compilar el artefacto sin ejecutar tests unitarios en el proceso de empaquetado
COPY src ./src
RUN mvn package -DskipTests

# ==========================================
# Fase 2: Runtime (Entorno seguro de ejecución)
# ==========================================
FROM eclipse-temurin:21-jre-alpine

# Hardening de Seguridad: Crear usuario de sistema no-privilegiado
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

WORKDIR /app

# Copiar el JAR compilado asignando la propiedad al usuario seguro
COPY --from=build --chown=spring:spring /app/target/*.jar app.jar

# Parámetros JVM óptimos para Gateways (G1GC gestiona de forma eficiente el tráfico web pesado)
ENV JAVA_OPTS="-XX:+UseG1GC -XX:+UseContainerSupport -XX:MaxRAMPercentage=80.0"

# Ejecución a través de una Shell intermedia para interpretar correctamente las variables de entorno
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]