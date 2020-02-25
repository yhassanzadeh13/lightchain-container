package simulation;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;

import blockchain.LightChainNode;
import blockchain.Parameters;
import skipGraph.NodeInfo;
import util.Const;
import util.Util;

public class Simulation {

	public static void main(String args[]) {

		Parameters params = new Parameters();
		params.setAlpha(12);
		params.setTxMin(10);
		params.setSignaturesThreshold(10);
		
		int iterations = 50;
		int pace = 1;
		try {
			
			LightChainNode node1 = new LightChainNode(params, 7001, Const.DUMMY_INTRODUCER, true);
			LightChainNode node2 = new LightChainNode(params, 7002, node1.getAddress(), false);
			LightChainNode node3 = new LightChainNode(params, 7003, node2.getAddress(), false);
			LightChainNode node4 = new LightChainNode(params, 7004, node3.getAddress(), false);
			LightChainNode node5 = new LightChainNode(params, 7005, node1.getAddress(), false);
			LightChainNode node6 = new LightChainNode(params, 7006, node2.getAddress(), false);
			LightChainNode node7 = new LightChainNode(params, 7007, node3.getAddress(), false);
			LightChainNode node8 = new LightChainNode(params, 7008, node1.getAddress(), false);
			LightChainNode node9 = new LightChainNode(params, 7009, node2.getAddress(), false);
			LightChainNode node10 = new LightChainNode(params,7010, node3.getAddress(), false);
			
			
			LightChainNode node11 = new LightChainNode(params, 7011, node8.getAddress(), false);
			LightChainNode node12 = new LightChainNode(params, 7012, node9.getAddress(), false);
			LightChainNode node13 = new LightChainNode(params, 7013, node8.getAddress(), false);
			LightChainNode node14 = new LightChainNode(params, 7014, node9.getAddress(), false);
			LightChainNode node15 = new LightChainNode(params, 7015, node4.getAddress(), false);
			
			
			LightChainNode node16 = new LightChainNode(params, 7016, node5.getAddress(), false);
			LightChainNode node17 = new LightChainNode(params, 7017, node8.getAddress(), false);
			LightChainNode node18 = new LightChainNode(params, 7018, node7.getAddress(), false);
			LightChainNode node19 = new LightChainNode(params, 7019, node6.getAddress(), false);
			LightChainNode node20 = new LightChainNode(params, 7020, node5.getAddress(), false);
			
			
			ArrayList<LightChainNode> nodes = new ArrayList<>();
			node1.insertGenesis();

			nodes.add(node1);
			nodes.add(node2);
			nodes.add(node3);
			nodes.add(node4);
			nodes.add(node5);
			nodes.add(node6);
			nodes.add(node7);
			nodes.add(node8);
			nodes.add(node9);
			nodes.add(node10);
			nodes.add(node11);
			nodes.add(node12);
			nodes.add(node13);
			nodes.add(node14);
			nodes.add(node15);
			nodes.add(node16);
			nodes.add(node17);
			nodes.add(node18);
			nodes.add(node19);
			nodes.add(node20);

			ConcurrentHashMap<NodeInfo, SimLog> map = new ConcurrentHashMap<>();
			CountDownLatch latch = new CountDownLatch(nodes.size());
			long startTime = System.currentTimeMillis();
			for (int i = 0; i < nodes.size(); ++i) {
				SimThread sim = new SimThread(nodes.get(i), latch, map, iterations, pace);
				sim.start();
			}
			latch.await();
			
			node1.printLevel(0);
			
			long endTime = System.currentTimeMillis();
			
			Util.log("Simulation Done. Time Taken " +(endTime - startTime)+ " ms");
			
			processTransactions(map,iterations);
			processMineAttempts(map,iterations);
			
			
			
			System.exit(0);
		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}


	private static void processMineAttempts(ConcurrentHashMap<NodeInfo, SimLog> map,int iterations) {

		try {
			String logPath = System.getProperty("user.dir") + File.separator + "Logs" + File.separator
					+ "MineAttemps.csv";
			File logFile = new File(logPath);

			logFile.getParentFile().mkdirs();
			PrintWriter writer;

			writer = new PrintWriter(logFile);

			StringBuilder sb = new StringBuilder();

			sb.append("NumID," + "Malicious," + "total time(ms)," + "foundTxMin," + "Validation time(ms)," + "successful\n");

			int successSum = 0;
			
			for (NodeInfo cur : map.keySet()) {
				SimLog log = map.get(cur);
				List<MineAttemptLog> validMine = log.getValidMineAttemptLog();
				List<MineAttemptLog> failedMine = log.getFailedMineAttemptLog();

				sb.append(cur.getNumID() + "," + log.getMode() + ",");
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
			}
			sb.append('\n');
			double successRate = (double)successSum / (1.0 * iterations * map.keySet().size()) * 100;
			sb.append("Success Rate = " + successRate + "\n");
			writer.write(sb.toString());
			writer.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

	}

	private static void processTransactions(ConcurrentHashMap<NodeInfo, SimLog> map,int iterations) {

		try {
			String logPath = System.getProperty("user.dir") + File.separator + "Logs" + File.separator
					+ "TransactionValidation.csv";
			File logFile = new File(logPath);

			logFile.getParentFile().mkdirs();
			PrintWriter writer;

			writer = new PrintWriter(logFile);

			StringBuilder sb = new StringBuilder();

			sb.append("NumID," + "Malicious," + "Transaction Trials," + "Transaction Success,"
					+ "Transaction time(ms)," + "Authenticated count," + "Sound count," + "Correct count,"
					+ "Has Balance count," + "Successful," + "Time per Validator\n");
			
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
					sb.append("\n");
				}
				successSum += validTransactions.size();
				for (int i = 0; i < failedTransactions.size(); i++) {
					sb.append(",,,,");
					sb.append(failedTransactions.get(i));
					sb.append("\n");
				}

			}
			sb.append('\n');
			double successRate = (double)(successSum * 100.0) / (1.0 * iterations * map.keySet().size());
			sb.append("Success Rate = " + successRate + "\n");
			writer.write(sb.toString());
			writer.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

	}
}
