package signature;

import java.io.Serializable;

/*
 * A wrapper class for a resultant signed Byte array from the Digital Signature class
 */
public class SignedBytes implements Serializable {
	private static final long serialVersionUID = 1L;

	private byte[] array;

	// For testing
	private boolean isAuthenticated;
	private boolean isSound;
	private boolean isCorrect;
	private boolean hasBalance;

	public SignedBytes(byte[] array) {
		if (array != null)
			this.array = array.clone();
	}

	// TODO: see if it is still necessary to store results of validation
	public SignedBytes(byte[] array, boolean isAuthenticated, boolean isSound, boolean isCorrect, boolean hasBalance) {
		if (array != null)
			this.array = array.clone();
		this.isAuthenticated = isAuthenticated;
		this.isSound = isSound;
		this.isCorrect = isCorrect;
		this.hasBalance = hasBalance;
	}

	public byte[] getBytes() {
		return array;
	}

	// for testing

	public boolean isAuth() {
		return isAuthenticated;
	}

	public boolean isSound() {
		return isSound;
	}

	public boolean isCorrect() {
		return isCorrect;
	}

	public boolean hasBalance() {
		return hasBalance;
	}

}
