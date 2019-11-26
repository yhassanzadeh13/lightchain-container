package blockchain;

import java.util.HashMap;
import java.util.Map;

public class View {

	private Map<Integer, Integer> lastBlk;
	private Map<Integer, Integer> state;
	private Map<Integer, Integer> balance;
	private Map<Integer, Boolean> mode;

	public View() {
		lastBlk = new HashMap<>();
		state = new HashMap<>();
		balance = new HashMap<>();
		mode = new HashMap<>();
	}

	public void updateLastBlk(int numID, int blkNumID) {
		lastBlk.put(numID, blkNumID);
	}

	public void updateState(int numID, int newState) {
		state.put(numID, newState);
	}

	public void updateBalance(int numID, int newBalance) {
		balance.put(numID, newBalance);
	}

	public void updateMode(int numID, boolean newMode) {
		mode.put(numID, newMode);
	}
	
	public int getLastBlk(int numID) {
		return lastBlk.get(numID);
	}
	
	public int getState(int numID) {
		return state.get(numID);
	}
	
	public int getBalance(int numID) {
		return balance.get(numID);
	}
	
	public boolean getMode(int numID) {
		return mode.get(numID);
	}
	
	public boolean hasLastBlkEntry(int numID) {
		return lastBlk.containsKey(numID);
	}
	
	public boolean hasStateEntry(int numID) {
		return state.containsKey(numID);
	}
	
	public boolean hasBalanceEntry(int numID) {
		return balance.containsKey(numID);
	}
	
	public boolean hasModeEntry(int numID) {
		return mode.containsKey(numID);
	}

}
