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

public class Simulation {

	public static void main(String args[]) {

		Parameters params = new Parameters();
		params.setAlpha(10);
		params.setTxMin(2);
		params.setSignaturesThreshold(2);

		int iterations = 3;

		try {
			LightChainNode node1 = new LightChainNode(params, 7001, Const.DUMMY_INTRODUCER, true);
			LightChainNode node2 = new LightChainNode(params, 7002, node1.getAddress(), false);
			LightChainNode node3 = new LightChainNode(params, 7003, node1.getAddress(), false);

			ArrayList<LightChainNode> nodes = new ArrayList<>();
			node1.insertGenesis();

			nodes.add(node1);
			nodes.add(node2);
			nodes.add(node3);

			ConcurrentHashMap<NodeInfo, SimLog> map = new ConcurrentHashMap<>();
			CountDownLatch latch = new CountDownLatch(nodes.size());
			for (int i = 0; i < nodes.size(); ++i) {
				SimThread sim = new SimThread(nodes.get(i), latch, map, 10, 1);
				sim.start();
			}
			latch.await();

			processData(map);

		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	private static void processData(ConcurrentHashMap<NodeInfo, SimLog> map) {
		processTransactions(map);
		processMineAttempts(map);
	}

	private static void processMineAttempts(ConcurrentHashMap<NodeInfo, SimLog> map) {

		try {
			String logPath = System.getProperty("user.dir") + File.separator + "Logs" + File.separator
					+ "MineAttemps.csv";
			File logFile = new File(logPath);

			logFile.getParentFile().mkdirs();
			PrintWriter writer;

			writer = new PrintWriter(logFile);

			StringBuilder sb = new StringBuilder();

			sb.append("NumID," + "Malicious," + "total time," + "foundTxMin," + "Validation time," + "successful\n");

			for (NodeInfo cur : map.keySet()) {
				SimLog log = map.get(cur);
				List<MineAttemptLog> validMine = log.getValidMineAttemptLog();
				List<MineAttemptLog> failedMine = log.getFailedMineAttemptLog();

				sb.append(cur.getNumID() + "," + log.getMode());
				for (int i = 0; i < validMine.size(); i++) {
					if (i != 0)
						sb.append(",,,,");
					sb.append(validMine.get(i));
					sb.append("\n");
				}

				for (int i = 0; i < failedMine.size(); i++) {
					if (i != 0)
						sb.append(",,,,");
					sb.append(failedMine.get(i));
					sb.append("\n");
				}
			}
			sb.append('\n');
			writer.write(sb.toString());
			writer.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

	}

	private static void processTransactions(ConcurrentHashMap<NodeInfo, SimLog> map) {

		try {
			String logPath = System.getProperty("user.dir") + File.separator + "Logs" + File.separator
					+ "TransactionValidation.csv";
			File logFile = new File(logPath);

			logFile.getParentFile().mkdirs();
			PrintWriter writer;

			writer = new PrintWriter(logFile);

			StringBuilder sb = new StringBuilder();

			sb.append("NumID," + "Malicious," + "Transaction Trials," + "Transaction Success,"
					+ "Transaction time(per)," + "Authenticated count," + "Sound count," + "Correct count,"
					+ "Has Balance count," + "Successful\n");

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

				for (int i = 0; i < failedTransactions.size(); i++) {
					if (i != 0)
						sb.append(",,,,");
					sb.append(failedTransactions.get(i));
					sb.append("\n");
				}

			}

			sb.append('\n');
			writer.write(sb.toString());
			writer.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

	}
}
