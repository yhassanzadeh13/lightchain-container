package underlay;

import blockchain.LightChainNode;
import blockchain.Parameters;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import underlay.mock.MockUnderlay;
import underlay.requests.lightchain.GetModeRequest;
import underlay.requests.lightchain.GetPublicKeyRequest;
import underlay.requests.skipgraph.PingRequest;
import underlay.responses.EmptyResponse;
import util.Const;
import util.Util;
import static org.junit.jupiter.api.Assertions.*;
import static underlay.responses.BooleanResponse.BooleanResponseOf;
import static underlay.responses.PublicKeyResponse.PublicKeyResponseOf;

/**
 * To test the application layer of LightChain, we need to mock out the RMI underlay implementation.
 */
public class MockUnderlayTest {
    private static MockUnderlay mockUnderlay1;
    private static MockUnderlay mockUnderlay2;
    static LightChainNode node1;
    static LightChainNode node2;
    private static Parameters params;
    private static int port1 = 7050;
    private static int port2 = 7051;
    private static String IP = Util.grabIP();
    private static String address1 = IP + ":" + port1;
    private static String address2 = IP + ":" + port2;


    @BeforeAll
    static void init() {
        params = new Parameters();
        mockUnderlay1 = new MockUnderlay(address1);
        node1 = new LightChainNode(params, port1, Const.DUMMY_INTRODUCER, true, mockUnderlay1);
        mockUnderlay2 = new MockUnderlay(address2);
        node2 = new LightChainNode(params, port2, node1.getAddress(), false, mockUnderlay2);
    }


    /**
     * When both underlays A and B are up and ready, their exchanged messages are delivered to each other intact.
     * We test different messages of lightchain to make sure all of them are sent correctly
     * We compare the results with the actual values returned by the nodes themselves
     */
    @Test
    void sendMessageLightChainTest() {
        assertEquals(node2.getMode(), (BooleanResponseOf(mockUnderlay1.sendMessage(new GetModeRequest(), address2)).result), "not equal");
        assertEquals(node1.getMode(), (BooleanResponseOf(mockUnderlay2.sendMessage(new GetModeRequest(), address1)).result), "not equal");

        assertEquals(node2.getPublicKey(), (PublicKeyResponseOf(mockUnderlay1.sendMessage(new GetPublicKeyRequest(), address2)).result), "not equal");
        assertEquals(node1.getPublicKey(), (PublicKeyResponseOf(mockUnderlay2.sendMessage(new GetPublicKeyRequest(), address1)).result), "not equal");

        assertEquals(mockUnderlay1.sendMessage(new PingRequest(), address2).getClass(), EmptyResponse.class, "not equal");
    }

    /**
     * When both underlays A and B are up and ready, their exchanged messages are delivered to each other intact.
     * We test different messages of skipNode to make sure all of them are sent correctly
     * We compare the results with the actual values returned by the nodes themselves
     */
    @Test
    void sendMessageSkipNodeTest(){
        assertEquals(node2.getPeer(), node1.searchByNumID(node2.getNumID()), "node 2 not found");
        assertEquals(node1.getPeer(), node2.searchByNumID(node1.getNumID()), "node 1 not found");

        assertEquals(node2.getPeer(), node1.searchByNameID(node2.getNameID()), "node 2 not found");
        assertEquals(node1.getPeer(), node2.searchByNameID(node1.getNameID()), "node 1 not found");
    }





}
