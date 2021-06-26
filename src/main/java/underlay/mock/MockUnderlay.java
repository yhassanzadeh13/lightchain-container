package underlay.mock;

import skipGraph.NodeInfo;
import underlay.Underlay;
import underlay.requests.GenericRequest;
import underlay.requests.lightchain.PoVRequest;
import underlay.requests.skipgraph.*;
import underlay.responses.*;

import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;

import static underlay.rmi.RMIUnderlay.processRequest;

/**
 * To test the application layer of LightChain, we need to mock out the RMI underlay implementation.
 * We just keep a static map of adresses to MockUnderlays and directly call the mockUnderlays, without using
 * RMI primitives
 */
public class MockUnderlay extends Underlay {
    private static Map<String, MockUnderlay> inventory = new HashMap<>();

    public MockUnderlay(String myAddress){
        inventory.put(myAddress, this);
    }


    @Override
    public GenericResponse sendMessage(GenericRequest req, String targetAddress) {
        try {
            return inventory.get(targetAddress).answer(req);
        } catch (Exception e) {
            System.out.println("target address not found");
        }
        return null;
    }

    @Override
    public boolean terminate() {
        return true;
    }

    public GenericResponse answer(GenericRequest req) throws FileNotFoundException {
        return processRequest(this.skipNode, this.lightChainNode, req);
    }

}
