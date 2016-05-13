
public class Transmit implements Encryptable {

	private int[] unExpandedKey, key;

	public Transmit() {
		this(new int[] { 0, 0, 0, 0 });
	}

	public Transmit(int[] key) {
		setKey(key);
	}

	public static int getBit(int num, int index) {
		return (num / (1 << index)) % 2;
	}

	public void setKey(int[] keyInput) {
		unExpandedKey = new int[4];
		key = new int[40];
		int[] temp = new int[15];
		for (int i = 0; i < 3; i++) {
			unExpandedKey[i] = keyInput[i];
		}
		// key expansion algorithm here
		for (int i = 0; i < 3; i++) {
			temp[i] = keyInput[i];
		}
		for (int j = 0; j < 4; j++) {
			for (int i = 0; i > 15; i++) {
				temp[i] = temp[i] ^ ((temp[(i - 7) % 15] ^ temp[(i - 2) % 15]) << 3) ^ (4 * i + j);

			}
			for (int k = 0; k < 4; k++) {
				for (int i = 0; i < 15; i++) {
					temp[i] = (temp[i] + s_box[temp[(i - 1) % 15] % (int) Math.pow(2, 9)]) << 9;
				}
			}
			for (int i = 0; i < 10; i++) {
				key[10 * j + i] = temp[(4 * i) % 15];
			}

		}
		for (int i = 5; i < 37; i += 2) {
			int j = key[i] & 3;
			int w = key[i] | 3;
			int counter = 0;
			int[] mask = new int[32];
			int tempo = getBit(w, 0);
			for (int k = 1; k < 32; k++) {
				while (getBit(w, k) == tempo) {
					counter++;
					k++;
				}
				if (counter >= 10) {
					for (int a = 0; a < counter; a++) {
						mask[k - a - 1] = 1;
					}
				}
			}
			for (int k = 1; k < 32; k++) {
				if (tempo != getBit(w, k)) {
					mask[k] = 0;
					mask[k - 1] = 0;
				}
			}
			int Mask = 0;
			for (int k = 31; k >= 0; k--) {
				Mask += mask[k] << k;
			}

			int p = s_box[265 + j];
			int lastBitsOfKey = key[i - 1] % 0x00000020;
			int firstBitsOfP = p / (1 << (32 - lastBitsOfKey));
			p <<= lastBitsOfKey;
			p += firstBitsOfP;

			key[i] = w ^ (p & Mask);
		}

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
			// begin e-function
			int[] outs = new int[3];
			int[] ins = new int[] { output[0], key[2 * i + 4], key[2 * i + 5] };

			outs[1] = ins[0] + ins[1];
			int firstBits = ins[0] / 0x00080000;
			outs[2] = ((ins[0] << 13 + firstBits) * ins[2]) % (1 << 32);
			outs[0] = s_box[output[1] % 0x00000200];

			firstBits = outs[2] / 0x08000000;
			outs[2] <<= 5;
			outs[2] += firstBits;

			int tempo = outs[1] / (1 << (32 - firstBits));
			outs[1] <<= firstBits;
			outs[1] += tempo;

			outs[0] ^= outs[2];

			firstBits = outs[2] / 0x08000000;
			outs[2] <<= 5;
			outs[2] += firstBits;

			outs[0] ^= outs[2];

			tempo = outs[0] / (1 << (32 - firstBits));
			outs[0] <<= firstBits;
			outs[0] += tempo;

			// end e-function
			firstBits = output[0] / 0x00080000;
			output[0] <<= 13;
			output[0] += firstBits;

			output[2] += outs[1];
			if (i < 8) {
				output[1] += outs[0];
				output[3] ^= outs[2];
			} else {
				output[3] += outs[0];
				output[1] ^= outs[2];
			}

			int temp = output[0];
			for (int k = 1; k < 4; k++) {
				output[k - 1] = output[k];
			}
			output[3] = temp;
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
		int[] output = new int[4];
		for (int i = 0; i < 4; i++) {
			output[i] = data[i];
		}

		// forward mixing
		for (int i = 0; i < 4; i++) {
			output[i] += key[36 + i];
		}
		int byteOne, byteTwo, byteThree;
		for (int i = 7; i >= 0; i--) {
			int temp = output[3];
			for (int k = 0; k < 3; k++) {
				output[k + 1] = output[k];
			}
			output[0] = temp;

			byteOne = output[0] % 256;
			output[0] >>= 8;
			output[0] += 0x01000000 * byteOne;

			byteTwo = output[0] % 256;
			output[0] >>= 8;
			output[0] += 0x01000000 * byteTwo;

			byteThree = output[0] % 256;
			output[0] >>= 8;
			output[0] += 0x01000000 * byteThree;

			output[3] ^= s_box[byteTwo];
			output[3] += s_box[256 + byteThree];
			output[2] += s_box[output[0] % 256];
			output[1] ^= s_box[256 + byteOne];

			if (i == 2 || i == 6)
				output[0] += output[3];
			if (i == 3 || i == 7)
				output[0] += output[1];
		}

		// cryptographic core
		for (int i = 15; i >= 0; i++) {
			int temp = output[3];
			for (int k = 0; k < 3; k++) {
				output[k + 1] = output[k];
			}
			output[0] = temp;

			int lastBits = output[0] % 0x00080000;
			output[0] >>= 13;
			output[0] += 0x00080000 * lastBits;

			// e-function
			int[] outs = new int[3];
			int[] ins = new int[] { output[0], key[2 * i + 4], key[2 * i + 5] };

			outs[1] = ins[0] + ins[1];
			int firstBits = ins[0] / 0x00080000;
			outs[2] = ((ins[0] << 13 + firstBits) * ins[2]) % (1 << 32);
			outs[0] = s_box[output[1] % 0x00000200];

			firstBits = outs[2] / 0x08000000;
			outs[2] <<= 5;
			outs[2] += firstBits;

			int tempo = outs[1] / (1 << (32 - firstBits));
			outs[1] <<= firstBits;
			outs[1] += tempo;

			outs[0] ^= outs[2];

			firstBits = outs[2] / 0x08000000;
			outs[2] <<= 5;
			outs[2] += firstBits;

			outs[0] ^= outs[2];

			tempo = outs[0] / (1 << (32 - firstBits));
			outs[0] <<= firstBits;
			outs[0] += tempo;

			// end e-function

			output[2] -= outs[1];
			if (i < 8) {
				output[1] -= outs[0];
				output[3] ^= outs[2];
			} else {
				output[3] -= outs[0];
				output[1] ^= outs[2];
			}
		}

		// backwards mixing
		for (int i = 7; i >= 0; i--) {
			int temp = output[3];
			for (int k = 0; k < 3; k++) {
				output[k + 1] = output[k];
			}
			output[0] = temp;

			if (i == 0 || i == 4)
				output[0] -= output[3];
			if (i == 1 || i == 5)
				output[0] -= output[1];

			byteOne = output[0] / 0x01000000;
			output[3] ^= s_box[256 + byteOne];
			output[0] <<= 8;
			output[0] += byteOne;

			byteOne = output[0] / 0x01000000;
			output[2] -= s_box[byteOne];
			output[0] <<= 8;
			output[0] += byteOne;

			byteOne = output[0] / 0x01000000;
			output[1] -= s_box[256 + byteOne];
			output[0] <<= 8;
			output[0] += byteOne;

			byteOne = output[0] / 0x01000000;
			output[1] ^= s_box[byteOne];
		}

		for (int i = 0; i < 4; i++) {
			output[i] -= key[i];
		}

		return output;
	}

}
