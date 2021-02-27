package evm;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import org.bouncycastle.util.encoders.Hex;
import org.ethereum.vm.DataWord;
import org.ethereum.vm.client.*;
import org.ethereum.vm.util.ByteArrayUtil;
import org.ethereum.vm.util.HashUtil;
import org.ethereum.vm.util.HexUtil;

import static org.ethereum.vm.util.ByteArrayUtil.merge;


public class ContractTransaction extends vmbase {



    public final BigInteger premine = BigInteger.valueOf(1L).multiply(Unit.ETH); // each account has 1 ether
    public Transaction transaction;
    public Block block;


    //@Before
    public void setup() {
        super.setup();
        transaction = new TransactionMock(false, caller, address, 0, value, data, gas, gasPrice);
        block = new BlockMock(number, prevHash, coinbase, timestamp, gasLimit);
        repository.addBalance(origin, premine);
        repository.addBalance(caller, premine);
        repository.addBalance(address, premine);
        repository.addBalance(coinbase, premine);
    }

    public byte[] createContract(String contractLocation, byte[] address, long nonce, long gas) throws IOException {
        return createContract(readContract(contractLocation), new byte[0], address, nonce, gas);
    }

    // This function reads a contract by taking the name of contract

    public byte[] readContract(String fileName) throws IOException {
        String path3 = fileName;
        //System.out.println("path: "+path3);
        List<String> lines = Files.readAllLines(Paths.get(path3), StandardCharsets.UTF_8);
        return HexUtil.fromHexString(lines.get(0));
    }

    // This function creates a contract and also returns the address of created contract
    public byte[] createContract(byte[] code, byte[] args, byte[] address, long nonce, long gas)  {
        byte[] data = merge(code, args);
        byte[] contractAddress = HashUtil.calcNewAddress(address, nonce);
        Transaction transaction = new TransactionMock(true, address, contractAddress, nonce, value, data, gas, gasPrice);
        TransactionExecutor executor = new TransactionExecutor(transaction, block, repository, blockStore);
        TransactionReceipt receipt = executor.run();
        System.out.println("\n inner contract address "+ receipt);

        return contractAddress;
    }

    //Wrapper function for the smart contract function code
    // This function is used to interact EVM with Lightchain
        public boolean TransctSol(int token, String contractloc,String functname) throws IOException {
                long nonce = 0;
                long nonce1 = 1;
                BigInteger toSend = new BigInteger(String.valueOf(token));
                byte[] contractAddress1 = createContract(contractloc, origin, nonce, gas);
                repository.addBalance(contractAddress1, premine);

                byte[] method = HashUtil.keccak256(functname.getBytes(StandardCharsets.UTF_8));
                byte[] data = ByteArrayUtil.merge(Arrays.copyOf(method, 4), DataWord.of(toSend).getData());

                Transaction transaction = new TransactionMock(false, caller, contractAddress1, nonce1, value, data, gas, gasPrice);

                TransactionExecutor executor = new TransactionExecutor(transaction, block, repository, blockStore);
                TransactionReceipt receipt = executor.run();
                int res = Integer.parseInt(Hex.toHexString(receipt.getReturnData()));
                System.out.println("\n"+res+"\n");
            return res == 1; // return true or false
        }
     
}

