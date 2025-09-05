# build 階段
FROM maven:3-openjdk-17 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline
COPY src ./src
RUN mvn package -DskipTests

# run 階段
FROM openjdk:17-jdk-slim
WORKDIR /app

# 複製任何 jar，並在容器裡命名成 app.jar
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
