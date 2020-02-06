package simulation;

import java.io.Serializable;


class TransactionLog implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private boolean success;
	private int isAuthenticated;
	private int isSound;
	private int isCorrect;
	private int hasBalance;
	private long timeTaken;
	
	public TransactionLog(boolean success,int isAuthenticated, int isSound, int isCorrect, int hasBalance, long timeTaken) {
		this.success=success;
		this.isAuthenticated = isAuthenticated;
		this.isSound = isSound;
		this.isCorrect = isCorrect;
		this.hasBalance = hasBalance;
		this.timeTaken=timeTaken;
	}
	
	public boolean isSuccessful() {
		return success;
	}
	
	public long timeTaken() {
		return timeTaken;
	}
	
	@Override
	public String toString() {
		return timeTaken+"," + isAuthenticated + "," + isSound + "," + isCorrect + "," + hasBalance + ","+success + "\n";
	}
}
