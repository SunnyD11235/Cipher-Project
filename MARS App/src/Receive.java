
public class Receive implements Encryptable {

	private int[] key;

	public Receive() {
		this(new int[] { 0, 0, 0, 0 });
	}

	public Receive(int[] key) {
		setKey(key);
	}

	public void setKey(int[] key) {
		this.key = key;
	}

	@Override
	public int[] encrypt(int[] data) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int[] decrypt(int[] data) {
		// TODO Auto-generated method stub
		return null;
	}
}
