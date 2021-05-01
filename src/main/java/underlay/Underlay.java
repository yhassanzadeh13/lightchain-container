package underlay;

import skipGraph.NodeInfo;
import underlay.requests.*;
import underlay.responses.*;
import util.Util;
import skipGraph.RMIInterface;

import java.io.FileNotFoundException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import org.apache.log4j.Logger;

/**
 * The underlay class is used to abstract away the RMI primitives used in the skipGraph and lightchain nodes.
 * */

public class Underlay {

    /**
     * The underlying RMIInterface instance.
     */
    private RMIInterface targetRMI;
    private Logger logger = Logger.getLogger("");

    public GenericResponse sendMessage(GenericRequest req, String targetAddress) throws RemoteException, FileNotFoundException {
        getRMI(targetAddress);
        switch(req.type){
            case SetLeftNodeRequest: {
                SetLeftNodeRequest r = (SetLeftNodeRequest) req;
                return new BooleanResponse(targetRMI.setLeftNode(r.num, r.level, r.newNode, r.oldNode));
            }
            case SetRightNodeRequest: {
                SetRightNodeRequest r = (SetRightNodeRequest) req;
                return new BooleanResponse(targetRMI.setRightNode(r.num, r.level, r.newNode, r.oldNode));
            }
            case SearchByNumIDRequest: {
                SearchByNumIDRequest r = (SearchByNumIDRequest) req;
                NodeInfo result =  targetRMI.searchByNumID(r.num);
                return new NodeInfoResponse(result);
            }
            case SearchByNameIDRequest: {
                SearchByNameIDRequest r = (SearchByNameIDRequest) req;
                NodeInfo result = targetRMI.searchByNameID(r.targetString);
                return new NodeInfoResponse(result);
            }
            case GetRightNodeRequest: {
                GetRightNodeRequest r = (GetRightNodeRequest) req;
                NodeInfo result = targetRMI.getRightNode(r.level, r.num);
                return new NodeInfoResponse(result);
            }
            case GetLeftNodeRequest: {
                GetLeftNodeRequest r = (GetLeftNodeRequest) req;
                NodeInfo result = targetRMI.getLeftNode(r.level, r.num);
                return new NodeInfoResponse(result);
            }
            case GetNumIDRequest: {
                return new NumIDResponse(targetRMI.getNumID());
            }
            case InsertSearchRequest: {
                InsertSearchRequest r = (InsertSearchRequest) req;
                NodeInfo result = targetRMI.insertSearch(r.level, r.direction, r.num, r.target);
                return new NodeInfoResponse(result);
            }
            case SearchNumIDRequest: {
                SearchNumIDRequest r =  (SearchNumIDRequest) req;
                return new NodeInfoListResponse(targetRMI.searchNumID(r.numID, r.searchTarget, r.level, r.lst));
            }
            case SearchNameRequest: {
                SearchNameRequest r =  (SearchNameRequest) req;
                return new NodeInfoResponse(targetRMI.searchName(r.numID, r.searchTarget, r.level, r.direction));
            }
            case GetRightNumIDRequest: {
                GetRightNumIDRequest r = (GetRightNumIDRequest) req;
                return new NumIDResponse(targetRMI.getRightNumID(r.level, r.num));
            }
            case GetLeftNumIDRequest: {
                GetLeftNumIDRequest r = (GetLeftNumIDRequest) req;
                return new NumIDResponse(targetRMI.getLeftNumID(r.level, r.num));
            }
            case GetNodeRequest: {
                GetNodeRequest r = (GetNodeRequest) req;
                return new NodeInfoResponse(targetRMI.getNode(r.num));
            }
            default:
                return null;

        }
    }

    public void getRMI(String adrs) {
        if (!Util.validateIP(adrs)) {
            logger.debug("Error in lookup up RMI. Address " + adrs + " is not a valid address");
        }

        try {
            targetRMI = (RMIInterface) Naming.lookup("//" + adrs + "/RMIImpl");
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void setTargetRMI(RMIInterface r){
        this.targetRMI = r;
    }


}
