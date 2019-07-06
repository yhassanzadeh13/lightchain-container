package remoteTest;

import java.io.Serializable;
import java.util.ArrayList;

import skipGraph.NodeInfo;

public class PingLog implements Serializable {
	private static final long serialVersionUID = 1L;
	private NodeInfo Pinger;
	private NodeInfo Pinged;
	private ArrayList<Long> RTTLog;
	
	public PingLog(NodeInfo Pinger, NodeInfo Pinged) {
		this.Pinger = Pinger;
		this.Pinged = Pinged;
		RTTLog = new ArrayList<Long>();
	}
	
	public void Log(Long time) {
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
	
	public NodeInfo getPinger() {
		return Pinger;
	}

	public NodeInfo getPinged() {
		return Pinged;
	}

	public ArrayList<Long> getRTTLog() {
		return RTTLog;
	}
	
	
}
