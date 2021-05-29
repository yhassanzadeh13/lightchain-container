package remoteTest;

import blockchain.LightChainInterface;
import simulation.SimLog;
import skipGraph.NodeInfo;
import skipGraph.SkipNodeInterface;
import underlay.rmi.RMIUnderlay;
import underlay.requests.skipgraph.GetNodeRequest;
import underlay.requests.skipgraph.GetNumIDRequest;
import underlay.requests.skipgraph.GetRightNodeRequest;
import underlay.responses.IntegerResponse;
import underlay.responses.NodeInfoResponse;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;

import static underlay.responses.IntegerResponse.IntegerResponseOf;
import static underlay.responses.NodeInfoResponse.NodeInfoResponseOf;

public class RemoteAccessTool {
  static String ip;
  static String port;
  static Scanner in = new Scanner(System.in);
  static ArrayList<NodeInfo> data;
  //	private static ArrayList<Transaction> transactions;
  static NodeInfo[][][] lookup;
  static String nameID;
  static int numID;
  static boolean skipInit = false;
  static LightChainInterface node;
  static RMIUnderlay RMIUnderlay;

  private static ArrayList<TestingLog> TestLogs;

  public static void main(String[] args) {
    while (true) {
      if (!skipInit) {
        log("Enter IP address along with port (x to exit)");
        ip = in.nextLine();
        while (ip.equals("x") || (!validateIP(ip) || ip.split(":").length != 2)) {
          if (ip.equals("x")) System.exit(0);
          log("Please enter a valid IP address with respective port");
          ip = in.nextLine();
        }
      } else skipInit = false;
      if (ip == null) continue;
      String[] ipsp = ip.split(":");
      ip = ipsp[0];
      port = ipsp[1];

      node = getRMI(ip + ":" + port);
      if (node == null) {
        log("Couldn't fetch the node. Please make sure the input is correct.");
        continue;
      }
      try {
        nameID = node.getNameID();
        numID = node.getNumID();
        // data = node.getData();
        // lookup = node.getLookupTable();
        if (data == null || lookup == null) {
          log("Couldn't fetch data and lookup properly. Please try again.");
          continue;
        }
        while (true) {
          printMenu();
          String input = get();
          if (!input.matches("[0-9]+")) {
            log("Invalid query. Please enter the number of one of the possible operations");
            continue;
          }
          int query = Integer.parseInt(input);
          if (query == 0) {
            startSim();
          } else if (query == 1) {
            log("Enter prev of transaction");
            String prev = get();
            log("Enter cont of transaction");
            String cont = get();
            // Transaction t = new Transaction(prev, numID, cont, ip + ":" + port);
            // node.put(t);
          } else if (query == 2) { // insert block
            log("Enter prev of block");
            String prev = get();
            // Block b = new Block(prev, numID, ip + ":" + port, -1);
            // node.put(b);
          } else if (query == 3) { // search by name ID
            log("Please Enter the name ID to be searched");
            String name = get();
            while (!name.matches("[0-1]+")) { // Makes sure the name is a binary string
              log("Name ID should be a binary string. Please enter a valid Name ID:");
              name = get();
            }
            NodeInfo result = null;
            try {
              result = node.searchByNameID(name);
            } catch (RemoteException e) {
              e.printStackTrace();
              log("Remote Exception in query.");
            }
            log("The result of search by name ID is: " + result.getAddress());
            if (promptSwitch(result)) break;
          } else if (query == 4) { // search by num ID
            log("Please Enter the numeric ID to be searched");
            String numInput = get();
            while (!numInput.matches("0|[1-9][0-9]*")) {
              log("Invalid number entered. Please enter a valid number");
              numInput = get();
            }
            int num = Integer.parseInt(numInput);
            List<NodeInfo> lst = new ArrayList<NodeInfo>();
            NodeInfo result = null;
            try {
              lst = node.searchByNumIDHelper(num, lst);
              result = lst.get(lst.size() - 1);
            } catch (RemoteException e) {
              e.printStackTrace();
              log("Remote Exception in query.");
            }
            log("The search path is: ");
            for (int i = 0; i < lst.size(); i++) {
              log(i + ") " + lst.get(i).getNumID());
            }
            log("The result of search by numeric ID is: " + result.getAddress());
            if (promptSwitch(result)) break;
          } else if (query == 5) { // print the lookup table of the current node
            //						log("In case you want the lookup table of the original node enter 0.");
            //						log("Otherwise, enter the index of the data node ");
            //						int num = Integer.parseInt(get());
            printLookup(0);
            //						if(num < node.getDataNum())
            //							printLookup(num);
            //						else
            //							log("Data node with given index does not exist");
          } else if (query == 6) {
            printData();
          } else if (query == 7) {
            log("This is the current lookup table: ");
            printLookup(0);
            log("Enter the number of the node you want to connect to: (invalid number to abort)");
            String st = get();
            try {
              int inp = Integer.parseInt(st);
              NodeInfo swtch = lookup[inp / 2][inp % 2][0];
              if (swtch == null) throw new Exception();
              if (promptSwitch(swtch)) break;
            } catch (Exception e) {
              log("Invalid number, aborting...");
            }
          } else if (query == 8) {
            pingStats();
          } else if (query == 9) {
            break;
          } else if (query == 10) {
            initiateShutDown();
          } else if (query == 11) {
            grabAllNodes();
            for (NodeInfo el : nodeList) {
              System.out.println(el.getAddress() + "\t" + el.getNameID() + "\t" + el.getNumID());
            }
          } else if (query == 12) {
            printCurrentLog();
          } else if (query == 13) {
            printAllLogs();
          } else if (query == 14) {
            log("Enter the numID of the node that you want to check if it is reachable:");
            String st = get();
            try {
              int inp = Integer.parseInt(st);
              checkIfReachable(inp);
            } catch (Exception e) {
              log("Invalid number, aborting...");
            }
          } else if (query == 15) {
            printAllErrors();
          } else if (query == 16) {
            long ind = System.currentTimeMillis() % 100;
            grabUniqueNodes();
            Configuration.generateConfigFile(
                nodeList,
                System.getProperty("user.dir")
                    + File.separator
                    + "Logs"
                    + File.separator
                    + "Configurations"
                    + File.separator
                    + "node"
                    + ind
                    + ".conf");
            grabAllNodes();
            Configuration.generateConfigFile(
                nodeList,
                System.getProperty("user.dir")
                    + File.separator
                    + "Logs"
                    + File.separator
                    + "Configurations"
                    + File.separator
                    + "node_with_data"
                    + ind
                    + ".conf");
          } else {
            log("No matching query.");
          }
        }
      } catch (Exception e) {
        e.printStackTrace();
        System.out.println("Exception caught. Restarting.");
      }
    }
  }

  public static void printMenu() throws IOException {
    log("Address of node being controlled: " + ip + ":" + port);
    log("Name ID: " + nameID + " Number ID: " + numID);
    log("Choose a query by entering it's code and then press Enter");
    log("0-Start a simulation");
    log("1-Insert Transaction");
    log("2-Insert Block");
    log("3-Search By Name ID");
    log("4-Search By Number ID");
    log("5-Print the Lookup Table");
    log("6-Print data");
    log("7-Traverse");
    log("8-Perform latency testing");
    log("9-Exit");
    log("10-Shut down all instances");
    log("11-Print all nodes");
    log("12-Print node's log (to csv)");
    log("13-Grab and print all logs");
    log("14-Check if a node is reachable.");
    log("15-Print logged exceptions");
    log("16-Generate config file of skipGraph.");
  }

  /*
   * Start Simulation
   */
  private static ConcurrentHashMap<NodeInfo, TestingLog> TestingLogMap;

  public static void startSim() {
    TestingLogMap = new ConcurrentHashMap<>();

    // Grab all the nodes so we can communicate with them
    grabUniqueNodes();

    // Simulation variables
    int numTransactions; // Number of transactions to insert per node
    int averagePace; // Every how many seconds on average should a node insert a transaction

    // Getting input from user

    String inp;
    log("How many transactions to insert: (per node)");
    inp = get();
    while (!inp.matches("[1-9][0-9]*")) {
      log("Enter a valid number:");
      inp = get();
    }
    numTransactions = Integer.parseInt(inp);

    log(
        "How often should a node insert a transaction in seconds? (60 means 1 transaction every 60 seconds)");
    inp = get();
    while (!inp.matches("[1-9][0-9]*")) {
      log("Enter a valid number:");
      inp = get();
    }
    averagePace = Integer.parseInt(inp);

    // Making threads to get the nodes to start functioning at the same time:
    int sz = nodeList.size();
    try {
      CountDownLatch ltch = new CountDownLatch(sz);
      for (int i = 0; i < sz; i++) {
        SimThread cur = new SimThread(i, ltch, numTransactions, averagePace);
        cur.start();
      }
      ltch.await();
    } catch (InterruptedException e) {
      e.printStackTrace();
    }

    printTestingLog();
  }

  /*
   * Print Testing Log
   */

  private static void printTestingLog() {
    try {
      File logPath =
          new File(
              System.getProperty("user.dir")
                  + File.separator
                  + "Logs"
                  + File.separator
                  + "Simulations"
                  + File.separator
                  + "TestingLog"
                  + System.currentTimeMillis() % 200
                  + ".csv");
      logPath.getParentFile().mkdirs();
      PrintWriter writer = new PrintWriter(logPath);
      StringBuilder sb = new StringBuilder();

      sb.append(
          "NumID,Malicious,Transaction Attempts,Transaction Success,Transaction time(per),numAuthenticated,numSound,numCorrect,hasBalance,Success?,View Update Time (per),"
              + "TX>TXMIN?,Validate Block time,Validate success\n");

      for (NodeInfo cur : TestingLogMap.keySet()) {
        TestingLog lg = TestingLogMap.get(cur);
        ArrayList<TransactionLog> transactions = lg.getTransactionAttempts();
        Collections.sort(transactions);
        ArrayList<ViewUpdateLog> viewUpdates = lg.getViewUpdateLog();
        Collections.sort(viewUpdates);
        sb.append(
            cur.getNumID()
                + ","
                + lg.isMalicious()
                + ","
                + lg.getAttempts()
                + ","
                + lg.getSuccess()
                + ",");
        for (int i = 0; i < transactions.size(); i++) {
          if (i != 0) sb.append(",,,,");
          sb.append(transactions.get(i));
          if (i < viewUpdates.size()) {
            sb.append("," + viewUpdates.get(i));
          }
          sb.append("\n");
        }
        sb.append('\n');
      }
      writer.write(sb.toString());
      writer.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private static int numPings = 10; // Total number of pinging attempts
  private static int numAtts = 1; // How many different pinging sessions to divide the attempts into
  private static boolean printProgress =
      true; // Whether you want the pingstats function to print % done while called
  // or not.
  private static ArrayList<NodeInfo> nodeList;
  private static ConcurrentHashMap<NodeInfo, ArrayList<PingLog>> res;

  public static void pingStats() {
    res = new ConcurrentHashMap<NodeInfo, ArrayList<PingLog>>();
    String inp;

    log(
        "Choose type of testing\n1- Retro testing (Using commandline 'ping')\n2- RMI testing (Calls an RMI function)");
    inp = get();
    while (!inp.matches("1|2")) {
      log("Choose a valid option");
      inp = get();
    }
    int choice = Integer.parseInt(inp);

    log("Enter the total number of ping attempts per node pair:");
    inp = get();
    while (!inp.matches("[1-9][0-9]*")) {
      log("Enter a valid number");
      inp = get();
    }
    numPings = Integer.parseInt(inp);
    log("Enter the total number of sessions to divide the attempts into:");
    inp = get();
    while (!inp.matches("0|[1-9][0-9]*")) {
      log("Enter a valid number");
      inp = get();
    }
    numAtts = Integer.parseInt(inp);
    grabUniqueNodes();

    int sz = nodeList.size();
    for (int k = 0; k < numAtts; k++) {
      try {
        CountDownLatch ltch = new CountDownLatch(sz);
        for (int i = 0; i < sz; i++) {

          PingingThread cur = new PingingThread(i, ltch, numPings / numAtts, choice);
          cur.start();
        }

        ltch.await();
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
      if (printProgress) System.out.println("Percentage done: " + 100.0 * k / numAtts + "%");
    }
    try {
      File pingLog =
          new File(
              System.getProperty("user.dir")
                  + File.separator
                  + "Logs"
                  + File.separator
                  + "RTTDelay"
                  + File.separator
                  + "log"
                  + System.currentTimeMillis() % 20
                  + ".csv");
      pingLog.getParentFile().mkdirs();
      PrintWriter writer = new PrintWriter(pingLog);
      StringBuilder sb = new StringBuilder();

      for (NodeInfo cur : res.keySet()) {
        sb.append("Pinger," + cur.getNumID());
        sb.append('\n');
        sb.append("Pinged,Avg Ping,StdDev,Individual Results");
        sb.append('\n');
        for (int i = 0; i < res.get(cur).size(); i++) {
          sb.append(res.get(cur).get(i).getPinged().getNumID());
          ArrayList<Double> hm = res.get(cur).get(i).getRTTLog();
          sb.append("," + res.get(cur).get(i).getAvg() + "," + res.get(cur).get(i).getStdDev());
          for (int j = 0; j < hm.size(); j++) {
            sb.append("," + hm.get(j));
          }
          sb.append('\n');
        }
        sb.append('\n');
      }
      sb.append('\n');
      sb.append("Information regarding the nodes in the current graph:\n");
      sb.append("IP,NameID,NumID\n");
      for (NodeInfo cur : nodeList) {
        sb.append(cur.getAddress() + "," + cur.getNameID() + "," + cur.getNumID() + "\n");
      }
      writer.write(sb.toString());
      writer.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  /*
   * Shuts down all the node in the network it connects to
   */

  private static void initiateShutDown() {
    grabUniqueNodes();
    int sz = nodeList.size();
    try {
      CountDownLatch ltch = new CountDownLatch(sz);
      for (int i = 0; i < sz; i++) {
        ShutDownThread t = new ShutDownThread(i, ltch);
        t.start();
      }
      ltch.await();
    } catch (Exception e) {
      // Not needed since they will always throw an exception
    }
  }

  /*
   * Grab the current node's log and print thtem to CSV
   */

  private static void printCurrentLog() {
    TestingLogMap = new ConcurrentHashMap<>();
    try {
      //	TestingLogMap.put(node.getNode(node.getNumID()), node.getSimLog());
    } catch (Exception e) {
      e.printStackTrace();
      return;
    }
    printTestingLog();
  }

  /*
   * Grabs all the testing logs from all the nodes and prints them to CSV (Use
   * this in case an exception arises during testing and you want to recover the
   * logs
   */

  private static void printAllLogs() {
    TestingLogMap = new ConcurrentHashMap<>();
    for (NodeInfo node : nodeList) {
      try {
        LightChainInterface cur = getRMI(node.getAddress());
        // TestingLogMap.put(node, cur.getTestLog());
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    printTestingLog();
  }

  /*
   * Grabs all the testing logs from all the nodes and prints the
   * errors/exceptions to a txt file
   */

  private static void printAllErrors() {
    try {
      grabUniqueNodes();
      File errordir =
          new File(
              System.getProperty("user.dir")
                  + File.separator
                  + "Logs"
                  + File.separator
                  + "Errors"
                  + File.separator
                  + "ErrorLog.txt");
      File excepdir =
          new File(
              System.getProperty("user.dir")
                  + File.separator
                  + "Logs"
                  + File.separator
                  + "Exceptions"
                  + File.separator
                  + "ExceptionLog.txt");
      errordir.getParentFile().mkdirs();
      excepdir.getParentFile().mkdirs();
      System.out.println(errordir.getPath());
      PrintWriter erpw = new PrintWriter(errordir);
      PrintWriter expw = new PrintWriter(excepdir);
      for (NodeInfo cur : nodeList) {
        try {
          log("Node: " + cur);
          LightChainInterface curRMI = getRMI(cur.getAddress());
          //	TestingLog lg = curRMI.getTestLog();// .printExceptionLogs();
          //	lg.printExceptionLogs(expw);
          //	lg.printOverflowLogs(erpw);
        } catch (Exception e) {
          log("Error printing errors.");
          e.printStackTrace();
        }
      }
    } catch (Exception e) {
      log("Error getting exceptions. :)");
      e.printStackTrace();
    }
  }

  /*
   * Traverse Skip Graph and populate nodeList with all the nodes in the graph
   */
  public static void grabAllNodes() {
    NodeInfo curNode = null;
    nodeList = new ArrayList<NodeInfo>();
    try {
      curNode = node.searchByNumID(0);
      System.out.println();
      while (curNode != null) {
        nodeList.add(curNode);
        NodeInfoResponse response = NodeInfoResponseOf(RMIUnderlay.sendMessage(
                new GetRightNodeRequest(0, curNode.getNumID()), curNode.getAddress()));
        curNode = response.result;
      }
    } catch (RemoteException e) {
      e.printStackTrace();
      return;
    }
    System.out.println("Total number of nodes: " + nodeList.size());
    return;
  }

  public static void grabUniqueNodes() { // To filter out transactions and blocks
    NodeInfo curNode = null;
    HashSet<String> addresses = new HashSet<String>();
    nodeList = new ArrayList<>();
    try {
      curNode = node.searchByNumID(0);
      while (curNode != null) {
        addresses.add(curNode.getAddress());
        NodeInfoResponse response = NodeInfoResponseOf(RMIUnderlay.sendMessage(
                new GetRightNodeRequest(0, curNode.getNumID()), curNode.getAddress()));
        curNode = response.result;
      }
      for (String adrs : addresses) {
        IntegerResponse response =
            IntegerResponseOf(RMIUnderlay.sendMessage(new GetNumIDRequest(), adrs));
        int numID = response.result;
        NodeInfoResponse nodeInfoResponse =
            NodeInfoResponseOf(RMIUnderlay.sendMessage(new GetNodeRequest(numID), adrs));
        NodeInfo nodeInfo = nodeInfoResponse.result;
        nodeList.add(nodeInfo);
      }
    } catch (RemoteException e) {
      e.printStackTrace();
      return;
    }
    System.out.println("Total number of nodes: " + nodeList.size());
    return;
  }

  public static boolean checkIfReachable(int numID) {
    grabAllNodes();
    NodeInfo[][][] curlookup;
    boolean flag = false;
    for (NodeInfo cur : nodeList) {
      try {
        SkipNodeInterface nd = getRMI(cur.getAddress());
        //				curlookup = nd.getLookupTable();
        //				for(int i=0;i<curlookup.length;i++) {
        //					for(int j=0;j<curlookup[0].length;j++) {
        //						for(int k=0;k<curlookup[0][0].length;k++) {
        //							if(curlookup[i][j][k]!=null && curlookup[i][j][k].getNumID() == numID) {
        //								log("The numID "+ numID + " is reachable from the following parameters.\nNode:
        // "+cur+"\ni: "+i+" j:"+j+" k:"+k);
        //								flag = true;
        //							}
        //						}
        //					}
        //				}
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    return flag;
  }

  /*
   * Taken from SkipNode class. However, it needs to be implemented here so that
   * println would print here rather than in the other node.
   */

  public static void printLookup(int num) {
    try {
      // lookup = node.getLookupTable();
    } catch (Exception e) {
      log("Couldn't update the lookup table properly. Aborting...");
      return;
    }
    int cnt = (lookup.length - 1) * 2;
    for (int i = lookup.length - 2; i >= 0; i--) // double check the initial value of i
    {
      cnt -= 2;
      log(
          cnt
              + " "
              + ((lookup[i][0][num] == null) ? "null\t" : (lookup[i][0][num].getNameID() + "\t"))
              + (cnt + 1)
              + " "
              + ((lookup[i][1][num] == null) ? "null\t" : (lookup[i][1][num].getNameID() + "\t")));
    }
  }

  public static void printData() {
    for (int i = 0; i < data.size(); ++i)
      log(data.get(i).getNumID() + " " + data.get(i).getNameID());
    log("");
  }

  public static boolean promptSwitch(NodeInfo node) {
    if (node == null) {
      System.out.println("Can't switch to null node. Aborting...");
      return false;
    }
    log("Would you like to switch the remote to node at address: " + node.getAddress() + " ?");
    log("The node's name ID is: " + node.getNameID() + " and its num ID is: " + node.getNumID());
    log("Enter 'Y' to confirm, anything else to abort.");
    String inp = get();
    if (inp.equalsIgnoreCase("Y")) {
      ip = node.getAddress();
    } else {
      return false;
    }
    skipInit = true;
    return true;
  }

  /*
   * The following methods are taken from the other class. It might be a good idea
   * to have a way to use them in both classes without having to copy paste it
   * here again
   */

  public static void log(String st) {
    System.out.println(st);
  }

  public static String get() {
    return in.nextLine();
  }

  private static boolean validateIP(String adrs) {
    int colonIndex = adrs.indexOf(':');
    String ip = adrs;
    if (colonIndex != -1) ip = adrs.substring(0, colonIndex);
    String[] parts = ip.split("\\.");
    if (parts.length != 4) {
      return false;
    }
    try {
      for (String el : parts) {
        int num = Integer.parseInt(el);
        if (num < 0 || num > 255) return false;
      }
    } catch (NumberFormatException e) {
      return false;
    }
    if (ip.endsWith(".")) return false;
    return true;
  }

  public static LightChainInterface getRMI(String adrs) {
    if (validateIP(adrs))
      try {
        return (LightChainInterface) Naming.lookup("//" + adrs + "/RMIImpl");
      } catch (Exception e) {
        log("Exception while attempting to lookup RMI located at address: " + adrs);
      }
    else {
      log("Error in looking up RMI. Address: " + adrs + " is not a valid address.");
    }
    return null;
  }

  /*
   * Thread classes for multithreading
   */
  static class PingingThread extends Thread {
    Thread t;
    String pinger;
    CountDownLatch latch;
    int count;
    int index;
    int choice;

    public PingingThread(int ind, CountDownLatch ltch, int count, int choice) {
      this.pinger = nodeList.get(ind).getAddress();
      this.latch = ltch;
      this.index = ind;
      this.count = count;
      this.choice = choice;
    }

    public void run() {
      SkipNodeInterface curRMI = getRMI(pinger);
      for (int i = 0; i < nodeList.size(); i++) {
        if (i == index) continue;
        PingLog current;
        try {
          if (choice == 2) {
            current = curRMI.pingStart(nodeList.get(i), count);
          } else {
            current = curRMI.retroPingStart(nodeList.get(i), count);
          }
          if (res.containsKey(nodeList.get(index))) {
            ArrayList<PingLog> cur = res.get(nodeList.get(index));
            if (cur.size() > (i < index ? i : i - 1)) {
              cur.get(i < index ? i : i - 1).append(current);
              res.put(nodeList.get(index), cur);
            } else {
              cur.add(current);
              res.put(nodeList.get(index), cur);
            }
          } else {
            ArrayList<PingLog> cur = new ArrayList<PingLog>();
            cur.add(current);
            res.put(nodeList.get(index), cur);
          }
        } catch (Exception e) {
          System.err.println("Exception thrown in pinging thread.");
          e.printStackTrace();
        }
      }
      latch.countDown();
    }
  }

  static class SimThread extends Thread {
    Thread t;
    CountDownLatch latch;
    int count;
    int pace;
    int ind;

    public SimThread(int ind, CountDownLatch ltch, int count, int pace) {
      this.latch = ltch;
      this.pace = pace;
      this.count = count;
      this.ind = ind;
    }

    public void run() {
      LightChainInterface curRMI = getRMI(nodeList.get(ind).getAddress());
      try {
        if (ind == 0) {
          curRMI.insertGenesis();
        }
        SimLog lg = curRMI.startSim(count, pace);
        // TestingLogMap.put(nodeList.get(ind), lg);
      } catch (RemoteException e) {
        e.printStackTrace();
      }
      latch.countDown();
    }
  }

  static class ShutDownThread extends Thread {
    Thread t;
    CountDownLatch latch;
    int ind;

    public ShutDownThread(int ind, CountDownLatch ltch) {
      this.latch = ltch;
      this.ind = ind;
    }

    public void run() {
      LightChainInterface curRMI = getRMI(nodeList.get(ind).getAddress());
      try {
        curRMI.shutDown();
      } catch (Exception e) {
        // No need (it will always throw an error)
      }
      latch.countDown();
    }
  }
}
