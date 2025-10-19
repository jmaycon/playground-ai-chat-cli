# ---- Build ----
FROM maven:3.9.11-eclipse-temurin-25-alpine AS build
WORKDIR /src
COPY pom.xml .
RUN mvn -e -DskipTests dependency:go-offline -T 3C
COPY src ./src
RUN mvn -DskipTests package
COPY opentelemetry-javaagent.jar /src/assets/otel-agent.jar

# ---- Runtime ----
FROM eclipse-temurin:25-jre
WORKDIR /app

COPY --from=build /src/target/*-SNAPSHOT.jar /app/app.jar
COPY --from=build /src/assets/otel-agent.jar /otel/otel-agent.jar

ENV JAVA_TOOL_OPTIONS="-javaagent:/otel/otel-agent.jar"

# Optional: smaller or larger heap as needed
ENV JAVA_OPTS=""

ENTRYPOINT ["sh","-c","java $JAVA_OPTS -jar /app/app.jar"]
