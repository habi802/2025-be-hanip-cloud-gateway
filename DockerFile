FROM amazoncorretto:21-alpine

WORKDIR /deploy

COPY build/libs/app-0.0.1-SNAPSHOT.jar app.jar

RUN apk add tzdata && ln -snf /usr/share/zoneinfo/Asia/Seoul /etc/localtime

ENV TZ=Asia/Seoul

CMD ["java", "-jar", "-Duser.timezone=Asia/Seoul", "/deploy/app.jar", "--spring.profiles.active=prod"]

EXPOSE 8080