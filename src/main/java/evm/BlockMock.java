package evm;

import org.ethereum.vm.client.Block;
import java.math.BigInteger;

public class BlockMock implements Block {

    private long number;
    private byte[] parentHash;
    private byte[] coinbase;
    private long timestamp;
    private long gasLimit;

   /**
	 * The method is used to form a block when the EVM functions.
	 *
	 * @param number block's serial number
	 * @param parentHash the hash of the predecessor block
	 * @param timestamp time in seconds at the time of block formation
     * @param gasLimit determines how many transactions can fit in a block
	 */
    public BlockMock(long number, byte[] parentHash, byte[] coinbase, long timestamp, long gasLimit) {
        this.gasLimit = gasLimit;
        this.parentHash = parentHash;
        this.coinbase = coinbase;
        this.timestamp = timestamp;
        this.number = number;
    }

    @Override
    public long getGasLimit() {
        return gasLimit;
    }

    @Override
    public byte[] getParentHash() {
        return parentHash;
    }

    @Override
    public byte[] getCoinbase() {
        return coinbase;
    }

    @Override
    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public long getNumber() {
        return number;
    }

    @Override
    public BigInteger getDifficulty() {
        return BigInteger.ONE;
    }
}
