# Issues encountered using RMI over the internet
In this document a problem encountered using RMI over the internet will be highlighted along with the solution to it.
## The Internet Connectivity Issue
The nodes were functioning fine both when tested locally as well as when tested on AWS EC2 instances that were locate in the <b>same region.</b> However, whenever nodes were deployed in **different regions**, such as nodes in Ohio attempting to connect to others in Frankfurt. 
First, let's see how RMI is set up in the SkipNodes:
```java
SkipNode skipNode = new SkipNode(); //The node constructor

Registry reg = LocateRegistry.createRegistry(RMIPort); //Locating the registry
reg.rebind("RMIImpl", skipNode); //rebinding the current node to the registry
log("Rebinding Successful");
```
And this is the insides of the constructor:
```java
protected SkipNode() throws RemoteException{
	super();
	try {
		String st = grabIP();//This grabs the public IP of the node from online services
		System.setProperty("java.rmi.server.hostname",st); //Sets the IP address 
		System.out.println("RMI Server proptery set. Inet4Address: "+st);
	}catch (Exception e) {
		System.err.println("Exception in constructor. Please terminate the program and try again.");
	}
}
```
This would not work. The nodes would work just fine on local networks but not over the internet. Assume we have two SkipNodes, nodeA in Ohio and nodeB in Singapore. Whenever nodeA, for example, would attempt to connect over the internet to nodeB in order to insert itself, the following exception would be thrown:
```java
Exception creating connection to: 192.168.XXX.XXX; nested exception is: 
java.net.SocketException: Network is unreachable
at sun.rmi.transport.tcp.TCPEndpoint.newSocket(TCPEndpoint.java:632)
at sun.rmi.transport.tcp.TCPChannel.createConnection(TCPChannel.java:216)
at sun.rmi.transport.tcp.TCPChannel.newConnection(TCPChannel.java:202)
at sun.rmi.server.UnicastRef.invoke(UnicastRef.java:128)
```
Here, 192.168.XXX.XXX, would be the local address of **nodeB**. This means that nodeA established a connection to nodeB, and managed to take a remote reference using Naming.lookup to nodeB (in order to call methods using RMI), but the remote reference, whenever used, would send an RMI call to the wrong address (the **local** address of nodeB instead of the **public** address). 

## The Solution

This issue had to do with the java.rmi.server.hostname system property. Despite the property being set before *rebinding*  the object to the registry, the issue went further than that. This issue is highlighted in the [RMI FAQ](https://docs.oracle.com/javase/6/docs/technotes/guides/rmi/faq.html#domain), and the solution suggested is to change the java.rmi.server.hostname to the public IP, which was already been done. 

However, despite the hostname property being set in the constructor of the Remote object, this was not enough. When the Remote object is constructed, the current hostname property is bound to the object and is the incorrect address that was being called in nodeA. 

The working solution is to set the property before any Remote object is constructed. In SkipNode's case, a seperate init() method was used for initialization before the constructor was called. This is the method:
```java
	public static void init() {
		try {
			System.setProperty("java.rmi.server.hostname",IP);
			System.setProperty("java.rmi.server.useLocalHostname", "false");
			System.out.println("RMI Server proptery set. Inet4Address: "+IP);
			log("My Address is :" + address);
		}catch (Exception e) {
			System.err.println("Exception in initialization. Please try running the program again.");
			System.exit(0);
		}
	} 
```

And for the rebinding of the SkipNode:

```java
			init()
			SkipNode skipNode = new SkipNode();
			if(testingMode == 2) skipNode.insert(new NodeInfo(address,numID,nameID));

			Registry reg = LocateRegistry.createRegistry(RMIPort);
			reg.rebind("RMIImpl", skipNode);
			log("Rebinding Successful");
```	

Now, with the hostname property being set before any rebinding or any Remote object being constructed, the SkipNodes can function properly over the internet.
