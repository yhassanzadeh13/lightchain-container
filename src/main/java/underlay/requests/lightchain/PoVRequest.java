package underlay.requests.lightchain;

import blockchain.Block;
import blockchain.Transaction;
import underlay.requests.RequestType;

public class PoVRequest extends GenericLightChainRequest {
  public final Block blk;
  public final Transaction t;

  public PoVRequest(Block blk) {
    super(RequestType.PoVRequest);
    this.blk = blk;
    this.t = null;
  }

  public PoVRequest(Transaction t) {
    super(RequestType.PoVRequest);
    this.t = t;
    this.blk = null;
  }
}
