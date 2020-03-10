package remoteTest;

import java.io.Serializable;
import java.util.ArrayList;

import skipGraph.NodeInfo;

public class PingLog implements Serializable {
	private static final long serialVersionUID = 1L;
	private NodeInfo Pinger;
	private NodeInfo Pinged;
	private ArrayList<Double> RTTLog;
	
	public PingLog(NodeInfo Pinger, NodeInfo Pinged) {
		this.Pinger = Pinger;
		this.Pinged = Pinged;
		RTTLog = new ArrayList<Double>();
	}
	
	public void Log(Double time) {
		RTTLog.add(time);
	}

	public double getAvg() {
		if(RTTLog.size()==0) return -1;
		double sum = 0;
		for(int i=0;i<RTTLog.size();i++) {
			sum+=RTTLog.get(i);
		}
		return sum/RTTLog.size();
	}
	
	public double getStdDev() {
		double mean = getAvg();
		double result = 0;
		for(int i=0;i<RTTLog.size();i++) {
			result += Math.pow(RTTLog.get(i)-mean, 2);
		}
		return Math.sqrt(result/RTTLog.size());
	}
	
	public NodeInfo getPinger() {
		return Pinger;
	}

	public NodeInfo getPinged() {
		return Pinged;
	}

	public ArrayList<Double> getRTTLog() {
		return RTTLog;
	}
	
	/*
	 * Appends two logs with the same Pinger and Pinged. This can be useful if you want to split the pinging sessions
	 * into smaller sessions.
	 */
	
	public boolean append(PingLog newlog) {
		if(newlog.getPinged().equals(this.Pinged) && newlog.getPinger().equals(this.Pinger)) {
			RTTLog.addAll(newlog.getRTTLog());
			return true;
		}else {
			System.err.println("Error. Can't append pinging log of different pinger and pinged.");
			return false;
		}
	}
	
}
