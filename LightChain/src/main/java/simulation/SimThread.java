package simulation;

import java.rmi.Naming;
import java.rmi.RemoteException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;

import blockchain.LightChainNode;
import blockchain.LightChainRMIInterface;
import remoteTest.TestingLog;
import skipGraph.NodeInfo;
import util.Util;

public class SimThread extends Thread {
	Thread t;
	CountDownLatch latch;
	int count;
	int pace;
	LightChainNode node;
	ConcurrentHashMap<NodeInfo, SimLog> map;

	public SimThread(LightChainNode node, CountDownLatch ltch, ConcurrentHashMap<NodeInfo, SimLog> map, int count,
			int pace) {
		this.latch = ltch;
		this.pace = pace;
		this.count = count;
		this.node = node;
		this.map = map;
	}

	public void run() {
		try {

			SimLog lg = node.startSim(count, pace);
			map.put(node.getPeer(), lg);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		latch.countDown();
	}

}
