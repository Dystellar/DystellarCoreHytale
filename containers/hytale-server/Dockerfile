FROM alpine:latest

RUN apk add openjdk25-jdk

COPY entrypoint.sh /usr/bin/entrypoint.sh

ENTRYPOINT [ "entrypoint.sh" ]
