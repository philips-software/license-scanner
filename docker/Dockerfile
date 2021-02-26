FROM philipssoftware/python:java

RUN /usr/local/bin/python -m pip install --upgrade pip

RUN pip install scancode-toolkit==21.2.25

COPY build/libs/license-scanner-service*.jar /app/service.jar
COPY docker/start.sh /app/start.sh

EXPOSE 8080

ADD REPO .
ADD TAGS .

RUN mkdir /root/.ssh

ENTRYPOINT ["/app/start.sh"]
