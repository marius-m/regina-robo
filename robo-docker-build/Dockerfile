FROM debian:buster-slim
RUN dpkg --add-architecture i386
RUN apt-get update
RUN apt-get install --no-install-recommends --assume-yes wine wine32
RUN wine cmd.exe /c echo '%ProgramFiles%'

RUN mkdir tts_output
RUN mkdir tts_input
COPY ./formatter formatter
COPY ./run-formatter.sh /

CMD ["./run-formatter.sh"]
