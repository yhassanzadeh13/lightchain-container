package underlay.rmi;

import org.apache.log4j.Logger;
import skipGraph.NodeInfo;
import underlay.Underlay;
import underlay.requests.GenericRequest;
import underlay.requests.lightchain.GenericLightChainRequest;
import underlay.requests.lightchain.PoVRequest;
import underlay.requests.skipgraph.*;
import underlay.responses.*;
import util.Util;

import java.io.FileNotFoundException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;

/**
 * The underlay class is used to abstract away the RMI primitives used in the skipGraph and
 * lightchain nodes.
 */
public class RMIUnderlay extends Underlay {

  private String IP;
  private int port;
  private String address;
  JavaRMIHost host;

  private final Logger logger = Logger.getLogger("" + port);

  public RMIUnderlay(int port) {
    this.IP = Util.grabIP();
    this.port = port;
    this.address = IP + ":" + port;
    try {
      initRMI();
      host = new JavaRMIHost(this);
      LocateRegistry.createRegistry(port).rebind("RMIImpl", host);
    } catch (Exception e) {
      System.err.println("[RMIUnderlay] Error while initializing at port " + port);
      e.printStackTrace();
    }
    logger.info("Rebinding Successful");
  }

  public GenericResponse sendMessage(GenericRequest req, String targetAddress){
    RMIService remote = getRMI(targetAddress);
    if (remote == null)
      return null;
    try {
      return remote.answer(req);
    } catch (Exception e) {
      System.err.println("[JavaRMIUnderlay] Could not send the message.");
      e.printStackTrace();
      return null;
    }
  }

  public GenericResponse answer(GenericRequest req) throws RemoteException, FileNotFoundException {
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


  /**
   * This method returns the underlying RMI of the given address based on the type.
   *
   * @param adrs A string which specifies the address of the target node.
   */
  public RMIService getRMI(String adrs) {
    if (!Util.validateIP(adrs)) {
      logger.debug("Error in lookup up RMI. Address " + adrs + " is not a valid address");
    }
    RMIService remote;

    try {
      remote = (RMIService) Naming.lookup("//" + adrs + "/RMIImpl");
    } catch (Exception e) {
      System.err.println("[JavaRMIUnderlay] Could not connect to the remote RMI server!");
      return null;
    }
    return remote;
  }

  /** This method initializes all the RMI system properties required for proper functionality */
  protected void initRMI() {
    try {
      System.setProperty("java.rmi.server.hostname", IP);
      System.setProperty("java.rmi.server.useLocalHostname", "false");
      System.out.println("RMI Server proptery set. Inet4Address: " + IP + ":" + port);
    } catch (Exception e) {
      System.err.println(e);
      System.err.println("Exception in initialization. Please try running the program again.");
      System.exit(0);
    }
  }

  /** Terminates the Java RMI underlay service. */
  public boolean terminate() {
    try {
      Naming.unbind("//" + address + "/RMIImpl");
    } catch (Exception e) {
      System.err.println("[JavaRMIUnderlay] Could not terminate.");
      e.printStackTrace();
      return false;
    }
    return true;
  }
}
