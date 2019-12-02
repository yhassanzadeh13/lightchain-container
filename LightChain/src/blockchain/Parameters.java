package blockchain;


public class Parameters {
	
	// default values
	private int ValidationFees = 1;
	private int SignaturesThreshold = 5;
	private int Alpha = 12;
	private int Levels = 30;
	private int InitialBalance = 20;
	private int TxMin = 4;
	private boolean Mode = true;
	
	public Parameters() {
		
	}

	public int getValidationFees() {
		return ValidationFees;
	}

	public void setValidationFees(int validationFees) {
		ValidationFees = validationFees;
	}

	public int getSignaturesThreshold() {
		return SignaturesThreshold;
	}

	public void setSignaturesThreshold(int signaturesThreshold) {
		SignaturesThreshold = signaturesThreshold;
	}

	public int getAlpha() {
		return Alpha;
	}

	public void setAlpha(int alpha) {
		Alpha = alpha;
	}

	public int getLevels() {
		return Levels;
	}

	public void setLevels(int levels) {
		Levels = levels;
	}

	public int getInitialBalance() {
		return InitialBalance;
	}

	public void setInitialBalance(int initialBalance) {
		InitialBalance = initialBalance;
	}

	public int getTxMin() {
		return TxMin;
	}

	public void setTxMin(int txMin) {
		TxMin = txMin;
	}

	public boolean isMode() {
		return Mode;
	}

	public void setMode(boolean mode) {
		Mode = mode;
	}
	
	
	
	
}
