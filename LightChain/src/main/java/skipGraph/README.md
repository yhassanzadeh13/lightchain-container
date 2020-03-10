SkipNode 
===============

This class represents a single node in the skip graph network. The implementation supports several functionalities that a SkipNode can 
carry out.These functionalities include:
* Inserting the node into the network with the help of the introducer node.  
* Inserting data nodes to the network that have the same address but with different numID and nameID.  
* Searching by Numerical ID (works in a recursive manner).  
* Searching by Name ID (works in a recursive manner).  

In addition, the class contains several helper methods that facilitate the execution of the methods responsible for the 
forementioned functionalities.  
Java RMI is the medium through which skip nodes communicate in the network. This is why the class implements RMIInterface and extends 
UnicastRemoteObject.  
It is assumed that the reader of this documentation has a basic understanding of the structure of a skip graph.  

Description
--------------

* ```java 
  public void insert(NodeInfo thisNode)
  ```
    This method recieves a NodeInfo instance (basically containing address +  numID + nameID of a node) and inserts it
    into the network with the help of the introducer node.  
    
    Let's call the to-be-inserted node as **thisNode**. First, it asks the 
    introducer to conduct a search on the numID of thisNode. The result of this search is supposed to be a neighbor of thisNode,
    so we check if it is the right or left neighbor, and update the lookup tables at level 0 of thisNode and its neighbors accordingly.  
    Now that we now its position at level 0, we go up level by level and find its neighbors in every level. At level *i* , we keep going
    in both left and right directions until we reach a node that has a nameID such that the length of the common prefix of the found node
    and the nameID of thisNode is at least *i* ,in other words, we keep going until we find a node (call it **otherNode**) such that
    ``` CommonBits(thisNode.nameID,otherNode.nameI) >= *i* ```. After we find the neighbors at level *i*, we update lookup tables for the
    nodes to insert thisNode, and we start the search at level *i+1* starting from the neighbors we found at level *i*. The insertions 
    continues at this fashion until we either reach an empty side at some level, or reach the last level.  
    
    The method ``` public NodeInfo insertSearch(int,int,int,String) ``` is used to make the search for insertion place work recursive 
    per level. That is, the search at level *i* for neighbors of thisNode is done by recursive calls using java RMI.  
    
    This methods works for inserting the node itself the first time it enters the network, and supports inserting data nodes, which
    are defined to be additional nodes that have the same address as their owner (the node that inserts them) but different numID
    and nameID. The idea of data nodes is extensible to accommodate the further blockchain implementation requirements, which are
    not discussed in this file.
    
    ---  
    
* ```java
  public NodeInfo searchByNumID(int searchTarget)
  ```
    This method receives an integer **searchTarget** and searches for the node that carries this numID or if such a node 
    does not exist, returns the node with the closest numID.  
    
    The search starts at the last level of the skipGraph. The searchTarget is compared with the numID of the current node. If 
    search target is less, then the search should be carried out in the left direction, otherwise it should be in the right direction.
    Assuming ,without loss of generality, that the search should be to the right, we look at the right neighbor, if its numID is less
    than or equal to the searchTarget, then we recursively call the search method of that node using RMI and the same argument that
    is carried out in the node will be applied there. Otherwise, if the numID of the neighbor node is greater than the searchTarget or
    there are no nodes in the right side, then this is a signal that we should go search in lower levels. Hence, we keep moving down
    until we either reach level 0, or until we find a right neighbor whose numID is less than or equal to searchTarget, or in
    case we have found our target. If we reach level 0, then we just return the node that we are currently at because its numID is
    closest to searchTarget. If at some level we find a neighbor that has numID less than or equal to searchTarget, we call its 
    search method and apply the same arguments we have applied on the current node and so on.  
    
    The method ``` public NodeInfo searchNum(int,int) ``` is where most of the work described above is done. We had to do so in order
    to make the method work in a recursive way.
    
    ---
    
* ```java
  public NodeInfo searchByNameID(String searchTarget)
  ```
    This method recieves a string **searchTarget** and searches for the node that carries this nameID and returns it, or if such a node
    does not exist, returns the node with a nameID that has the longest common prefix with searchTarget.  
    
    The search start at level *k*, where *k* is the longest common prefix of nameID of searchTarget and nameID of node from which
    the search starts, i.e ` k = CommonBits(searchTarget,curNode.nameID) `, where **curNode** is the node at which the search is 
    being processed currently. From curNode, we can search either in left direction or right direction, so we first recursively 
    call the search of any of the two neighbors and wait for the returned result. In case the returned result is not the exact 
    target, we call the search on the other neighbor. When the search call arrives at a node, if it was not the target, we calculate 
    the length of the longest common prefix between searchTarget and the nameID of the node we reached, let it be *len*. If *len >= current level*
    then we first go to the level *len* and then call the search on both sides in the same fashion described. The search ends
    when we either find the target, or when we reach a dead end on both sides of the skip graph.  
    
    The method ``` public NodeInfo searchName(String,int,int) ``` is where most of the described work happens, and it was mainly 
    added in this manner to make the method work in a recursive way.
    
    ---
    
Helping Methods
------------------

* ```java
  public void setInfo(int num)
  ```
    This method is responsible for getting from the user the necessary information to get the node running.
    Not only this, but it also checks if the given information is in the proper format. It mainly asks the user to enter:  
    * _The address of the introducer_. In case this is the first node in the network, then user should enter "none". The address
      should be in the form IP:PORT .  
    * _RMI PORT_ . The port on your device on which you would like to open the RMI registry.
----

* ```java
  public int getBestNum(int num)
  ```
    This method is specific to the data nodes insertion functionality. Since there might be more than one node in the network
    with the same address, if a search passes through one of these nodes, we look for the node that is closest to the target among
    these nodes with common address, and route the search to it. So this method iterates over the nodes at the current address,
    and returns the numID of the node that has the closest numID to *num*.
---

* ```java
  public int getBestName(String name)
  ```
    This method is specific to the data nodes insertion functionality. Since there might be more than one node in the network
    with the same address, if a search passes through one of these nodes, we look for the node that is closest to the target among
    these nodes with common address, and route the search to it. So this method iterates over the nodes at the current address,
    and returns the numID of the node with nameID that shares the longest prefix with *name*.
---

* ```java
  public static RMIInterface getRMI(String adrs)
  ```
    This method takes the address of a node and returns an RMIInterface instance which we can use to access the methods
    of the node with the given address
---

* ```java
  public static int commonBits(String name1, String name2)
  ```
    This method simply takes two strings and returns the length of the longest common prefix of the two strings.
---

* ```java
  public static void printLookup(int num)
  ```
    This method prints the lookup table of the node with the given numID, it used mainly for testing purposes.
 ---

* ```java
  public void log(String s)
  ```
    This method is just a shorter way to print to console.
---

* ```java
  public static String get()
  ```
    This method is a shorter way to get input from user.





 
 
 
