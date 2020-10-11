FROM openjdk:8u242-jre

run mkdir /app
COPY target/LightChain-*-jar-with-dependencies.jar /app
# COPY lightchain-container/src/main/resources/log4j.properties /app
COPY simulation.config /app

WORKDIR /app

# NOTE: I hard coded the version here
# TODO: Make the filename below change dynamically with new versions
CMD ["java", "-cp", "LightChain-0.0.1-SNAPSHOT-jar-with-dependencies.jar", "simulation.SimulationDriver"]
