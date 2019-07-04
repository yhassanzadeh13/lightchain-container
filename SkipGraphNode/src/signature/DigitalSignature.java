package signature;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Scanner;

public class DigitalSignature {
	
	private static String genAlgorithm = "RSA";
	private static String signAlgorithm = "SHA256withRSA";
		 
	private PrivateKey privateKey;
	private PublicKey publicKey;
	private String privateKeyName;
	private String publicKeyName;
	private String keysPath;
	private static Scanner in = new Scanner(System.in);
	private ArrayList<String> otherPubKeys;
	
	
	/*
	 * A constructor for the digital signature
	 * Once we declare a digital signature for a node
	 * we generate a public-private key pair for the node given a certain algorithm
	 */
	public DigitalSignature() {
		try {
			KeyPairGenerator gen = KeyPairGenerator.getInstance(genAlgorithm);
			gen.initialize(2048);
			KeyPair keyPair = gen.generateKeyPair();
			privateKey = keyPair.getPrivate();
			publicKey = keyPair.getPublic();
			otherPubKeys = new ArrayList<>();
			log("Enter the name of the private key.");
			privateKeyName = get();
			log("Enter the name of the public key.");
			publicKeyName = get();
			//log("Enter the storage path of the key pair.");
			keysPath = "C:\\Users\\USER\\Documents\\Research\\TestKeys";
			storeKeyPair();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
	}
	
	/*
	 * A method that stores the public and private key on disk.
	 */
	private void storeKeyPair() {
		
		try {
			FileOutputStream out = new FileOutputStream(keysPath + "\\" + privateKeyName + ".key");
			out.write(privateKey.getEncoded());
			out.close();
			out = new FileOutputStream(keysPath + "\\" + publicKeyName + ".key");
			out.write(publicKey.getEncoded());
			out.close();
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/*
	 * A method that loads public and private key from disk 
	 */
	public KeyPair loadKeyPair(String path, String algorithm) {
		
		try {
			Path p = Paths.get(keysPath + "\\" + publicKeyName + ".key");
			byte[] bytes;
			bytes = Files.readAllBytes(p);
			X509EncodedKeySpec pub = new X509EncodedKeySpec(bytes);
			KeyFactory kf = KeyFactory.getInstance(algorithm);
			PublicKey pb = kf.generatePublic(pub);
			
			p = Paths.get(keysPath + "\\" + privateKeyName + ".key");
			bytes = Files.readAllBytes(p);
			PKCS8EncodedKeySpec pri = new PKCS8EncodedKeySpec(bytes);
			KeyFactory kk = KeyFactory.getInstance(algorithm);
			PrivateKey pr = kk.generatePrivate(pri);
			return new KeyPair(pb,pr);
				
		} catch (IOException e) {
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (InvalidKeySpecException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/*
	 * A method to store a given public key on disk
	 */
	public void storeKey(String path, String name, PublicKey key) {
		otherPubKeys.add(path + "\\" + name + ".key");
		try {
			FileOutputStream out = new FileOutputStream(path + "\\" + name + ".key");
			out.write(key.getEncoded());
			out.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/*
	 * A method to lead a public key that is stored on disk
	 */
	public PublicKey loadKey(String path, String name, String algorithm) {
		Path p = Paths.get(path + "\\" + name + ".key");
		byte[] bytes;
		try {
			bytes = Files.readAllBytes(p);
			X509EncodedKeySpec pub = new X509EncodedKeySpec(bytes);
			KeyFactory kf = KeyFactory.getInstance(algorithm);
			PublicKey pb = kf.generatePublic(pub);
			return pb;
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (InvalidKeySpecException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/*
	 * This method takes a string, signs it, and then returns the signed string
	 */
	public String signString(String text) {
		
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
	public boolean signFile(String filePath) {
		
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
	public boolean verifyString(String data, String signedData, PublicKey pKey) {
			
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
	public boolean verifyFile(String filePath, String signedPath, PublicKey pKey) {
		
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
	public static void log(String s) {
		System.out.println(s);
	}
	public static String get() {
		String res = in.nextLine();
		return res;
	}
}
