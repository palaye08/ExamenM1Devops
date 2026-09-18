# =========================================================
# Dockerfile - Application Spring Boot (Exam DevOps)
# Build multi-stage : Maven pour compiler, JRE pour exécuter
# =========================================================

# --- Étape 1 : Build avec Maven ---
FROM maven:3.9-amazoncorretto-17 AS build
WORKDIR /app

# Copier pom.xml et télécharger les dépendances en cache
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copier les sources et compiler
COPY src ./src
RUN mvn package -DskipTests

# --- Étape 2 : Image d'exécution légère ---
FROM amazoncorretto:17-alpine
WORKDIR /app

# Copier le JAR compilé depuis l'étape build
COPY --from=build /app/target/*.jar app.jar

# Exposer le port applicatif
EXPOSE 8080

# Lancer l'application
ENTRYPOINT ["java", "-jar", "app.jar"]
