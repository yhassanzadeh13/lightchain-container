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

    /**
     * This is the exact same method as in RMIUnderlay. I believe it would be better to refactor it as the
     * method of the generic Underlay, as I think all implementations of the Underlay will use the same method.
     */
    public GenericResponse answer(GenericRequest req) throws FileNotFoundException {
        switch (req.type) {
            case PingRequest:
            {
                skipNode.ping();
                return new EmptyResponse();
            }
            case SetLeftNodeRequest:
            {
                SetLeftNodeRequest r = (SetLeftNodeRequest) req;
                return new BooleanResponse(skipNode.setLeftNode(r.num, r.level, r.newNode, r.oldNode));
            }
            case SetRightNodeRequest:
            {
                SetRightNodeRequest r = (SetRightNodeRequest) req;
                return new BooleanResponse(skipNode.setRightNode(r.num, r.level, r.newNode, r.oldNode));
            }
            case SearchByNumIDRequest:
            {
                SearchByNumIDRequest r = (SearchByNumIDRequest) req;
                NodeInfo result = skipNode.searchByNumID(r.num);
                return new NodeInfoResponse(result);
            }
            case SearchByNameIDRequest:
            {
                SearchByNameIDRequest r = (SearchByNameIDRequest) req;
                NodeInfo result = skipNode.searchByNameID(r.targetString);
                return new NodeInfoResponse(result);
            }
            case GetRightNodeRequest:
            {
                GetRightNodeRequest r = (GetRightNodeRequest) req;
                NodeInfo result = skipNode.getRightNode(r.level, r.num);
                return new NodeInfoResponse(result);
            }
            case GetLeftNodeRequest:
            {
                GetLeftNodeRequest r = (GetLeftNodeRequest) req;
                NodeInfo result = skipNode.getLeftNode(r.level, r.num);
                return new NodeInfoResponse(result);
            }
            case GetNumIDRequest:
            {
                return new IntegerResponse(skipNode.getNumID());
            }
            case InsertSearchRequest:
            {
                InsertSearchRequest r = (InsertSearchRequest) req;
                NodeInfo result = skipNode.insertSearch(r.level, r.direction, r.num, r.target);
                return new NodeInfoResponse(result);
            }
            case SearchNumIDRequest:
            {
                SearchNumIDRequest r = (SearchNumIDRequest) req;
                return new NodeInfoListResponse(
                        skipNode.searchNumID(r.numID, r.searchTarget, r.level, r.lst));
            }
            case SearchNameRequest:
            {
                SearchNameRequest r = (SearchNameRequest) req;
                return new NodeInfoResponse(
                        skipNode.searchName(r.numID, r.searchTarget, r.level, r.direction));
            }
            case GetRightNumIDRequest:
            {
                GetRightNumIDRequest r = (GetRightNumIDRequest) req;
                return new IntegerResponse(skipNode.getRightNumID(r.level, r.num));
            }
            case GetLeftNumIDRequest:
            {
                GetLeftNumIDRequest r = (GetLeftNumIDRequest) req;
                return new IntegerResponse(skipNode.getLeftNumID(r.level, r.num));
            }
            case GetNodeRequest:
            {
                GetNodeRequest r = (GetNodeRequest) req;
                return new NodeInfoResponse(skipNode.getNode(r.num));
            }
            case RemoveFlagNodeRequest:
            {
                lightChainNode.removeFlagNode();
                return new EmptyResponse();
            }
            case PoVRequest:
            {
                PoVRequest r = (PoVRequest) req;
                // PoV Request is either with a block or a transaction
                if (r.blk != null) {
                    return new SignatureResponse(lightChainNode.PoV(r.blk));
                }
                return new SignatureResponse(lightChainNode.PoV(r.t));
            }
            case GetPublicKeyRequest:
            {
                return new PublicKeyResponse(lightChainNode.getPublicKey());
            }
            case GetModeRequest:
            {
                return new BooleanResponse(lightChainNode.getMode());
            }
            case GetTokenRequest:
            {
                return new IntegerResponse(lightChainNode.getToken());
            }

            default:
                return null;
        }
    }

}
