package hashing;

public interface Hasher {
	public String getHash(String input, int neededSize);
	public String getHash(byte[] input, int neededSize);
	public String getHash(String input);;
	public String getHash(byte[] input);
}
