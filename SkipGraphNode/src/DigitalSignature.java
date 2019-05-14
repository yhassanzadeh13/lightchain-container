import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;

public class DigitalSignature {
	
	// investigate using SecureRandom
	 
	private PrivateKey privateKey;
	private PublicKey publicKey;
	
	/*
	 * A constructor for the digital signature
	 * Once we declare a digital signature for a node
	 * we generate a public-private key pair for the node given a certain algorithm
	 */
	public DigitalSignature(String genAlgorithm, String signAlgorithm) {
		try {
			KeyPairGenerator gen = KeyPairGenerator.getInstance(genAlgorithm);
			gen.initialize(2048);
			KeyPair keyPair = gen.generateKeyPair();
			privateKey = keyPair.getPrivate();
			publicKey = keyPair.getPublic();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
	}
	
	/*
	 * This method takes a string, signs it, and then returns the signed string
	 */
	public String signString(String text, String signAlgorithm) {
		
		Signature signature;
		try {	
			signature = Signature.getInstance(signAlgorithm);
			signature.initSign(privateKey);
			byte[] data = text.getBytes();
			signature.update(data);
			byte[] signed = signature.sign();
			return new String(signed);
			
		} catch (Exception e) {
			log("Exception caught: " + e.toString());
			e.printStackTrace();
			return null;
		}
	}
	/*
	 * This method takes a path of a file, signs it, and then generates the signed file
	 * in the same path of the of the given file
	 * and returns true if the signing operation was successful and false otherwise
	 */
	public boolean signFile(String filePath, String signAlgorithm) {
		
		Signature signature;
		try {
			signature = Signature.getInstance(signAlgorithm);
			signature.initSign(privateKey);
			
			Path path = FileSystems.getDefault().getPath(filePath);
			byte[] data = Files.readAllBytes(path);
			signature.update(data);
			byte[] signed = signature.sign();
			
			FileOutputStream signedFile = new FileOutputStream(filePath+"_Signature");
			signedFile.write(signed);
			signedFile.close();
			return true;
			
		}catch(Exception e) {
			log("Exception caught: " + e.toString());
			e.printStackTrace();
			return false;
		} 
	}
	
	/* 
	 * This methods receives a string and the signed data and verifies it
	 * It returns true if the operation is successful and false otherwise
	 */
	public boolean verifyString(String data, String signedData,String signAlgorithm, PublicKey pKey) {
			
		Signature signature ;
		try {
			signature = Signature.getInstance(signAlgorithm);
			signature.initVerify(pKey);
			signature.update(data.getBytes());
			boolean verification = signature.verify(signedData.getBytes());
			return verification;
		}catch(Exception e) {
			log("Exception caught: " + e.toString());
			e.printStackTrace();
			return false;
		}
	}
	
	/*
	 * This method receives that paths of a data file and a signed file and verifies the data file
	 * It return true if the operation is successful and false otherise.
	 */
	public boolean verifyFile(String filePath, String signedPath,String signAlgorithm, PublicKey pKey) {
		
		Signature signature;
		try {
			FileInputStream input = new FileInputStream(signedPath);
			byte[] data = new byte[input.available()];
			input.read(data);
			input.close();
			signature = Signature.getInstance(signAlgorithm);
			signature.initVerify(pKey);
			FileInputStream dataFile = new FileInputStream(filePath);
			BufferedInputStream dataBuffered = new BufferedInputStream(dataFile);
			byte[] buffer = new byte[1024];
			int len ;
			while(dataBuffered.available() != 0) {
				len = dataBuffered.read(buffer);
				signature.update(buffer,0,len);
			}
			dataBuffered.close();
			boolean verification = signature.verify(data);
			return verification;
		}catch(Exception e) {
			log("Exception caught: " + e.toString());
			e.printStackTrace();
			return false;
		}
	}
	
	public PublicKey getPublicKey() {
		return publicKey;
	}
	public void log(String s) {
		System.out.println(s);
	}
}
