package fixture;

import blockchain.Transaction;
import signature.SignedBytes;
import skipGraph.NodeInfo;

import java.util.Random;
import java.util.UUID;

/**
 * A class to randomly generate objects for tests
 */
public class Fixtures {
    Random r = new Random();

    public String randomString(){
        char[] chars = "abcdefghijklmnopqrstuvwxyz".toCharArray();
        StringBuilder sb = new StringBuilder(10);
        for (int i = 0; i < 10; i++) {
            char c = chars[r.nextInt(chars.length)];
            sb.append(c);
        }
        return sb.toString();
    }

    public String IPAddressFixture(){
        return r.nextInt(256) + "." + r.nextInt(256) + "." + r.nextInt(256) + "." + r.nextInt(256);
    }

    public NodeInfo NodeInfoFixture(){
        String IP = IPAddressFixture();
        int numID = r.nextInt(100);
        String nameID = r.nextInt(100)+"";
        return new NodeInfo(IP, numID, nameID);
    }

    public SignedBytes SignedBytesFixture(){
        String randStr = UUID.randomUUID().toString();
        return new SignedBytes(randStr.getBytes());
    }

    public Transaction TransactionFixture(){
        String IP = IPAddressFixture();
        int numID = r.nextInt(100);
        String randStr1 = randomString();
        String randStr2 = randomString();
        int levels = r.nextInt(100);
        return new Transaction(randStr1, numID, randStr2, IP, levels);
    }

}
