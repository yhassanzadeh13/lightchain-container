package skipGraph;

import blockchain.Block;
import blockchain.Transaction;
import localnet.LocalSkipGraph;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import util.Const;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

public class SkipGraphTest {

    private static int port = 7000;
    private int maxLevels = 5;
    private int numID1 = 1;
    private int numID2 = 2;
    private int numID3 = 3;
    private String nameID1 = "011";
    private String nameID2 = "001";
    private String nameID3 = "100";
    private NodeConfig initialConfig;
    private NodeConfig config1;
    private NodeConfig config2;


    @BeforeEach
    public void init() {
        initialConfig = new NodeConfig(maxLevels, port++, numID2, nameID2);
        config1 = new NodeConfig(maxLevels, port++, numID1, nameID1);
        config2 = new NodeConfig(maxLevels, port++, numID3, nameID3);
    }

    @Test
    void oneNodeSequentialBlocksInsertion() {
        try {
            SkipNode node = new SkipNode(initialConfig, Const.DUMMY_INTRODUCER, true);
            List<NodeInfo> list = new ArrayList<>();
            list.add(node.getPeer());
            LocalSkipGraph localGraph = new LocalSkipGraph(list, maxLevels);


            //create and insert genesis
            StringBuilder st = new StringBuilder();
            for (int i = 0; i < maxLevels; i++)
                st.append("0");
            String prev = st.toString();
            int index = 0;

            Block genesis = new Block(prev, node.getNumID(), node.getAddress(), index, maxLevels);
            localGraph.insertNode(genesis);
            node.insertNode(genesis);

            //insert 100 blocks
            Block latestBlock = genesis;
            for(int i = 0; i< 100; i++){

                NodeInfo tmpCorrect = localGraph.searchByNumID(latestBlock.getNumID());
                NodeInfo tmpTest = node.searchByNumID(latestBlock.getNumID());

                assert(tmpCorrect instanceof Block && tmpTest instanceof Block && tmpTest.getNameID().equals(tmpCorrect.getNameID()));
                Block blk = (Block) tmpCorrect;
                Block newBlk = new Block(blk.getHash(), node.getNumID(), node.getAddress(), new ArrayList<>(), blk.getIndex() + 1, maxLevels);
                localGraph.insertNode(newBlk);
                node.insertNode(newBlk);
                latestBlock = newBlk;
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
    //@Test
    void twoNodesSequentialBlocksInsertion() {
        try {
            SkipNode node1 = new SkipNode(initialConfig, Const.DUMMY_INTRODUCER, true);
            SkipNode node2 = new SkipNode(config1, node1.getAddress(), false);
            List<NodeInfo> list = new ArrayList<>();

            list.add(node1.getPeer());
            list.add(node2.getPeer());

            LocalSkipGraph localGraph = new LocalSkipGraph(list, maxLevels);

            //create and insert genesis
            StringBuilder st = new StringBuilder();
            for (int i = 0; i < maxLevels; i++)
                st.append("0");

            String prev = st.toString();
            int index = 0;
            Block genesis = new Block(prev, node1.getNumID(), node1.getAddress(), index, maxLevels);
            localGraph.insertNode(genesis);

            node1.insertNode(genesis);

            //insert 200 blocks
            Block latestBlock = genesis;
            for(int i = 0; i< 100; i++){
                //let node1 insert a block
                NodeInfo tmpCorrect = localGraph.searchByNumID(latestBlock.getNumID());
                NodeInfo tmpTest = node1.searchByNumID(latestBlock.getNumID());
                assert(tmpCorrect instanceof Block && tmpTest instanceof Block && tmpCorrect.getNameID().equals(tmpTest.getNameID()));
                Block blk = (Block) tmpCorrect;
                Block newBlk = new Block(blk.getHash(), node1.getNumID(), node1.getAddress(), new ArrayList<>(), blk.getIndex() + 1, maxLevels);
                localGraph.insertNode(newBlk);
                node1.insertNode(newBlk);

                latestBlock = newBlk;

                tmpCorrect = localGraph.searchByNumID(latestBlock.getNumID());
                tmpTest = node2.searchByNumID(latestBlock.getNumID());
                assert(tmpCorrect instanceof Block && tmpTest instanceof Block && tmpCorrect.getNameID().equals(tmpTest.getNameID()));
                blk = (Block) tmpCorrect;
                newBlk = new Block(blk.getHash(), node2.getNumID(), node2.getAddress(), new ArrayList<>(), blk.getIndex() + 1, maxLevels);
                localGraph.insertNode(newBlk);
                node2.insertNode(newBlk);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    void twoNodesConcurrentBlocksInsertion() {

//        try {
//            SkipNode node1 = new SkipNode(initialConfig, Const.DUMMY_INTRODUCER, true);
//            SkipNode node2 = new SkipNode(config1, node1.getAddress(), false);
//
//            List<NodeInfo> list = new ArrayList<NodeInfo>();
//            list.add(node1.peerNode);
//            list.add(node2.peerNode);
//            LocalSkipGraph localGraph = new LocalSkipGraph(list, maxLevels);
//
//            //create and insert genesis
//            StringBuilder st = new StringBuilder();
//            for (int i = 0; i < maxLevels; i++)
//                st.append("0");
//            String prev = st.toString();
//            int index = 0;
//            Block genesis = new Block(prev, node1.getNumID(), node1.getAddress(), index, maxLevels);
//            localGraph.insertNode(genesis);
//
//            //insert 200 blocks
//            Block latestBlock = genesis;
//            for(int i = 0; i< 100; i++){
//                //let node1 insert a block
//                NodeInfo tmp = localGraph.searchByNumID(latestBlock.getNameID());
//                assert(tmp instanceof Block && tmp.getNameID().equals(latestBlock.getNameID()));
//                Block blk = (Block) tmp;
//                Block newBlk = new Block(blk.getHash(), node1.getNumID(), node1.getAddress(), new ArrayList<>(), blk.getIndex() + 1, maxLevels);
//                localGraph.insertNode(newBlk);
//                latestBlock = newBlk;
//
//                //let node2 insert a block
//                tmp = localGraph.searchByNumID(latestBlock.getNameID());
//                assert(tmp instanceof Block && tmp.getNameID().equals(latestBlock.getNameID()));
//                blk = (Block) tmp;
//                newBlk = new Block(blk.getHash(), node2.getNumID(), node2.getAddress(), new ArrayList<>(), blk.getIndex() + 1, maxLevels);
//                localGraph.insertNode(newBlk);
//                latestBlock = newBlk;
//            }
//        } catch (RemoteException e) {
//            e.printStackTrace();
//        }
    }






}
