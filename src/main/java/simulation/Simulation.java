package simulation;

import blockchain.LightChainNode;
import blockchain.Parameters;
import mock.MockNetwork;
import skipGraph.LookupTable;
import skipGraph.NodeInfo;
import util.Const;
import util.Util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.rmi.RemoteException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;

public class Simulation {

	public static void startSimulation(Parameters params, int nodeCount ,int iterations, int pace, boolean mockMode){

	  Map<Integer, LightChainNode> nodesMap = new HashMap<>();

	  try {
			Random rnd = new Random();
			ArrayList<LightChainNode> nodes = new ArrayList<>();
			LightChainNode initialNode = null;
			int numFailures;
			for(int i = 0 ; i < nodeCount ; i++){
				try{
					int port = rnd.nextInt(65535);
					LightChainNode node;
					if(i == 0){
						node = new LightChainNode(params, port, Const.DUMMY_INTRODUCER, true);
						initialNode = node;
					} else {
						node = new LightChainNode(params, port, initialNode.getAddress(), false);
					}
					nodes.add(node);
					nodesMap.put(node.getNumID(), node);
				}catch(RemoteException re){
					i--;
					continue;
				}
			}

			if(mockMode) {
        MockNetwork mock = new MockNetwork(nodesMap);

        for(LightChainNode node : nodes) {
          node.setMockMode(true);
          node.setMockNetwork(mock);
        }
      }

			initialNode.insertGenesis();

			initialNode.logLevel(0);

			ConcurrentHashMap<NodeInfo, SimLog> map = new ConcurrentHashMap<>();
			CountDownLatch latch = new CountDownLatch(nodes.size());
			long startTime = System.currentTimeMillis();
			for (int i = 0; i < nodes.size(); ++i) {
				SimThread sim = new SimThread(nodes.get(i), latch, map, iterations, pace);
				sim.start();
			}
			latch.await();
			

			long endTime = System.currentTimeMillis();
			Util.log("Simulation Done. Time Taken " +(endTime - startTime)+ " ms");
			
			processData(map, iterations);

		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}   }


	private static void processData(ConcurrentHashMap<NodeInfo, SimLog> map,int iterations) {
		processTransactions(map, iterations);
		processMineAttempts(map, iterations);
	}

	private static void processMineAttempts(ConcurrentHashMap<NodeInfo, SimLog> map,int iterations) {

		try {
			String logPath = System.getProperty("user.dir") + File.separator + "Logs" + File.separator
					+ "MineAttempts.csv";
			File logFile = new File(logPath);

			logFile.getParentFile().mkdirs();
			PrintWriter writer;

			writer = new PrintWriter(logFile);

			StringBuilder sb = new StringBuilder();

			sb.append("NumID," + "Honest," + "total time," + "foundTxMin," + "Validation time," + "successful\n");

			int successSum = 0;

			for (NodeInfo cur : map.keySet()) {
				SimLog log = map.get(cur);
				List<MineAttemptLog> validMine = log.getValidMineAttemptLog();
				List<MineAttemptLog> failedMine = log.getFailedMineAttemptLog();

				sb.append(cur.getNumID() + "," + log.getMode()+",");
				for (int i = 0; i < validMine.size(); i++) {
					if (i != 0)
						sb.append(",,");
					sb.append(validMine.get(i));
				}
				successSum += validMine.size();
				for (int i = 0; i < failedMine.size(); i++) {
					sb.append(",,");
					sb.append(failedMine.get(i));
				}
				sb.append('\n');
			}
			double successRate = (double)successSum / (1.0 * iterations * map.keySet().size()) * 100;
			sb.append("Success Rate = " + successRate + "\n");

			writer.write(sb.toString());
			writer.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private static void processTransactions(ConcurrentHashMap<NodeInfo, SimLog> map, int iterations) {

		try {
			String logPath = System.getProperty("user.dir") + File.separator + "Logs" + File.separator
					+ "TransactionValidationAttempts.csv";
			File logFile = new File(logPath);

			logFile.getParentFile().mkdirs();
			PrintWriter writer;

			writer = new PrintWriter(logFile);

			StringBuilder sb = new StringBuilder();

			sb.append("NumID," + "Honest," + "Transaction Trials," + "Transaction Success,"
					+ "Transaction time(per)," + "Authenticated count," + "Sound count," + "Correct count,"
					+ "Has Balance count," + "Successful,"  +  "Timer Per Validator(ms)\n");

			int successSum = 0;

			for (NodeInfo cur : map.keySet()) {
				SimLog log = map.get(cur);
				List<TransactionLog> validTransactions = log.getValidTransactions();
				List<TransactionLog> failedTransactions = log.getFailedTransactions();

				sb.append(cur.getNumID() + "," + log.getMode() + "," + log.getTotalTransactionTrials() + ","
						+ log.getValidTransactionTrials() + ",");
				for (int i = 0; i < validTransactions.size(); i++) {
					if (i != 0)
						sb.append(",,,,");
					sb.append(validTransactions.get(i));
				}
				successSum += validTransactions.size();
				for (int i = 0; i < failedTransactions.size(); i++) {
					sb.append(",,,,");
					sb.append(failedTransactions.get(i));
				}
				sb.append('\n');
			}
			double successRate = (double)(successSum * 100.0) / (1.0 * iterations * map.keySet().size());
			sb.append("Success Rate = " + successRate + "\n");
			writer.write(sb.toString());
			writer.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

	}
}
