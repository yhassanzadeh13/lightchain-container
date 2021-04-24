package underlay.responses;

import skipGraph.NodeInfo;

public class SearchResponse extends GenericResponse{
    public NodeInfo responseResult;

    public SearchResponse(NodeInfo responseResult) {
        this.responseResult = responseResult;
    }
}
