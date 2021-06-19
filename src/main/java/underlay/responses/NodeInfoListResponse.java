package underlay.responses;

import skipGraph.NodeInfo;

import java.util.List;

/** Represents a response which returns a List of NodeInfo's. */
public class NodeInfoListResponse extends GenericResponse {
  public final List<NodeInfo> result;

  public NodeInfoListResponse(List<NodeInfo> responseResultList) {
    this.result = responseResultList;
  }

  public static NodeInfoListResponse NodeInfoListResponseOf(GenericResponse response) {
    return (NodeInfoListResponse) response;
  }

}
