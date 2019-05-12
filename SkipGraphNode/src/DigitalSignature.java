import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;

public class DigitalSignature {
	
	// investigate using SecureRandom
	private PrivateKey privateKey;
	private PublicKey publicKey;
	
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

	public PublicKey getPublicKey() {
		return publicKey;
	}
}
