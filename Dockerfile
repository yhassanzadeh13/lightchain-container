FROM ethereum/solc:0.4.24 as builder
# We use a solidity container to compile our contracts

# All our files are transfered to a single folder called "app"
RUN  mkdir /app
COPY target/LightChain-*-jar-with-dependencies.jar /app
COPY Lightchain/src/main/resources/log4j.properties /app
COPY simulation.config /app
COPY solidityContracts/*.sol /app
WORKDIR /app
# NOTE: I hard coded the filename here (eg. testcon.sol)
RUN  solc -o . --bin testcon.sol

# Here we use a java container for further simulation
FROM openjdk:8u242-jre
WORKDIR /app
# The files from above solidity container are copied into this current container
COPY --from=builder /app .

# NOTE: I hard coded the version here
# TODO: Make the filename below change dynamically with new versions
CMD ["java", "-cp", "LightChain-0.0.1-SNAPSHOT-jar-with-dependencies.jar", "simulation.SimulationDriver"]
