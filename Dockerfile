FROM openjdk:17-jre-slim

WORKDIR /app

# Copy the JAR file
COPY target/saga-orchestrator-1.0.0.jar app.jar

# Expose the application port
EXPOSE 8080

# Set JVM options for production
ENV JAVA_OPTS="-Xms512m -Xmx1024m -XX:+UseG1GC"

# Run the application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"] 