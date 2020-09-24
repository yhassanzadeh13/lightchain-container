package evm;

import org.ethereum.vm.client.Transaction;

import java.math.BigInteger;

public class TransactionMock implements Transaction {

    private boolean isCreate;
    private byte[] from;
    private byte[] to;
    private long nonce;
    private BigInteger value;
    private byte[] data;
    private long gas;
    private BigInteger gasPrice;

    /**
	 * The method is used to create a psuedo transaction when the EVM functions.
	 *
	 * @param isCreate 
	 * @param from
	 * @param to
     * @param nonce
     * @param value
	 * @param data
     * @param gas
     * @param gasPrice
	 */

    public TransactionMock(boolean isCreate, byte[] from, byte[] to, long nonce, BigInteger value, byte[] data,
                           long gas, BigInteger gasPrice) {
        this.isCreate = isCreate;
        this.from = from;
        this.to = to;
        this.nonce = nonce;
        this.value = value;
        this.data = data;
        this.gas = gas;
        this.gasPrice = gasPrice;
    }

    @Override
    public boolean isCreate() {
        return isCreate;
    }

    @Override
    public byte[] getFrom() {
        return from;
    }

    @Override
    public byte[] getTo() {
        return to;
    }

    @Override
    public long getNonce() {
        return nonce;
    }

    @Override
    public BigInteger getValue() {
        return value;
    }

    @Override
    public byte[] getData() {
        return data;
    }

    @Override
    public long getGas() {
        return gas;
    }

    @Override
    public BigInteger getGasPrice() {
        return gasPrice;
    }
}
