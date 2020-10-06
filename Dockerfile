FROM openjdk:8-jdk-alpine

RUN apk update && apk add wine && ln -s /usr/bin/wine64 /usr/bin/wine

# Prepare tessearct
#RUN yes | apt install tesseract-ocr
#RUN mkdir -p /usr/share/tessdata
#ADD https://github.com/tesseract-ocr/tessdata/raw/master/lit.traineddata /usr/share/tessdata/lit.traineddata
#ADD https://github.com/tesseract-ocr/tessdata/raw/master/eng.traineddata /usr/share/tessdata/eng.traineddata
#RUN chown -R spring:spring /usr/share/tessdata && chmod 770 /usr/share/tessdata
#RUN tesseract --list-langs
#RUN tesseract -v

# Prep formatter
ADD /Volumes/MMSandisk/tilde/formatter /usr/local/

EXPOSE 8080

COPY ws/build/libs/*.jar /app.jar
ENV APP_PATH /app.jar
ENV APP_ENV=dev
ENV APP_PORT=8080

ENTRYPOINT java -jar /app.jar
