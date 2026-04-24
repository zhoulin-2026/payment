FROM maven:3.8.6-openjdk-8-slim AS build
WORKDIR /app

# 复制 pom.xml 并下载依赖（利用 Docker 缓存）
COPY pom.xml .
RUN mvn dependency:go-offline -B

# 复制源代码并打包
COPY src ./src
RUN mvn package -DskipTests -B

# 运行时镜像
FROM openjdk:8-jre-slim
WORKDIR /app

# 复制打包好的 JAR 文件
COPY --from=build /app/target/*.jar app.jar

# 暴露端口（Render 会使用该端口）
EXPOSE 8089

# 运行应用
ENTRYPOINT ["java", "-jar", "app.jar"]
