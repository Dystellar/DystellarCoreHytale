FROM eclipse-temurin:25-jdk

ARG AUTH_ENCRYPT_PATH
ARG JAR_PLUGIN_PATH
ARG HYTALE_RELEASE_PATH
ARG BASEDIR
ARG SERVER_NAME
ARG BACKEND
ARG RAM_ALLOC
ARG API_KEY

RUN apt-get update && apt-get install -y --no-install-recommends \
    unzip \
    && rm -rf /var/lib/apt/lists/*

ENV AUTH_ENCRYPT_PATH=${AUTH_ENCRYPT_PATH} \
    JAR_PLUGIN_PATH=${JAR_PLUGIN_PATH} \
    HYTALE_RELEASE_PATH=${HYTALE_RELEASE_PATH} \
    BASEDIR=${BASEDIR} \
    SERVER_NAME=${SERVER_NAME} \
    BACKEND=${BACKEND} \
    RAM_ALLOC=${RAM_ALLOC} \
    API_KEY=${API_KEY}

COPY entrypoint.sh /usr/bin/entrypoint.sh

ENTRYPOINT ["entrypoint.sh"]

EXPOSE 5520
