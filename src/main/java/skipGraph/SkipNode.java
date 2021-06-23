package skipGraph;

import org.apache.log4j.Logger;
import remoteTest.Configuration;
import remoteTest.PingLog;
import remoteTest.TestingLog;
import underlay.Underlay;
import underlay.requests.skipgraph.*;
import underlay.responses.IntegerResponse;
import underlay.responses.NodeInfoListResponse;
import underlay.responses.NodeInfoResponse;
import util.Const;
import util.Util;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static underlay.responses.IntegerResponse.IntegerResponseOf;
import static underlay.responses.NodeInfoListResponse.NodeInfoListResponseOf;
import static underlay.responses.NodeInfoResponse.NodeInfoResponseOf;

public class SkipNode implements SkipGraphNode {

    private static final long serialVersionUID = 1L;

    protected NodeInfo peerNode;
    protected String address;
    protected String nameID;
    protected String IP;
    private String introducer;
    private int maxLevels;
    protected int numID;
    protected int port;
    protected boolean isInserted = false;
    private LookupTable lookup;
    private Logger logger;

    protected Underlay underlay;

    // TODO: fork-resolving mechanism unimplemented
    // TODO: bootstrapping unimplemented


    /**
     * This version of the constructor is used as long as LightChainNode extends SkipNode
     * Because it defers RMI binding and insertion task to LightChainNode after setting
     * the correct information (numID and nameID
     *
     * @param introducer
     */
    public SkipNode(int port, int maxLevels, String introducer, Underlay underlay) {
        this.port = port;
        this.maxLevels = maxLevels;
        this.introducer = introducer;
        this.IP = Util.grabIP();
        this.address = IP + ":" + port;
        this.logger = Logger.getLogger(port + "");

        this.underlay = underlay;
        underlay.setSkipNode(this);

        // check if introducer has valid address
        if (!introducer.equals(Const.DUMMY_INTRODUCER) && !Util.validateIP(introducer)) {
            logger.error("Invalid introducer address");
        }

        // check if node address it valid
        if (!Util.validateIP(address)) {
            logger.error("Invalid node adress");
        }
        lookup = new LookupTable(maxLevels);

    }

    /**
     * Constructor for SkipNode class The node requires the following info to be
     * able to function:
     *
     * @param introducer the node that helps in inserting the current node
     * @param isInitial  Indicator that this node is an initial node in the skipGraph
     */
    public SkipNode(NodeConfig config, String introducer, boolean isInitial, Underlay underlay) {

        this(config.getPort(), config.getMaxLevels(), introducer, underlay);
        this.numID = config.getNumID();
        this.nameID = config.getNameID();

        peerNode = new NodeInfo(address, numID, nameID);
        lookup.addNode(peerNode);
        if (isInitial)
            isInserted = true;

        // TODO: this should be removed when launching LightChainNode
        if (!isInitial) {
            insertNode(peerNode);
        }

    }


    /**
     * Deletes a data node with a given numerical ID
     *
     * @param num numerical ID of node to be deleted
     */
    public void delete(int num) {

        try {
            logger.debug("Deleting :" + num);
            for (int j = lookup.getMaxLevels(); j >= 0; j--) {
                // if there are no neighbors at level j, just move on
                NodeInfo lNode = lookup.get(num, j, Const.LEFT);
                NodeInfo rNode = lookup.get(num, j, Const.RIGHT);
                NodeInfo thisNode = lookup.get(num);

                if (lNode == null && rNode == null) {
                    continue;
                    // if left is null, then update right
                } else if (lNode == null) {
                    underlay.sendMessage(new SetLeftNodeRequest(rNode.getNumID(), j, lNode, thisNode), rNode.getAddress());

                    // if right is null, update left
                } else if (rNode == null) {
                    underlay.sendMessage(new SetRightNodeRequest(lNode.getNumID(), j, rNode, thisNode), lNode.getAddress());


                    // otherwise update both sides and connect them to each other.
                } else {
                    underlay.sendMessage(new SetLeftNodeRequest(rNode.getNumID(), j, lNode, thisNode), rNode.getAddress());
                    underlay.sendMessage(new SetRightNodeRequest(lNode.getNumID(), j, rNode, thisNode), lNode.getAddress());
                }
            }
            // Delete the node from the lookup.
            lookup.remove(num);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void insertDataNode(int nodeNumID, String nodeNameID) {
        insertNode(new NodeInfo(address, nodeNumID, nodeNameID));
    }

    /**
     * This method inserts either the current node to the skip graph of the
     * introducer, or it is used to insert a data node.
     * <p>
     * TODO: investigate using a sparate function for dataNode insertion
     */
    public void insertNode(NodeInfo insertedNode) {
        try {
            logger.debug("Inserting: " + insertedNode.getNumID());
            // We search through the introducer node to find the node with
            // the closest num ID
            NodeInfo closestNode;
            if (isInserted) {
                closestNode = searchByNumID(insertedNode.getNumID());
            } else {
                isInserted = true;
                NodeInfoResponse response = NodeInfoResponseOf(underlay.sendMessage(new SearchByNumIDRequest(insertedNode.getNumID()), introducer));
                closestNode = response.result;
            }

            lookup.initializeNode(insertedNode);

            if (closestNode == null) {
                logger.error("The address resulting from the search is null");
                return;
            }

            // First, we insert the node at level 0
            // numID of the closest node
            int closestNodeNumID = closestNode.getNumID();

            NodeInfo leftNode = null;
            NodeInfo rightNode = null;
            // numID of left node
            int leftNodeNumID = Const.UNASSIGNED_INT;
            // numID of right node
            int rightNodeNumID = Const.UNASSIGNED_INT;

            // if the closest node is to the right
            if (insertedNode.getNumID() < closestNodeNumID) {
                NodeInfoResponse response = NodeInfoResponseOf(underlay.sendMessage(new GetLeftNodeRequest(Const.ZERO_LEVEL, closestNodeNumID), closestNode.getAddress()));
                leftNode = response.result;

                rightNode = Util.assignNode(closestNode);
                // we need the numID to be able to access it
                rightNodeNumID = rightNode.getNumID();
                // insert the current node in the lookup table of my left node if it exists
                if (leftNode != null) {
                    IntegerResponse leftNodeNumIDResponse = IntegerResponseOf(underlay.sendMessage(new GetNumIDRequest(), leftNode.getAddress()));
                    leftNodeNumID = leftNodeNumIDResponse.result;
                    // null because the lookup table is empty
                    lookup.put(insertedNode.getNumID(), Const.ZERO_LEVEL, Const.LEFT, Util.assignNode(leftNode), null);
                    underlay.sendMessage(new SetRightNodeRequest(leftNodeNumID, Const.ZERO_LEVEL, insertedNode, rightNode), leftNode.getAddress());
                }
                lookup.put(insertedNode.getNumID(), Const.ZERO_LEVEL, Const.RIGHT, Util.assignNode(rightNode), null);
                // insert the current node in the lookup table of its right neighbor
                underlay.sendMessage(new SetLeftNodeRequest(closestNodeNumID, Const.ZERO_LEVEL, insertedNode, leftNode), closestNode.getAddress());

            } else { // if the closest node is to the left
                NodeInfoResponse response = NodeInfoResponseOf(underlay.sendMessage(new GetRightNodeRequest(Const.ZERO_LEVEL, closestNodeNumID), closestNode.getAddress()));
                rightNode = response.result;

                leftNode = closestNode;
                leftNodeNumID = leftNode.getNumID(); // we need the numID to be able to access it
                // This is before the if statement so that the order of insertion is the same
                lookup.put(insertedNode.getNumID(), Const.ZERO_LEVEL, Const.LEFT, Util.assignNode(leftNode), null);
                // insert the current node in the lookup table of its right neighbor

                underlay.sendMessage(new SetRightNodeRequest(closestNodeNumID, Const.ZERO_LEVEL, insertedNode, rightNode), closestNode.getAddress());
                if (rightNode != null) { // insert current node in the lookup table of its right neighbor if it exists
                    rightNodeNumID = rightNode.getNumID();
                    // null because the lookup table is empty
                    lookup.put(insertedNode.getNumID(), Const.ZERO_LEVEL, Const.RIGHT, Util.assignNode(rightNode),
                            null);
                    underlay.sendMessage(new SetLeftNodeRequest(rightNodeNumID, Const.ZERO_LEVEL, insertedNode, leftNode), rightNode.getAddress());
                }
            }

            // Now, we insert the node in the rest of the levels
            // In level i , we make a recursive search for the nodes that will be
            // the neighbors of the inserted nodes at level i+1

            int level = Const.ZERO_LEVEL;
            while (level < maxLevels) {

                if (leftNode != null) {
                    NodeInfoResponse response = NodeInfoResponseOf(underlay.sendMessage(
                            new InsertSearchRequest(level, Const.LEFT, leftNodeNumID, insertedNode.getNameID()),
                            leftNode.getAddress()));
                    NodeInfo lft = response.result;
                    lookup.put(insertedNode.getNumID(), level + 1, Const.LEFT, Util.assignNode(lft), null);

                    // set left and leftNum to default values (null,-1)
                    // so that if the left neighbor is null then we no longer need
                    // to search in higher levels to the left
                    leftNode = null;
                    leftNodeNumID = Const.UNASSIGNED_INT;

                    if (lft != null) {
                        underlay.sendMessage(new SetRightNodeRequest(lft.getNumID(), level + 1, insertedNode, null), lft.getAddress());

                        leftNode = lft;
                        leftNodeNumID = lft.getNumID();
                    }
                }
                if (rightNode != null) {
                    NodeInfoResponse response = NodeInfoResponseOf(underlay.sendMessage(
                            new InsertSearchRequest(level, Const.RIGHT, rightNodeNumID, insertedNode.getNameID()), rightNode.getAddress()));
                    NodeInfo rit = response.result;

                    lookup.put(insertedNode.getNumID(), level + 1, Const.RIGHT, Util.assignNode(rit), null);

                    // set right and rightNum to default values (null,-1)
                    // so that if the right neighbor is null then we no longer need
                    // to search in higher levels to the right
                    rightNode = null;
                    rightNodeNumID = Const.UNASSIGNED_INT;

                    if (rit != null) {
                        underlay.sendMessage(new SetLeftNodeRequest(rit.getNumID(), level + 1, insertedNode, null), rit.getAddress());

                        rightNode = rit;
                        rightNodeNumID = rit.getNumID();
                    }
                }
                level++;
            }
            // after we conclude inserting the node in all levels,
            // we add the inserted node to the data array
            // and we map its numID with its index in the data array using dataID
            lookup.finalizeNode();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * A helper method for Insert(), inserts a node recursively per level.
     *
     * @param level     The level in which a node is being inserted
     * @param direction At a certain level, the method go LEFT and RIGHT finding the
     *                  neighbors of the inserted node
     * @param nodeNumID The numerical ID of the node at which the search has arrived
     * @param target    the name ID of the inserted node.
     * @return Right neighbor if direction is RIGHT, and left neighbor if direction
     * is LEFT
     * @see SkipGraphNode#insertSearch(int, int, int, java.lang.String)
     */
    public NodeInfo insertSearch(int level, int direction, int nodeNumID, String target) {
        try {
            logger.debug("Inserting " + target + " at level " + level);
            NodeInfo currentNode = lookup.get(nodeNumID);

            if (currentNode == null)
                return null;
            // If the current node and the inserted node have common bits more than the
            // current level,
            // then this node is the neighbor so we return it
            if (Util.commonBits(target, currentNode.getNameID()) > level)
                return currentNode;
            // If search is to the right then delegate the search to right if it exists
            // If the right neighbor is null then at this level the right neighbor of the
            // inserted node is null

            if (!lookup.nodeExist(nodeNumID))
                return null;

            if (direction == Const.RIGHT) {

                if (!lookup.isLockAvailable(nodeNumID))
                    wait(new Random().nextInt(900) + 100);

                NodeInfo rNode = lookup.get(nodeNumID, level, direction);
                if (rNode == null)
                    return null;
                NodeInfoResponse response = NodeInfoResponseOf(underlay.sendMessage(new InsertSearchRequest(level, direction, rNode.getNumID(), target), rNode.getAddress()));
                return response.result;
            } else {

                // If search is to the left then delegate the search left neighbor if it exists
                // If the left neighbor is null, then the left neighbor of the inserted node at
                // this level is null.
                NodeInfo lNode = lookup.get(nodeNumID, level, direction);
                if (lNode == null)
                    return null;
                NodeInfoResponse response = NodeInfoResponseOf(underlay.sendMessage(new InsertSearchRequest(level, direction, lNode.getNumID(), target), lNode.getAddress()));
                return response.result;

            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("sars");
            logLevel(Const.ZERO_LEVEL);
            return null;
        }
    }

    /**
     * This method receives a number, and returns the data node (or possibly the
     * main node) which has the closest numID to the given number
     *
     * @param num numerical ID of node for which we want to find the closest node
     *            with this address
     * @return numerical ID of node closest to given value
     */
    public int getBestNum(int num) {
        return lookup.getBestNum(num);
    }

    /**
     * Executes a search through the skip graph by numeric id and returns the a
     * NodeInfo object which contains the address, numID, and nameID of the node
     * with closest numID to the target It starts the search from last level of the
     * current node
     *
     * @param searchTarget numerical ID of target of search
     * @return NodeInfo of the target node if found, or closest node in case it was
     * not found
     */
    public NodeInfo searchByNumID(int searchTarget) {
        logger.debug("Searching for " + searchTarget);
        try {
            List<NodeInfo> lst = new ArrayList<NodeInfo>();
            lst = searchByNumIDHelper(searchTarget, lst);
            return lst == null ? null : lst.get(lst.size() - 1);

        } catch (IllegalMonitorStateException | NullPointerException e) {
            return null;
            //IllegalMonitorStateException: when the flag node is deleted and reinserted by another node
            //NullPointerException: when the flag node is deleted and the other node did not insert it yet
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * A helper method for searchByNumID, it essentially starts the search operation
     * but it supplies the search with an empty list to collect the nodes on the
     * path of the search
     *
     * @param searchTarget numerical ID to be searched
     * @param lst          the list which will collect the nodes on the search path
     * @return a list containing all nodes that have been encountered on the search
     * path
     */
    public List<NodeInfo> searchByNumIDHelper(int searchTarget, List<NodeInfo> lst) {
        try {

            int level = lookup.getMaxLevels();
            // route search to closest data node
            int num = getBestNum(searchTarget);
            if (lookup.get(num, Const.ZERO_LEVEL, Const.LEFT) == null
                    && lookup.get(num, Const.ZERO_LEVEL, Const.RIGHT) == null) {
                lst.add(lookup.get(num));
                return lst;
            }
            return searchNumID(numID, searchTarget, level, lst);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * This method is a hepler method for searchByNumID() It recieves the target
     * numID and the level it is searching in, and it routes the search through the
     * skip graph recursively using RMI
     *
     * @param numID     numerical ID of current node at which the search has arrived
     * @param targetInt the target of search
     * @param level     the level of skip graph at which we are searching
     * @return list of nodes on the search path
     */
    public List<NodeInfo> searchNumID(int numID, int targetInt, int level, List<NodeInfo> lst) {

        int num;
        if (numID != lookup.bufferNumID()) {
            // get the data node (or main node) that is closest to the target search
            num = getBestNum(targetInt);
//			num = numID;
            // Add the current node's info to the search list
        } else {
            logger.debug("Accessing Buffered Node " + port + " ...");
            num = numID;
        }
        lst.add(lookup.get(num));

        if (num == targetInt)
            return lst;

        // If the target is greater than the current node then we should search right
        if (num < targetInt) {
            logger.debug("Going Right from " + num + " to " + targetInt + "...");
            // Keep going down levels as long as there is either no right neighbor
            // or the right neighbor has a numID greater than the target
            while (level >= Const.ZERO_LEVEL && (lookup.get(num, level, Const.RIGHT) == null
                    || lookup.get(num, level, Const.RIGHT).getNumID() > targetInt))
                level--;
            // If there are no more levels to go down to return the current node
            if (level < Const.ZERO_LEVEL) {
                return lst;
            }
            // delegate the search to the right neighbor
            try {
                NodeInfoListResponse response = NodeInfoListResponseOf(underlay.sendMessage(
                        new SearchNumIDRequest(lookup.get(num, level, Const.RIGHT).getNumID(), targetInt, level, lst)
                        , lookup.get(num, level, Const.RIGHT).getAddress()));
                return response.result;
            } catch (StackOverflowError e) {
                return null;
            } catch (Exception e) {
                return lst;
            }
        } else {
            // If the target is less than the current node then we should search left
            logger.debug("Going Left from " + num + " to " + targetInt + "...");
            // Keep going down levels as long as there is either no right neighbor
            // or the left neighbor has a numID greater than the target
            while (level >= Const.ZERO_LEVEL && (lookup.get(num, level, Const.LEFT) == null
                    || lookup.get(num, level, Const.LEFT).getNumID() < targetInt))
                level--;
            // If there are no more levels to go down to return the current node
            if (level < Const.ZERO_LEVEL)
                return lst;
            // delegate the search to the left neighbor
            try {
                NodeInfoListResponse response = NodeInfoListResponseOf(underlay.sendMessage(
                        new SearchNumIDRequest(lookup.get(num, level, Const.LEFT).getNumID(), targetInt, level, lst)
                        , lookup.get(num, level, Const.LEFT).getAddress()));
                return response.result;
            } catch (StackOverflowError e) {
                logger.error("StackOverflow", e);
                StringBuilder sb = new StringBuilder();
                for (NodeInfo node : lst)
                    sb.append(node.getNumID() + " " + node.getAddress() + "\n");
                logger.error(sb.toString());
                return null;
            } catch (Exception e) {
                return lst;
            }
        }
    }

    /**
     * This method receives a nameID and returns the index of the data node which
     * has the most common prefix with the given nameID
     *
     * @param name      name ID of node for which we want to find node in this
     *                  address with closest name ID
     * @param direction direction of search
     * @return numerical ID of closest node found
     */
    public int getBestName(String name, int direction) {
        return lookup.getBestName(name, direction);
    }

    /**
     * This methods starts a search by nameID, and returns the node as an instance
     * of NodeInfo class which contains (address, numID, nameID) of the node, such
     * that the nameID of the returned node is the most similar with the
     * searchTarget. Similarity is defined to be the maximum number of common bits
     * between the two strings
     *
     * @param searchTarget name ID which we are searching for
     * @return NodeInfo of target if found, or its closest node found
     * @see SkipGraphNode#searchByNameID(java.lang.String)
     * <p>
     * TODO: currently, when a numID search for a value that does not exist in
     * the skip graph occurs, the returned result depends on the side from
     * which the search had started, if search started from the right of the
     * target, upper bound, if search starts from left of target, lowerbound is
     * returned
     */
    public NodeInfo searchByNameID(String searchTarget) {
        try {
            int bestNum = getBestName(searchTarget, 1);
            NodeInfo ansNode = lookup.get(bestNum);
            if (ansNode.getNameID().equals(searchTarget))
                return ansNode;

            int newLevel = Util.commonBits(searchTarget, ansNode.getNameID());

            // First execute the search in the right direction and see the result it returns
            if (lookup.get(bestNum, newLevel, Const.RIGHT) != null) {
                NodeInfoResponse response = NodeInfoResponseOf(underlay.sendMessage(new SearchNameRequest(lookup.get(bestNum, newLevel, Const.RIGHT).getNumID(),
                        searchTarget, newLevel, Const.RIGHT), lookup.get(bestNum, newLevel, Const.RIGHT).getAddress()));
                NodeInfo rightResult = response.result;
                int commonRight = Util.commonBits(rightResult.getNameID(), searchTarget);
                if (commonRight > newLevel)
                    ansNode = Util.assignNode(rightResult);
            }
            // If the desired result was not found try to search to the left
            if (lookup.get(bestNum, newLevel, Const.LEFT) != null) {
                NodeInfoResponse response = NodeInfoResponseOf(underlay.sendMessage(new SearchNameRequest(lookup.get(bestNum, newLevel, Const.LEFT).getNumID(),
                        searchTarget, newLevel, Const.LEFT), lookup.get(bestNum, newLevel, Const.LEFT).getAddress()));
                NodeInfo leftResult = response.result;

                int commonLeft = Util.commonBits(leftResult.getNameID(), searchTarget);
                if (commonLeft > newLevel)
                    ansNode = Util.assignNode(leftResult);
            }
            return ansNode;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * This method is a Util method for searchByNameID() It receives the target
     * nameID and the level it is searching in, and also the direction of search,
     * and it routes the search through the skip graph recursively using RMI It
     * return the most similar node if the node itself is not found The similarity
     * is defined to be the the maximum number of common bits
     *
     * @param numID        numerical ID of current node at which search has arrived
     * @param searchTarget name ID of target of search
     * @param level        the level at which search is happening
     * @param direction    direction of search currently
     * @return NodeInfo of result of search
     */

    public NodeInfo searchName(int numID, String searchTarget, int level, int direction) {
        logger.debug("Searching nameID at " + port + "...");
        try {
            // TODO: handle this after finalizing lookupTable
            if (numID == lookup.bufferNumID()) {
                // only executes when the buffer node finishes inserting
                lookup.get(numID, 0, Const.LEFT);
            }
            int bestNum = getBestName(searchTarget, direction);
            // we initialize the result to current node
            NodeInfo ansNode = lookup.get(bestNum);
            // if the current node hold the same nameID, return it.
            if (ansNode.getNameID().equals(searchTarget))
                return ansNode;

            // calculate common bits to find to which level the search must be routed
            int newLevel = Util.commonBits(ansNode.getNameID(), searchTarget);

            // If the number of common bits is not more than the current level
            // then we continue the search in the same level in the same direction
            if (newLevel <= level) {
                // If no more nodes in this direction return the current node
                if (lookup.get(bestNum, level, direction) == null)
                    return ansNode;
                NodeInfoResponse response = NodeInfoResponseOf(underlay.sendMessage(new SearchNameRequest(lookup.get(bestNum, level, direction).getNumID(), searchTarget, level,
                        direction), lookup.get(bestNum, level, direction).getAddress()));
                return response.result;
            }
            // If the number of common bits is more than the current level
            // then the search will be continued on the new level
            // so we start a search in both directions in the new level

            // First we start the search on the same given direction and wait for the result
            // it returns
            if (lookup.get(bestNum, newLevel, direction) != null) {
                NodeInfoResponse response = NodeInfoResponseOf(underlay.sendMessage(new SearchNameRequest(lookup.get(bestNum, newLevel, direction).getNumID(), searchTarget,
                        newLevel, direction), lookup.get(bestNum, newLevel, direction).getAddress()));
                NodeInfo curNode = response.result;

                int common = Util.commonBits(curNode.getNameID(), searchTarget);
                if (common > newLevel)
                    ansNode = Util.assignNode(curNode);
            }

            // Continue the search on the opposite direction
            if (lookup.get(bestNum, newLevel, 1 - direction) != null) {
                NodeInfoResponse response = NodeInfoResponseOf(underlay.sendMessage(new SearchNameRequest(lookup.get(bestNum, newLevel, 1 - direction).getNumID(),
                        searchTarget, newLevel, 1 - direction), lookup.get(bestNum, newLevel, 1 - direction).getAddress()));
                NodeInfo otherNode = response.result;


                int common = Util.commonBits(otherNode.getNameID(), searchTarget);
                if (common > newLevel)
                    ansNode = Util.assignNode(otherNode);
            }
            return ansNode;
        } catch (StackOverflowError e) {
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Error when inserting " + searchTarget + " at address " + address);
            return null;
        }
    }

    /**
     * This method receives the numID (Hash Value) of a block and conducts a nameID
     * search on it in order to find all transactions with this nameID. First it
     * finds one node, and then goes to the linked list at last level of the skip
     * graph which contains the found node and collects all nodes that are
     * transactions from that level because they all will have the same nameID
     * according to the structure of the skip graph
     *
     * @param name nameID for which we want to collect nodes that have this name ID
     * @return list of nodes who have the given name as name ID
     */
    public List<NodeInfo> getNodesWithNameID(String name) {
        logger.debug("Gathering Node batch ...");
        try {
            // find a transaction that has the given nameID
            NodeInfo ansNode = searchByNameID(name);
            // an empty list to add transaction to it and return it
            List<NodeInfo> list = new ArrayList<>();

            if (ansNode == null || !ansNode.getNameID().equals(name)) {
                logger.debug("getNodesWithNameID: No Node was found with the given nameID");
                return list;
            }

            list.add(ansNode);

            // leftNode and rightNode will store the nodes we are visiting in left
            // and right respectively
            NodeInfo leftNode, rightNode;
            // leftNum and rightNum will store numIDs of left and right nodes, used to
            // correctly access nodes (data nodes functionality)
            int leftNumID = Const.UNASSIGNED_INT, rightNumID = Const.UNASSIGNED_INT;
            // thisRMI is just used to extract information of neighbors

            int ansNodeNumID = ansNode.getNumID();

            // get addresses of left and right nodes, as well as their numIDs
            NodeInfoResponse response = NodeInfoResponseOf(underlay.sendMessage(new GetLeftNodeRequest(maxLevels, ansNodeNumID), ansNode.getAddress()));
            leftNode = response.result;

            if (leftNode != null) {
                IntegerResponse integerResponse = IntegerResponseOf(underlay.sendMessage(new GetLeftNumIDRequest(maxLevels, ansNodeNumID), ansNode.getAddress()));
                leftNumID = integerResponse.result;
            }

            NodeInfoResponse rightNodeResponse = NodeInfoResponseOf(underlay.sendMessage(new GetRightNodeRequest(maxLevels, ansNodeNumID), ansNode.getAddress()));
            rightNode = rightNodeResponse.result;

            if (rightNode != null) {
                IntegerResponse rightNumIDResponse = IntegerResponseOf(underlay.sendMessage(new GetRightNumIDRequest(maxLevels, ansNodeNumID), ansNode.getAddress()));
                rightNumID = rightNumIDResponse.result;
            }


            // now in the last level of the skip graph, we go left and right
            while (leftNode != null) {
                String addr = leftNode.getAddress();
                NodeInfoResponse curNodeResponse = NodeInfoResponseOf(underlay.sendMessage(new GetNodeRequest(leftNumID), addr));
                NodeInfo curNode = curNodeResponse.result;
                list.add(curNode);
                NodeInfoResponse leftNodeResponse = NodeInfoResponseOf(underlay.sendMessage(new GetLeftNodeRequest(lookup.getMaxLevels(), leftNumID), addr));
                leftNode = leftNodeResponse.result;

                if (leftNode != null) {
                    IntegerResponse leftNumIDResponse = IntegerResponseOf(underlay.sendMessage(new GetLeftNumIDRequest(lookup.getMaxLevels(), leftNumID), addr));
                    leftNumID = leftNumIDResponse.result;
                }
            }
            while (rightNode != null) {
                String addr = rightNode.getAddress();
                NodeInfoResponse curNodeResponse = NodeInfoResponseOf(underlay.sendMessage(new GetNodeRequest(rightNumID), addr));
                NodeInfo curNode = curNodeResponse.result;
                list.add(curNode);
                rightNodeResponse = NodeInfoResponseOf(underlay.sendMessage(new GetRightNodeRequest(lookup.getMaxLevels(), rightNumID), addr));
                rightNode = rightNodeResponse.result;

                if (rightNode != null) {
                    IntegerResponse rightNumIDResponse = IntegerResponseOf(underlay.sendMessage(new GetRightNumIDRequest(lookup.getMaxLevels(), rightNumID), addr));
                    rightNumID = rightNumIDResponse.result;
                }

            }
            return list;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    public void printLevel(int level) {
        List<NodeInfo> list = lookup.getLevel(level, peerNode);
        for (NodeInfo n : list) {
            Util.log(n.getNumID() + " " + n.getNameID() + " " + n.getClass());
        }
    }

    public void logLevel(int level) {
        List<NodeInfo> list = lookup.getLevel(level, peerNode);
        for (NodeInfo n : list) {
            logger.info(n.getNumID() + " " + n.getNameID() + " " + n.getClass());
        }
    }


    public void printPeerLookup() {
        lookup.printLookup(numID);
    }

    public void printLookup(int level) {
        lookup.printLookup(level);
    }

    /*
     * getters and setters for lookup table and numID and nameID
     *
     */

    public NodeInfo getPeer() {
        return peerNode;
    }

    public NodeInfo getPeerLeftNode(int level) {
        return lookup.get(numID, level, Const.LEFT);
    }

    public NodeInfo getPeerRightNode(int level) {
        return lookup.get(numID, level, Const.RIGHT);
    }

    public NodeInfo getLeftNode(int level, int num) {
        return lookup.get(num, level, Const.LEFT);
    }

    public NodeInfo getRightNode(int level, int num) {
        return lookup.get(num, level, Const.RIGHT);
    }

    public boolean setLeftNode(int num, int level, NodeInfo newNode, NodeInfo oldNode) {
        return lookup.put(num, level, Const.LEFT, Util.assignNode(newNode), oldNode);
    }

    public boolean setRightNode(int num, int level, NodeInfo newNode, NodeInfo oldNode) {
        return lookup.put(num, level, Const.RIGHT, Util.assignNode(newNode), oldNode);
    }

    public void addPeerNode(NodeInfo node) {
        peerNode = node;
        lookup.addNode(node);
    }

    public int getNumID() {
        return numID;
    }

    public String getNameID() {
        return nameID;
    }

    protected int getDataNum() {
        return lookup.size();
    }

    public String getAddress() {
        return address;
    }

    public int getPort() {
        return port;
    }

    protected void setNumID(int num) {
        numID = num;
    }

    protected void setNameID(String s) {
        nameID = s;
    }

    public int getLeftNumID(int level, int num) {
        return lookup.get(num, level, Const.LEFT).getNumID();
    }

    public int getRightNumID(int level, int num) {
        return lookup.get(num, level, Const.RIGHT).getNumID();
    }

    public String getLeftNameID(int level, int num) {
        return lookup.get(num, level, Const.LEFT).getNameID();
    }

    public String getRightNameID(int level, int num) {
        return lookup.get(num, level, Const.RIGHT).getNameID();
    }

    public NodeInfo getNode(int num) {
        return lookup.get(num);
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////
    // For Testing purposes
    protected static int configurationsLeft = 0;
    protected static ArrayList<Configuration> cnfs; // 0th = master nodeinfo

    protected static TestingLog testLog;

    /*
     * Retro ping function does not test latency using RMI calls but rather through
     * the command line
     */

    public PingLog retroPingStart(NodeInfo node, int freq) {
        PingLog lg = new PingLog(new NodeInfo(address, numID, nameID), node);

        // The commands to put in the commandline
        List<String> commands = new ArrayList<String>();
        commands.add("ping");

        // -n if windows, -c if other OS
        String osname = System.getProperty("os.name");
        if (osname.contains("Win")) {
            commands.add("-n");
        } else {
            commands.add("-c");
        }

        // The number of times to ping
        commands.add(freq + "");

        // The address of the node
        commands.add(node.getAddress().split(":")[0]);// Just the address without the port

        try {
            ProcessBuilder pb = new ProcessBuilder(commands);
            Process process = pb.start();

            BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream())); // to read the
            // output
            String cur;

            int timeIndex = -1; // The index of the token containing time, in order to not repeat many
            // calculations
            while ((cur = br.readLine()) != null) {
                System.out.println(cur);
                if (cur.length() > 5 && (cur.substring(0, 5).equalsIgnoreCase("reply")
                        || cur.substring(0, 2).equalsIgnoreCase("64"))) {
                    String[] arr = cur.split(" ");
                    if (timeIndex == -1) {
                        for (int i = 0; i < arr.length; i++) {
                            String tok = arr[i];
                            if (tok.length() > 4 && tok.substring(0, 4).equalsIgnoreCase("time")) {
                                timeIndex = i;
                                break;
                            }
                        }
                    }
                    lg.Log(parseTime(arr[timeIndex]));
                }
            }
        } catch (IOException e) {
            System.out.println("Exception in retro pinging");
            e.printStackTrace();
            return lg;
        }
        return lg;
    }

    private double parseTime(String st) { // Gets the time from the string
        st = st.replace("time=", "");
        st = st.replace("time<", "");
        st = st.replace("ms", "");
        return Double.parseDouble(st);
    }

    /*
     * Method which pings a specific node a freq number of times using the function
     * ping().
     */
    public PingLog pingStart(NodeInfo node, int freq) {
        PingLog lg = new PingLog(new NodeInfo(address, numID, nameID), node);
        long befTime;
        long afrTime;
        while (freq-- > 0) {
            befTime = System.currentTimeMillis();
            underlay.sendMessage(new PingRequest(), node.getAddress());
            afrTime = System.currentTimeMillis();
            lg.Log((double) afrTime - befTime);
        }
        return lg;
    }

    /*
     * Ping function. Simply returns true.
     */
    public boolean ping() {
        return true;
    }

    /*
     * Getters (For use in the remote testing)
     */

    public Configuration getConf() {
        synchronized (this) {
            if (configurationsLeft == 0 || cnfs == null) {
                return null;
            }
            System.out.println("Configuration given out. Configurations left: " + (configurationsLeft - 1));
            return cnfs.get(cnfs.size() - (configurationsLeft--));
        }
    }

    public void logAllAndShutDown() {
        File logFile = new File(System.getProperty("user.dir") + File.separator + "logs" + File.separator + "CrashLogs"
                + File.separator + "Log_" + numID + ".txt");
        logFile.getParentFile().mkdirs();
        try {
            NodeInfo curNode = null;
            ArrayList<NodeInfo> nodeList = new ArrayList<NodeInfo>();
            curNode = searchByNumID(0);
            System.out.println();
            while (curNode != null) {
                nodeList.add(curNode);
                NodeInfoResponse response = NodeInfoResponseOf(underlay.sendMessage(new GetRightNodeRequest(0, curNode.getNumID()), curNode.getAddress()));
                curNode = response.result;
            }
            System.out.println("Total number of nodes: " + nodeList.size());
            Configuration.generateConfigFile(nodeList);
            PrintWriter writer = new PrintWriter(logFile);
            writer.println(lookup.keySet());
            for (Integer cur : lookup.keySet()) {
                writer.println(lookup.get(cur).debugString());
                System.out.println("\n");
                for (int i = lookup.getMaxLevels(); i >= 0; i--) {
                    if (lookup.get(cur, i, Const.LEFT) == null)
                        writer.print("null\t");
                    else {
                        NodeInfo lNode = lookup.get(cur, i, Const.LEFT);
                        writer.print(lNode.getAddress() + " " + lNode.getNumID() + " " + lNode.getNameID() + "\t");
                    }
                    if (lookup.get(cur, i, Const.RIGHT) == null)
                        writer.print("null\t");
                    else {
                        NodeInfo rNode = lookup.get(cur, i, Const.RIGHT);
                        writer.print(rNode.getAddress() + " " + rNode.getNumID() + " " + rNode.getNameID() + "\t");
                    }
                    writer.println("\n\n");
                }
            }
            writer.close();
            System.exit(0);
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

}