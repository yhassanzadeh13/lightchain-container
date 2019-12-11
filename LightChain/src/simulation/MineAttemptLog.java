package simulation;

import java.io.Serializable;

public class MineAttemptLog implements Serializable {
	private static final long serialVersionUID = 1L;

	private boolean foundTxMin;
	private boolean success;
	private long totalTime;
	private long validationTime;

	public MineAttemptLog(boolean foundTxMin, boolean success, long totalTime, long validationTime) {
		super();
		this.foundTxMin = foundTxMin;
		this.success = success;
		this.totalTime = totalTime;
		this.validationTime = validationTime;
	}

	public MineAttemptLog() {
		super();
	}

	public boolean isFoundTxMin() {
		return foundTxMin;
	}

	public boolean isSuccessful() {
		return success;
	}

	public long getTotalTime() {
		return totalTime;
	}

	public long getValidationTime() {
		return validationTime;
	}

	
	public String toString() {
		return totalTime + "," + foundTxMin + "," + validationTime + "," + success;
	}
}
