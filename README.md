
# Overview
This repository implements a proof-of-concept version of [LightChain](https://arxiv.org/pdf/1904.00375.pdf) blockchain.

# Running Simulation

## Setting the parameters:
Modify the simulation parameters in simulation.config, which looks something like this:
```
nodeCount = 3 // number of nodes to spawn
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
Detailed instructions are given in [docs](solidityContracts/smartcontract.md).

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

## Publications
- Hassanzadeh-Nazarabadi, Yahya, Alptekin Küpçü, and Öznur Özkasap. "LightChain: Scalable DHT-Based Blockchain." IEEE Transactions on Parallel and Distributed Systems 32.10 (2021): 2582-2593. [(PDF)](https://ieeexplore.ieee.org/abstract/document/9397334)
- Hassanzadeh-Nazarabadi, Yahya, Nazir Nayal, Shadi Sameh Hamdan, Öznur Özkasap, and Alptekin Küpçü. "A containerized proof-of-concept implementation of lightchain system." In 2020 IEEE International Conference on Blockchain and Cryptocurrency (ICBC), pp. 1-2. IEEE, 2020. [(PDF)](https://arxiv.org/pdf/2007.13203.pdf)
- Hassanzadeh-Nazarabadi, Yahya, Alptekin Küpçü, and Öznur Özkasap. "Lightchain: A dht-based blockchain for resource constrained environments." arXiv preprint arXiv:1904.00375 (2019). [(PDF)](https://arxiv.org/pdf/1904.00375.pdf)

## Citation
For citing this implementation in a publication please use: 
_Hassanzadeh-Nazarabadi, Yahya, Alptekin Küpçü, and Öznur Özkasap. "LightChain: Scalable DHT-Based Blockchain." IEEE Transactions on Parallel and Distributed Systems 32.10 (2021): 2582-2593._
```
@article{hassanzadeh2021lightchain,
  title={LightChain: Scalable DHT-Based Blockchain},
  author={Hassanzadeh-Nazarabadi, Yahya and K{\"u}p{\c{c}}{\"u}, Alptekin and {\"O}zkasap, {\"O}znur},
  journal={IEEE Transactions on Parallel and Distributed Systems},
  volume={32},
  number={10},
  pages={2582--2593},
  year={2021},
  publisher={IEEE}
}
```
