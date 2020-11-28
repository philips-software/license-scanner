FROM philipssoftware/python:java

RUN pip install scancode-toolkit[full]

COPY build/libs/license-scanner-service.jar /app/service.jar
COPY docker/start.sh /app/start.sh

EXPOSE 8080

ENTRYPOINT ["/app/start.sh"]
