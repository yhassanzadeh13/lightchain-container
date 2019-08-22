package remoteTest;

import java.io.Serializable;
import java.util.ArrayList;

public class TestingLog implements Serializable, Comparable<TestingLog>{
	
	private static final long serialVersionUID = 1L;

	private boolean malicious=false;
	private int Attempts;
	private int Success;
	private ArrayList<TransactionLog> transactionAttempts;
	private ArrayList<ViewUpdateLog> viewUpdateLog;
	private ViewUpdateLog curLog;
	
	public TestingLog(boolean malicious) {
		this.malicious=malicious;
		Attempts = 0;
		Success = 0;
		transactionAttempts = new ArrayList<>();
		viewUpdateLog = new ArrayList<>();
		curLog = new ViewUpdateLog();

	}
	
	public void logTransaction(boolean success, long timeTaken) {
		transactionAttempts.add(new TransactionLog(success,timeTaken));
		//if(malicious) {
			Attempts++;
			if(success) Success++;
		//}
	}
	
	public void logBlockValidation(long time, boolean success) {
		curLog.setValidateBlockTime(time);
		curLog.setValidationSuccessful(success);
	}
	
	public void logViewUpdate(long time, boolean success) {
		curLog.setTimeTaken(time);
		curLog.setHadMoreThanThreshold(success);
		viewUpdateLog.add(curLog);
		curLog = new ViewUpdateLog();
	}

	
	
	
	
	public boolean isMalicious() {
		return malicious;
	}

	public int getAttempts() {
		return Attempts;
	}

	public int getSuccess() {
		return Success;
	}

	public ArrayList<TransactionLog> getTransactionAttempts() {
		return transactionAttempts;
	}

	public ArrayList<ViewUpdateLog> getViewUpdateLog() {
		return viewUpdateLog;
	}


	@Override
	public int compareTo(TestingLog o) {
		if(o.isMalicious()==malicious) return 0;
		if(o.isMalicious()) return -1;
		if(malicious) return 1;
		return 0;
	}
}

class ViewUpdateLog implements Serializable, Comparable<ViewUpdateLog>{
	private static final long serialVersionUID = 1L;

	private boolean hadMoreThanThreshold;
	private boolean validationSuccessful;
	private long timeTaken;
	private long validateBlockTime;
	
	
	public ViewUpdateLog(boolean hadMoreThanThreshold, boolean validationSuccessful, long timeTaken,
			long validateBlockTime) {
		super();
		this.hadMoreThanThreshold = hadMoreThanThreshold;
		this.validationSuccessful = validationSuccessful;
		this.timeTaken = timeTaken;
		this.validateBlockTime = validateBlockTime;
	}

	public ViewUpdateLog() {
		super();
	}
	
	public boolean hadMoreThanThreshold() {
		return hadMoreThanThreshold;
	}

	public boolean isValidationSuccessful() {
		return validationSuccessful;
	}

	public long timeTaken() {
		return timeTaken;
	}
	
	public long validateBlockTimeTaken() {
		return this.validateBlockTime;
	}

	public void setHadMoreThanThreshold(boolean hadMoreThanThreshold) {
		this.hadMoreThanThreshold = hadMoreThanThreshold;
	}

	public void setValidationSuccessful(boolean validationSuccessful) {
		this.validationSuccessful = validationSuccessful;
	}

	public void setTimeTaken(long timeTaken) {
		this.timeTaken = timeTaken;
	}

	public void setValidateBlockTime(long validateBlockTime) {
		this.validateBlockTime = validateBlockTime;
	}

	@Override
	public int compareTo(ViewUpdateLog o) {
		if(hadMoreThanThreshold == o.hadMoreThanThreshold) {
			if(validationSuccessful == o.validationSuccessful) return 0;
			if(validationSuccessful) return -1;
			else return 1;
		}
		if(hadMoreThanThreshold) return -1;
		else return 1;
	}
	public String toString() {
		return timeTaken()+","+hadMoreThanThreshold+","+validateBlockTimeTaken()+","+validationSuccessful;
	}
}


class TransactionLog implements Serializable, Comparable<TransactionLog>{
	private static final long serialVersionUID = 1L;
	
	private boolean success;
	private long timeTaken;
	
	public TransactionLog(boolean success, long timeTaken) {
		this.success=success;
		this.timeTaken=timeTaken;
	}
	
	public boolean isSuccessful() {
		return success;
	}
	
	public long timeTaken() {
		return timeTaken;
	}

	@Override
	public int compareTo(TransactionLog o) {
		if(o.isSuccessful()==isSuccessful()) return 0;
		if(isSuccessful()) return -1;
		return 1;
	}
	
	public String toString() {
		return timeTaken+","+success;
	}
	
	
}