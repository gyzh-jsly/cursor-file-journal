# 使用 Java 11 作为基础镜像
FROM openjdk:11-jre-slim

# 设置工作目录
WORKDIR /app

# 复制 JAR 包到容器（把 xxx 替换成你实际的 JAR 文件名）
# E:\WorkSpace\cursor-file-journal\target\file-journal-0.0.1-SNAPSHOT.jar
COPY target/file-journal-0.0.1-SNAPSHOT.jar app.jar

# 暴露端口（改成你 Spring Boot 项目配置的端口，默认 8080）
EXPOSE 8080

# 启动命令
ENTRYPOINT ["java", "-jar", "app.jar"]