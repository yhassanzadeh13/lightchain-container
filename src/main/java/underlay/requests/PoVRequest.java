package underlay.requests;

import blockchain.Block;
import blockchain.Transaction;

public class PoVRequest extends GenericRequest{
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
