FROM philipssoftware/python:java

RUN /usr/local/bin/python -m pip install --upgrade pip
RUN pip install scancode-toolkit[full]

COPY build/libs/license-scanner-service*.jar /app/service.jar
COPY docker/start.sh /app/start.sh

EXPOSE 8080

ADD REPO .
ADD TAGS .

ENTRYPOINT ["/app/start.sh"]
