package evm;

import org.ethereum.vm.DataWord;
//import main.java.evm.BlockStoreMock;
import org.ethereum.vm.client.BlockStore;
import org.ethereum.vm.client.Repository;
//import main.java.evm.RepositoryMock;

//import org.ethereum.vm.program.Program;
import org.ethereum.vm.program.invoke.ProgramInvokeImpl;
//import org.ethereum.vm.util.HexUtil;
//import org.junit.Before;

import java.math.BigInteger;
import java.util.Arrays;

public class vmbase {
    public final byte[] address = address(1);
    public final byte[] origin = address(2);
    public final byte[] caller = address(2);
    public final long gas = 1_000_000L;
    public final BigInteger gasPrice = BigInteger.ONE;
    public final BigInteger value = BigInteger.ZERO;
    public final byte[] data = new byte[0];

    public final byte[] prevHash = new byte[32];
    public final byte[] coinbase = address(3);
    public final long timestamp = System.currentTimeMillis();
    public final long number = 1;
    public final BigInteger difficulty = BigInteger.TEN;
    public final long gasLimit = 10_000_000L;

    public final int callDepth = 0;
    public final boolean isStaticCall = false;

    public Repository repository;
    public BlockStore blockStore;

    public Repository track;
    public Repository originalTrack;

    public ProgramInvokeImpl invoke;


//@Before
    public void setup() {
        this.repository = new RepositoryMock();
        this.blockStore = new BlockStoreMock();

        this.track = repository.startTracking();
        this.originalTrack = track.clone();

        this.invoke = new ProgramInvokeImpl(
                DataWord.of(address),
                DataWord.of(origin),
                DataWord.of(caller),
                gas,
                DataWord.of(gasPrice),
                DataWord.of(value),
                data,
                DataWord.of(prevHash),
                DataWord.of(coinbase),
                DataWord.of(timestamp),
                DataWord.of(number),
                DataWord.of(difficulty),
                DataWord.of(gasLimit),
                track,
                originalTrack,
                blockStore,
                callDepth,
                isStaticCall);
    }

    public byte[] address(int n) {
        byte[] a = new byte[20];
        Arrays.fill(a, (byte) n);
        return a;
    }

}
