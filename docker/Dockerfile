FROM philipssoftware/python:java

RUN python -m pip install --upgrade pip

COPY docker/requirements.txt /app/requirements.txt
RUN pip install scancode-toolkit[full] -c /app/requirements.txt

COPY build/libs/license-scanner-service*.jar /app/service.jar
COPY docker/start.sh /app/start.sh

EXPOSE 8080

ADD REPO .
ADD TAGS .

RUN mkdir /root/.ssh

ENTRYPOINT ["/app/start.sh"]
