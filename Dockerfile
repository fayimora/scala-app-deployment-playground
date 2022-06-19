FROM openjdk:11-slim

USER root

COPY target/scala-3.1.2/scala-app-deployment-* /app/run-app

COPY ./startup.sh /app/startup.sh

ENV SERVICE="zio-hello-world"

HEALTHCHECK --interval=30s --timeout=5s --retries=3 \
    CMD curl -f http://localhost:8090/hello || exit 1

CMD ["/bin/sh", "/app/startup.sh"]
