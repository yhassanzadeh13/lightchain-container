package fixture;

import blockchain.Transaction;
import signature.SignedBytes;
import skipGraph.NodeInfo;

import java.util.Random;
import java.util.UUID;

/** A class to randomly generate objects for tests */
public class Fixtures {
  static Random r = new Random();
  static int socket = 7000;

  public static String randomString() {
    char[] chars = "abcdefghijklmnopqrstuvwxyz".toCharArray();
    StringBuilder sb = new StringBuilder(10);
    for (int i = 0; i < 10; i++) {
      char c = chars[r.nextInt(chars.length)];
      sb.append(c);
    }
    return sb.toString();
  }

  public static String IPAddressFixture() {
    return r.nextInt(256) + "." + r.nextInt(256) + "." + r.nextInt(256) + "." + r.nextInt(256);
  }

  public static NodeInfo NodeInfoFixture() {
    String IP = IPAddressFixture();
    int numID = r.nextInt(32);
    String nameID = r.nextInt(32) + "";
    return new NodeInfo(IP, numID, nameID);
  }

  public static SignedBytes SignedBytesFixture() {
    String randStr = UUID.randomUUID().toString();
    return new SignedBytes(randStr.getBytes());
  }

  public static Transaction TransactionFixture() {
    String IP = IPAddressFixture();
    int numID = r.nextInt(32);
    String randStr1 = randomString();
    String randStr2 = randomString();
    int levels = r.nextInt(32);
    return new Transaction(randStr1, numID, randStr2, IP, levels);
  }

  public static int PortFixture() {
    socket += 1;
    if (socket == 7020) socket = 7000;

    return socket;
  }
}
