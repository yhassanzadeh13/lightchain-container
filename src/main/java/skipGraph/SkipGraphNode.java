package skipGraph;

import java.io.FileNotFoundException;
import java.util.List;

import remoteTest.PingLog;

public interface SkipGraphNode {

	/*
	 * This are the signatures of the methods we are calling using RMI for the
	 * SkipNode method.
	 */

	public NodeInfo getLeftNode(int level, int num);

	public NodeInfo getRightNode(int level, int num);

	public String getNameID();

	public String getAddress();

	public String getLeftNameID(int level, int num);

	public String getRightNameID(int level, int num);

	public NodeInfo getNode(int num);

	public int getNumID();

	public int getLeftNumID(int level, int num);

	public int getRightNumID(int level, int num);

	public void delete(int num);

	/*
	 * Setters
	 */

	public boolean setLeftNode(int num, int level, NodeInfo newNode, NodeInfo oldNode);

	public boolean setRightNode(int num, int level, NodeInfo newNode, NodeInfo oldNode);

	/*
	 * Searches
	 */

	public NodeInfo searchByNameID(String targetString);

	public NodeInfo searchByNumID(int targetNum);

	public List<NodeInfo> searchByNumIDHelper(int targetNum, List<NodeInfo> lst);

	public List<NodeInfo> searchNumID(int numID, int searchTarget, int level, List<NodeInfo> lst);

	/*
	 * Skip Node with data nodes functions
	 */

	public NodeInfo searchName(int numID, String searchTarget, int level, int direction);

	public NodeInfo insertSearch(int level, int direction, int num, String target) throws FileNotFoundException;

	// Pinging

	public boolean ping();

	public PingLog pingStart(NodeInfo node, int freq);

	public PingLog retroPingStart(NodeInfo node, int freq);

}
