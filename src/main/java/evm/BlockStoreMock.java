package evm;

import org.ethereum.vm.client.BlockStore;

    /**
    * The method is used to store blocks when the EVM functions.
    * This function gives the requested block's hash value.
    */
    public class BlockStoreMock implements BlockStore {
        
        @Override
        public byte[] getBlockHashByNumber(long index) {
            return new byte[32];
        }
    }
