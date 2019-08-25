package signature;

import java.io.Serializable;

/*
 * A wrapper class for a resultant signed Byte array from the Digital Signature class
 */
public class SignedBytes implements Serializable{
	private static final long serialVersionUID = 1L;

	private byte[] array;
	
	public SignedBytes(byte[] array) {
		this.array = array.clone();
	}
	
	public byte[] getBytes() {
		return array;
	}
}
