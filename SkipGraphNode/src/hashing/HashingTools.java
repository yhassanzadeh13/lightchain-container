package hashing;

import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class HashingTools implements Hasher,Serializable {
	/*
	 * SHA-256 Hashing function, will be used in the future for generating name IDs.
	 */
	public String getHash(String input, int neededSize) { //Returns a binary hash of input with required size
		if(neededSize>256 || neededSize<0) {
			System.out.println("The input size to the getHash function is out of bounds. Returning full hash.");
			return getHash(input);
		}
		return getHash(input).substring(0, neededSize);
	}
	
	public String getHash(byte[] input, int neededSize) {
		if(neededSize>256 || neededSize<0) {
			System.out.println("The input size to the getHash function is out of bounds. Returning full hash.");
			return getHash(input);
		}
		
		return getHash(input).substring(0, neededSize);
	}
	
	public String getHash(String input) { //Returns the SHA-256 hash of the input String
		byte[] inputBytes = input.getBytes(StandardCharsets.UTF_8);
		
		return getHash(inputBytes);
	}
	
	public String getHash(byte[] input) { //Returns the SHA-256 hash of the input array of bytes
		MessageDigest dig; 
		try {
			dig = MessageDigest.getInstance("SHA-256"); //Using JAVA's built in SHA-256 implementation
		} catch (NoSuchAlgorithmException e) {
			System.out.println("Failed to get SHA-256 Hash Function.");
			return null;
		}
		byte[] encodedhash = dig.digest(input);
		return bytesToBinary(encodedhash);
	}
	
	private String bytesToBinary(byte[] hash) {//Converts an array of bytes to the corresponding binary representation.
	    StringBuffer binString = new StringBuffer(); // To speed things up considering java String class being immutable.
	    for (int i = 0; i < hash.length; i++) { 
	    	
	    	//Getting the binary representation of each byte and padding it from the left with zeroes to make it 8 bits
	    	String bin = String.format("%8s", Integer.toBinaryString(((1<<8)-1) & hash[i])).replace(" ", "0"); 
	    	
	    	binString.append(bin);
	    }
	    return binString.toString();
	}

}
