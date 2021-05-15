package underlay.requests.lightchain;

import blockchain.Block;
import blockchain.Transaction;
import underlay.requests.RequestType;

public class PoVRequest extends GenericLightChainRequest {
  public Block blk;
  public Transaction t;

  public PoVRequest(Block blk) {
    super(RequestType.PoVRequest);
    this.blk = blk;
  }

  public PoVRequest(Transaction t) {
    super(RequestType.PoVRequest);
    this.t = t;
  }
}
