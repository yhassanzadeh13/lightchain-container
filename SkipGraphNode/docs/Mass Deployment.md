# Mass Deployment Guide
A guide on how to deploy a large number of nodes using a given configuration conveniently. 

## Setting a configuration file
To deploy a number of nodes, a configuration for each node is required. To do this, there will be two seperate configuration files. One for the "Master" node and an identical configuration file for each of the "Slave" nodes. 
### Master node configuration
The master node is the node that all the slave nodes would use as their introducer. It will parse its configuration file and then 'give out' a configuration to each node that attempts to connect to it.

For the master node, the configuration file should be named node.conf and should be in the same directory as the jar file/main directory. The syntax of the configuration file is as follows:
```
16 //Number of nodes to deploy
introducer=none //The following four lines is the configuration of the master node
nameID=1010
numID=1
port=1099
introducer=xxx.xxx.xxx.xxx:xxxx //Configuration of each of the slave nodes
nameID=0011
numID=8
port=1100
introducer=xxx.xxx.xxx.xxx:xxxx
nameID=0000
numID=16
port=1101
```
For the current purpose, the introducer field is not required but is kept for future purposes.

### Slave node configuration
The slave node's configuration file is simple and consists of only the ip address + port of the master node it will connect to and get the needed configuration from. The syntax of its configuration file is as follows:
```
introducer=xxx.xxx.xxx.xxx:xxxx
```
The configuration file should also be named node.conf and should be in the same directory.

## Deploying all the nodes
In order to deploy the whole graph, it is first required to deploy the master node. After deploying the master node, its IP address and port can be used to create the configuration file of the slave nodes. In order to deploy a master node, the field "testingMode" in the SkipNode class should be modified before running. The following states are currently possible:
```java
private static int testingMode = 1;/*   0 = normal functionality
					1 = master: Gives out a number (N) of configurations to first N nodes connecting to it
					2 = slave: opens local config file and connects to the master as its introducer
										*/	 
```
Setting the testingMode field to 1 and running would run a master node. After that, the field should be switched to  2 and whatever number of nodes required should be ran. 

**Note: Do not forget to swap out the configuration files in order for the nodes to work properly.**

Using this, a large number of nodes can be deployed very conveniently. After running all the nodes, all their interactions are entirely identical to a normal SkipNode functions and thus it would be possible to test anything either using each node's console or using the main remote control (more convenient to use).
