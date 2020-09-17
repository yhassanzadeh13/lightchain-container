package evm;

import org.ethereum.vm.client.BlockStore;

public class BlockStoreMock implements BlockStore {

    @Override
    public byte[] getBlockHashByNumber(long index) {
        return new byte[32];
    }
}
