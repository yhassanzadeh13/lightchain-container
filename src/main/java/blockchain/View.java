package blockchain;

import java.util.HashMap;
import java.util.Map;

public class View {

	private Map<Integer, Integer> lastBlk;
	private Map<Integer, Integer> state;
	private Map<Integer, Integer> balance;
	private Map<Integer, Boolean> mode;
	private Map<Integer, Integer> token;
	
	/**
	 * Constructor for an empty view
	 */
	public View() {
		lastBlk = new HashMap<>();
		state = new HashMap<>();
		balance = new HashMap<>();
		mode = new HashMap<>();
		token  = new HashMap<>();
	}

	/**
	 * updates the latest block of the node whose numID is given
	 * 
	 * @param numID    numerical ID of node whose entry is to be updated
	 * @param blkNumID the numerical ID of the latest block of the given node
	 */
	public synchronized void updateLastBlk(int numID, int blkNumID) {
		lastBlk.put(numID, blkNumID);
	}
	
	/**
	 * Updates the state of the node whose numerical ID is given
	 * @param numID numerical ID of node whose entry is to be updated
	 * @param newState new state of the node
	 */
	 
	public synchronized void updateState(int numID, int newState) {
		state.put(numID, newState);
	}

	public synchronized void updateBalance(int numID, int newBalance) {
		balance.put(numID, newBalance);
	}

	public synchronized void updateMode(int numID, boolean newMode) {
		mode.put(numID, newMode);
	}

	public synchronized int getLastBlk(int numID) {
		return lastBlk.get(numID);
	}

	public synchronized int getState(int numID) {
		return state.get(numID);
	}

	public synchronized int getBalance(int numID) {
		return balance.get(numID);
	}

	public synchronized boolean getMode(int numID) {
		return mode.get(numID);
	}

	public synchronized boolean hasLastBlkEntry(int numID) {
		return lastBlk.containsKey(numID);
	}

	public synchronized boolean hasStateEntry(int numID) {
		return state.containsKey(numID);
	}

	public synchronized boolean hasBalanceEntry(int numID) {
		return balance.containsKey(numID);
	}

	public synchronized boolean hasModeEntry(int numID) {
		return mode.containsKey(numID);
	}

	public synchronized int getToken(int numID) {
		return token.get(numID);
	}

	public synchronized void updateToken(int numID, int newToken) {
		token.put(numID, newToken);
	}

	public synchronized boolean hasTokenEntry(int numID) {
		return token.containsKey(numID);
	}

}
