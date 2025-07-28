FROM openjdk:23-jdk-slim
WORKDIR /app
COPY . .
RUN mkdir -p build && \
    find src -name "*.java" | xargs javac -d build -cp src
EXPOSE 8080
CMD ["java", "-cp", "build", "Main"]