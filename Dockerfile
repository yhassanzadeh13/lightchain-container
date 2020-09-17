FROM ethereum/solc:0.4.24 as builder
RUN  mkdir /app
COPY target/LightChain-*-jar-with-dependencies.jar /app
COPY Lightchain/src/main/resources/log4j.properties /app
COPY simulation.config /app
COPY solidityContracts/*.sol /app
WORKDIR /app
# NOTE: I hard coded the filename here (eg. transf.sol)
RUN  solc -o . --bin transf.sol

FROM openjdk:8u242-jre
WORKDIR /app
COPY --from=builder /app .
# NOTE: I hard coded the version here
# TODO: Make the filename below change dynamically with new versions
CMD ["java", "-cp", "LightChain-0.0.1-SNAPSHOT-jar-with-dependencies.jar", "simulation.SimulationDriver"]
