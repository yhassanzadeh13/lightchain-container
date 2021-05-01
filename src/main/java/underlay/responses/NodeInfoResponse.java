package underlay.responses;

import skipGraph.NodeInfo;

/**
 * Represents a response which returns a NodeInfo.
 */
public class NodeInfoResponse extends GenericResponse{
    public NodeInfo responseResult;

    public NodeInfoResponse(NodeInfo responseResult) {
        this.responseResult = responseResult;
    }
}
