
# Overview
This repository implements a proof-of-concept version of [LightChain](https://arxiv.org/pdf/1904.00375.pdf) blockchain.

# Running Simulation

## Setting the parameters:
Modify the simulation parameters in simulation.config, which looks something like this:
```
nodeCount = 3 [//The](//the) number of nodes to spawn
iterations = 100 // The number of transactions to generate
pace = 1 // The time between every transaction generated (in seconds)
alpha = 10  // upper limit of attempts to search for validators
txmin = 1 // minimum number of Tx in a block
signaturesThreshold = 1 // minimum number of signatures to accept a block
initialBalance = 20  // balance to start with at launch
levels = 30  // length of nameID (levels of skip graph)
Mode = True // honest or malicious
validationFees = 1 // reward received by validator

Token = 20 // Represents any type of asset

// Mode of lightchain 
//(False -> original || True -> Smartcontract)
ContractMode = False 
```
## Pre-requisite redarding smart contract integration
In solidityContracts folder put your .sol file. 
Detailed instructions are given in [docs](contracts/smartcontract.md).

## Running the simulation 

Then you have to build the container. You need to do this step for any change in the code. 
```
make build-container
```
Then you run the docker container. If needed, you can change the name of the docker container `simdocker` to anything else, provided you replace all other instances of the name subsequently.
```
sudo docker run --name simdocker -d lightchain
```
Then, to copy all the logs and the simulation results, use the following command:
```
sudo docker cp simdocker:/app/ .
```

## Removing existing docker containers

If you have too many containers that are stopped and you want to remove them, the following command is useful:
```
sudo docker rm $(sudo docker ps -a -q)
```