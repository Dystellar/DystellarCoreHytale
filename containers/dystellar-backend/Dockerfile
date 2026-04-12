FROM alpine:latest

ARG BACKEND_PATH
ARG API_KEY
ARG CLIENT_ID
ARG CLIENT_SECRET
ARG REDIRECT_URI
ARG PRIVILEGED_AUTHORIZED_IP

RUN apk update && apk upgrade && apk add --no-cache openssl rust cargo git openssl-dev openssl-libs-static curl libgcc gcc musl-dev

COPY entrypoint.sh /usr/bin/entrypoint.sh

ENV BACKEND_PATH=${BACKEND_PATH} \
    API_KEY=${API_KEY} \
    CLIENT_ID=${CLIENT_ID} \
    CLIENT_SECRET=${CLIENT_SECRET} \
    REDIRECT_URI=${REDIRECT_URI} \
    PRIVILEGED_AUTHORIZED_IP=${PRIVILEGED_AUTHORIZED_IP}

HEALTHCHECK --interval=30s --timeout=5s --start-period=10s --retries=3 \
    CMD curl -f http://localhost:3000/api/signal/status || exit 1

ENTRYPOINT [ "entrypoint.sh" ]

EXPOSE 3000
