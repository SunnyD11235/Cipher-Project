
public class Transmit implements Encryptable {

	private int[] key;

	public Transmit() {
		this(new int[] { 0, 0, 0, 0 });
	}

	public Transmit(int[] key) {
		setKey(key);
	}

	public void setKey(int[] key) {
		this.key = key;
	}

	@Override
	/**
	 * @Precondition data is an array of size 4.
	 */
	public int[] encrypt(int[] data) {
		int[] output = new int[4];
		for (int i = 0; i < 4; i++) {
			output[i] = data[i];
		}

		// forward mixing
		for (int i = 0; i < 4; i++) {
			output[i] += key[i];
		}
		int lastByte;
		for (int i = 0; i < 8; i++) {
			lastByte = output[0] % 256;
			output[1] ^= s_box[lastByte];
			output[0] >>= 8;
			output[0] += 0x01000000 * lastByte;

			lastByte = output[0] % 256;
			output[1] += s_box[256 + lastByte];
			output[0] >>= 8;
			output[0] += 0x01000000 * lastByte;

			lastByte = output[0] % 256;
			output[2] += s_box[lastByte];
			output[0] >>= 8;
			output[0] += 0x01000000 * lastByte;

			lastByte = output[0] % 256;
			output[3] ^= s_box[256 + lastByte];

			if (i == 0 || i == 4)
				output[0] += output[3];
			if (i == 1 || i == 5)
				output[0] += output[1];

			int temp = output[0];
			for (int k = 1; k < 4; k++) {
				output[k - 1] = output[k];
			}
			output[3] = temp;
		}

		// crytographic core
		for (int i = 0; i < 16; i++) {

		}

		// backwards mixing
		int firstByte;
		for (int i = 0; i < 8; i++) {
			if (i == 2 || i == 6)
				output[0] -= output[3];
			if (i == 3 || i == 7)
				output[0] -= output[1];

			firstByte = output[0] / 0x01000000;
			lastByte = output[0] % 256;
			output[1] ^= s_box[256 + lastByte];
			output[0] <<= 8;
			output[0] += firstByte;

			firstByte = output[0] / 0x01000000;
			lastByte = output[0] % 256;
			output[2] -= s_box[lastByte];
			output[0] <<= 8;
			output[0] += firstByte;

			firstByte = output[0] / 0x01000000;
			lastByte = output[0] % 256;
			output[3] -= s_box[256 + lastByte];
			output[0] <<= 8;
			output[0] += firstByte;

			lastByte = output[0] % 256;
			output[3] ^= s_box[lastByte];

			int temp = output[0];
			for (int k = 1; k < 4; k++) {
				output[k - 1] = output[k];
			}
			output[3] = temp;
		}
		for (int i = 0; i < 4; i++) {
			output[i] -= key[i];
		}

		return output;
	}

	@Override
	public int[] decrypt(int[] data) {
		// TODO Auto-generated method stub
		return null;
	}

}
