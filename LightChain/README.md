# Running a Simulation:
First you have to build the container. You need to do this step for any change in the code.
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