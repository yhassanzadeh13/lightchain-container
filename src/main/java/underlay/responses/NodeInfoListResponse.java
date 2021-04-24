package underlay.responses;

import skipGraph.NodeInfo;

import java.util.List;

public class NodeInfoListResponse extends GenericResponse {
    public List<NodeInfo> responseResultList;

    public NodeInfoListResponse(List<NodeInfo> responseResultList) {
        this.responseResultList = responseResultList;
    }
}
