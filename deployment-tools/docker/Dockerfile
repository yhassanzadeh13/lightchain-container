FROM openjdk:8-jre-alpine

RUN mkdir /app/
COPY ./light-chain-node.jar  /app/
# Use ./master/run.sh or ./slave/run.sh depending on the image you want to build
COPY ./master/run.sh  /app/run.sh
RUN ["chmod", "+x", "/app/run.sh"]

EXPOSE 8080

ENTRYPOINT ["/app/run.sh"]
