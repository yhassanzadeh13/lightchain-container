package underlay.responses;

import skipGraph.NodeInfo;

import java.util.List;

/** Represents a response which returns a NodeInfo. */
public class NodeInfoResponse extends GenericResponse {
  public final NodeInfo result;

  public NodeInfoResponse(NodeInfo responseResult) {
    this.result = responseResult;
  }

  public static NodeInfoResponse NodeInfoResponseOf(GenericResponse response) {
    return (NodeInfoResponse) response;
  }

}
