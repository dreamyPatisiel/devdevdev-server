FROM openjdk:21-jdk
# JAR 파일 메인 디렉토리에 복사
COPY build/libs/*.jar app.jar

# 타임존 설정
ENV TZ Asia/Seoul

# 시스템 진입점 정의
CMD java -jar -Dspring.profiles.active=local /app.jar