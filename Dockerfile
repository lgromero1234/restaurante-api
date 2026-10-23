# Compilacion Maven con Java 17 (etapa 1)
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /build

# Aprovecha la cache de Docker para descargar dependencias antes que el codigo.
COPY pom.xml ./
RUN mvn -B -ntp -DskipTests dependency:go-offline

COPY src ./src
RUN mvn -B -ntp -DskipTests package

# Ejecucion: imagen Java sin Maven ni fuentes (etapa 2)
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /build/target/restaurante-api-1.0.0.jar /app/app.jar

EXPOSE 8080
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75"
USER 10001:10001
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
