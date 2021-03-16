FROM azul/zulu-openjdk:8

RUN useradd -m spring

RUN dpkg --add-architecture i386
RUN apt-get update
RUN yes | apt-get install wine32
RUN yes | apt-get install wine64

# Prep formatter
RUN mkdir -p /usr/local/formatter
COPY formatter /usr/local/formatter

EXPOSE 8080

COPY ws/build/libs/*.jar /app.jar
ENV APP_PATH /app.jar
ENV APP_ENV=dev
ENV APP_PORT=8080

RUN chown spring:spring $APP_PATH && chmod 500 $APP_PATH
RUN mkdir -p /usr/local/formatter && chown -R spring:spring /usr/local/formatter && chmod -R 777 /usr/local/formatter
RUN mkdir -p /usr/local/res && chown -R spring:spring /usr/local/res && chmod -R 777 /usr/local/res

USER spring:spring
ENTRYPOINT ["java", "-Dspring.profiles.active=dev", "-DdockerHost=${DOCKER_HOST}", "-Dserver.port=8080", "-DtoolPath=/usr/local/formatter", "-DoutPath=/usr/local/res", "-jar", "/app.jar"]
