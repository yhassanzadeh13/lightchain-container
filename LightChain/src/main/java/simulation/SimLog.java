package simulation;

import java.util.ArrayList;
import java.util.List;

public class SimLog {

	private boolean mode;
	private List<TransactionLog> validTransactions;
	private List<TransactionLog> failedTransactions;
	private List<MineAttemptLog> validMineLog;
	private List<MineAttemptLog> failedMineLog;
	

	public SimLog(boolean mode) {
		this.mode = mode;
		validTransactions = new ArrayList<>();
		failedTransactions = new ArrayList<>();
		validMineLog = new ArrayList<>();
		failedMineLog = new ArrayList<>();
	}

	public void logTransaction(boolean success, int isAuthenticated, int isSound, int isCorrect, int hasBalance,
			long time) {
		TransactionLog log = new TransactionLog(success, isAuthenticated, isSound, isCorrect, hasBalance, time);
		if (success)
			validTransactions.add(log);
		else
			failedTransactions.add(log);
	}

	public void logMineAttemptLog(boolean foundTxMin, boolean success, long totalTime, long validationTime) {
		MineAttemptLog log = new MineAttemptLog(foundTxMin, success, totalTime, validationTime);
		if(success)
			validMineLog.add(log);
		else
			failedMineLog.add(log);
	}

	public List<TransactionLog> getValidTransactions() {
		return validTransactions;
	}

	public List<TransactionLog> getFailedTransactions(){
		return failedTransactions;
	}
	
	public List<MineAttemptLog> getValidMineAttemptLog(){
		return validMineLog;
	}
	
	public List<MineAttemptLog> getFailedMineAttemptLog(){
		return failedMineLog;
	}
	
	public boolean getMode() {
		return mode;
	}
	
	public int getTotalTransactionTrials() {
		return validTransactions.size() + failedTransactions.size();
	}
	
	public int getValidTransactionTrials() {
		return validTransactions.size();
	}
	
	public int getFailedTransactionTrials() {
		return failedTransactions.size();
	}
	
	public int getTotalMineTrials() {
		return validMineLog.size() + failedMineLog.size();
	}
	
	public int getValidMineTrials() {
		return validMineLog.size();
	}
	
	public int getFailedMineTrials() {
		return failedMineLog.size();
	}
	
	

}
