package underlay;

import skipGraph.LookupTable;
import skipGraph.NodeInfo;
import underlay.requests.*;
import underlay.responses.GenericResponse;
import underlay.responses.SearchResponse;
import util.Const;
import util.Util;
import skipGraph.RMIInterface;

import java.rmi.Naming;
import java.rmi.RemoteException;
import org.apache.log4j.Logger;


public class Underlay {

    protected String address;
    protected String IP;
    private Logger logger;
    private String RMIPort;

    public GenericResponse sendMessage(GenericRequest req, String targetAddress) throws RemoteException {
        RMIInterface targetRMI = getRMI(targetAddress);
        switch(req.type){
            case SetLeftNodeRequest: {
                SetLeftNodeRequest r = (SetLeftNodeRequest) req;
                targetRMI.setLeftNode(r.num, r.level, r.newNode, r.oldNode);
                return null;               /// what should I return here?
            }
            case SetRightNodeRequest: {
                SetRightNodeRequest r = (SetRightNodeRequest) req;
                targetRMI.setLeftNode(r.num, r.level, r.newNode, r.oldNode);
                return null;
            }
            case SearchByNumIDRequest: {

            }
            case SearchByNameIDRequest: {
                SearchByNameIDRequest r = (SearchByNameIDRequest) req;
                NodeInfo rightResult = targetRMI.searchByNameID(r.targetString);
                return new SearchResponse(rightResult);
            }
            default:
                return null;

        }
    }

    public RMIInterface getRMI(String adrs) {

        if (!Util.validateIP(adrs)) {
            logger.debug("Error in lookup up RMI. Address " + adrs + " is not a valid address");
            return null;
        }

        try {
            return (RMIInterface) Naming.lookup("//" + adrs + "/RMIImpl");

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }


    /**
     * This method initializes all the RMI system properties required for proper
     * functionality
     */
    protected void initRMI() {
        this.IP = Util.grabIP();
        try {
            System.setProperty("java.rmi.server.hostname", IP);
            System.setProperty("java.rmi.server.useLocalHostname", "false");
            System.out.println("RMI Server proptery set. Inet4Address: " + IP + ":" + RMIPort);
        } catch (Exception e) {
            System.err.println("Exception in initialization. Please try running the program again.");
            System.exit(0);
        }
    }


}
