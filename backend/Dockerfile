# ===================================================================
# Dockerfile para Spring Boot - Optimizado para Render.com
# ===================================================================
# Imagen base: Java 17 (Eclipse Temurin LTS)
# Usamos 'jammy' (Ubuntu 22.04) para compatibilidad garantizada
FROM eclipse-temurin:17-jdk-jammy AS builder

# Establecer directorio de trabajo
WORKDIR /build

# ===================================================================
# COPIA DE ARCHIVOS NECESARIOS
# ===================================================================
# Copiamos primero el pom.xml para aprovechar caché de Docker
COPY pom.xml .

# Copiamos el código fuente
COPY src ./src

# ===================================================================
# COMPILACIÓN DEL PROYECTO
# ===================================================================
# Instalamos Maven y ejecutamos la compilación
# Si existe el wrapper (mvnw), lo usamos; si no, usamos Maven directo
RUN if [ -f "./mvnw" ]; then \
        chmod +x ./mvnw && ./mvnw clean package -DskipTests; \
    else \
        apt-get update && apt-get install -y maven && mvn clean package -DskipTests; \
    fi

# ===================================================================
# IMAGEN FINAL - RUNTIME
# ===================================================================
# Imagen base más ligera solo para ejecutar (JDK no necesario, solo JRE)
FROM eclipse-temurin:17-jre-jammy

# Establecer directorio de trabajo
WORKDIR /app

# ===================================================================
# DETECCIÓN AUTOMÁTICA DEL JAR GENERADO
# ===================================================================
# Busca cualquier archivo .jar en target/ y lo copia como app.jar
# Esto funciona para cualquier proyecto Spring Boot sin edición manual
COPY --from=builder /build/target/*.jar app.jar

# ===================================================================
# CONFIGURACIÓN DE PUERTO DINÁMICO (Render.com)
# ===================================================================
# Render proporciona el puerto en la variable PORT
# Si no existe, usamos 8080 como默认值 (default)
ENV PORT=8080

# Expone el puerto configurado
EXPOSE ${PORT}

# ===================================================================
# CONFIGURACIÓN DE SPRING BOOT
# ===================================================================
# Configura Spring para usar el puerto dinámico de Render
ENV JAVA_OPTS="-Dserver.port=${PORT}"

# ===================================================================
# PUNTO DE ENTRADA - EJECUCIÓN DE LA APLICACIÓN
# ===================================================================
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
