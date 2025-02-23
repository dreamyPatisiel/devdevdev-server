FROM openjdk:21-jdk
# JAR 파일 메인 디렉토리에 복사
COPY build/libs/*.jar app.jar

# 타임존 설정
ENV TZ Asia/Seoul

# 시스템 진입점 정의
# CMD java -jar -Dspring.profiles.active=local /app.jar
CMD java -jar -Dspring.profiles.active=prod -javaagent:/pinpoint-agent/pinpoint-bootstrap-3.0.0.jar -Dpinpoint.agentId=devdevdev -Dpinpoint.applicationName=devdevdev-server /app.jar

ENTRYPOINT ["java", \
 "-javaagent:/pinpoint-agent/pinpoint-bootstrap-3.0.0.jar", \
 "-Dpinpoint.agentId=devdevdev", \
 "-Dpinpoint.applicationName=devdevdev-server", \
 "-Dspring.profiles.active=prod", \
 "-jar", "/app.jar"]