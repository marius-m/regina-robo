FROM azul/zulu-openjdk:8

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

ENTRYPOINT java -Dspring.profiles.active=dev -Dserver.port=8080 -DtoolPath=/usr/local/formatter -jar /app.jar