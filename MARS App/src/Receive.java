
public class Receive implements Encryptable {

	private int[] unExpandedKey, key;

	public Receive() {
		this(new int[] { 0, 0, 0, 0 });
	}

	public Receive(int[] key) {
		setKey(key);
	}

	public static int getNumBits(int num) {
		int a = num;
		int count = 0;
		while (a > 0) {
			a = a / 2;
			count++;
		}
		return count;
	}

	public static int Mask(int keywae) {
		System.out.println(Integer.toBinaryString(keywae));
		int counter = 1;
		int[] mask = new int[32];
		boolean s = false;
		for (int k = 1; k < 32; k++) {
			int tempo = getBit(keywae, k - 1);
			while (k < 32 && getBit(keywae, k) == tempo) {
				counter++;
				k++;
				// System.out.println("k: " + k);
				s = true;
			}
			if (s) {
				k--;
			}
			if (counter >= 10) {
				for (int a = 0; a < counter; a++) {
					// System.out.println("counter: " + counter);
					// System.out.println("set: " + (k - a));
					mask[k - a] = 1;
				}
			}
			s = false;
			counter = 1;
		}
		// System.out.println(Arrays.toString(mask));
		for (int k = 1; k < 32; k++) {
			if (getBit(keywae, k) != getBit(keywae, k - 1)) {
				mask[k] = 0;
				mask[k - 1] = 0;
			}
		}
		int Mask = 0;
		for (int k = 31; k >= 0; k--) {
			Mask += mask[31 - k] << k;
		}
		// System.out.println(Mask);
		return Mask;
	}

	public static int[] Masker(int[] keyEnter) {
		int[] key = keyEnter;
		for (int i = 5; i < 37; i += 2) {
			int j = key[i] & 3;
			int w = key[i] | 3;
			int Mask = Mask(w);
			// System.out.println(Mask);
			int p = s_box[265 + j];
			int lastBitsOfKey = mod(key[i - 1], 0x00000020);
			int firstBitsOfP = p / (1 << (32 - lastBitsOfKey));
			p <<= lastBitsOfKey;
			p += firstBitsOfP;

			key[i] = w ^ (p & Mask);
		}
		return key;
	}

	public static int rotateRight(int num, int amount) {
		return rotateLeft(num, 32 - amount);
	}

	public static int rotateLeft(int num, int amount) {
		int answer = mod(num, 1 << amount);
		int firstBits = num / 1 << (32 - amount);
		answer <<= amount;
		answer += firstBits;
		return answer;
	}

	public static int getBit(int num, int index) {
		return (num >>> (31 - index)) % 2;
		// return mod((num >>> (getNumBits(num) - index - 1)), 2);
	}

	public static int getFirstnBits(int num, int n) {
		return num >>> (32 - n);
	}

	public static int getLastnBits(int num, int n) {
		return mod(num, 1 << n);
	}

	public static int mod(int num, int mod) {
		return ((num % mod) + mod) % mod;
	}

	public static int[] efunction(int in, int key1, int key2) {
		int[] answer = new int[3];
		int M = in + key1;
		// int firstBits = in / 0x0080000;
		int R = (rotateLeft(in, 13)) * key2;
		int i = getLastnBits(M, 9);
		int L = s_box[i];
		R = rotateLeft(R, 5);
		int r = getLastnBits(R, 5);
		M = rotateLeft(M, r);
		L ^= R;
		R = rotateLeft(R, 5);
		L ^= R;
		r = getLastnBits(R, 5);
		L = rotateLeft(L, r);
		answer[0] = L;
		answer[1] = M;
		answer[2] = R;
		return answer;
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
				int temporary = temp[mod(i - 7, 15)] ^ temp[mod(i - 2, 15)];
				int firstBits = temporary / 0x20000000;
				temporary <<= 3;
				temporary += firstBits;
				temp[i] ^= (temporary) ^ (4 * i + j);

			}
			for (int k = 0; k < 4; k++) {
				for (int i = 0; i < 15; i++) {
					temp[i] += s_box[mod(temp[mod(i - 1, 15)], (1 << 9))];
					int firstBits = temp[i] / 0x00800000;
					temp[i] <<= 9;
					temp[i] += firstBits;
				}
			}
			for (int i = 0; i < 10; i++) {
				key[10 * j + i] = temp[mod(4 * i, 15)];
			}

		}
		key = Masker(key);
		// for (int i = 5; i < 37; i += 2) {
		// int j = key[i] & 3;
		// int w = key[i] | 3;
		// int counter = 0;
		// int[] mask = new int[32];
		// int tempo = getBit(w, 0);
		// for (int k = 1; k < 32; k++) {
		// while (getBit(w, k) == tempo) {
		// counter++;
		// k++;
		// }
		// if (counter >= 9) {
		// for (int a = 0; a < counter; a++) {
		// mask[k - a - 1] = 1;
		// }
		// }
		// counter = 0;
		// }
		// for (int k = 1; k < 32; k++) {
		// if (tempo != getBit(w, k)) {
		// mask[k] = 0;
		// mask[k - 1] = 0;
		// }
		// }
		// int Mask = 0;
		// for (int k = 31; k >= 0; k--) {
		// Mask += mask[k] << k;
		// }
		//
		// int p = s_box[265 + j];
		// int lastBitsOfKey = mod(key[i - 1], 0x00000020);
		// int firstBitsOfP = p / (1 << (32 - lastBitsOfKey));
		// p <<= lastBitsOfKey;
		// p += firstBitsOfP;
		//
		// key[i] = w ^ (p & Mask);
		// }

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
			lastByte = mod(output[0], 256);
			output[1] ^= s_box[lastByte];
			output[0] >>= 8;
			output[0] += 0x01000000 * lastByte;

			lastByte = mod(output[0], 256);
			output[1] += s_box[256 + lastByte];
			output[0] >>= 8;
			output[0] += 0x01000000 * lastByte;

			lastByte = mod(output[0], 256);
			output[2] += s_box[lastByte];
			output[0] >>= 8;
			output[0] += 0x01000000 * lastByte;

			lastByte = mod(output[0], 256);
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
			outs[2] = mod(((ins[0] << 13 + firstBits) * ins[2]), (1 << 32));
			outs[0] = s_box[mod(output[1], 0x00000200)];

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

			firstByte = output[0] >>> 24;
			lastByte = mod(output[0], 256);
			output[1] ^= s_box[256 + lastByte];
			output[0] <<= 8;
			output[0] += firstByte;

			firstByte = output[0] >>> 24;
			lastByte = mod(output[0], 256);
			output[2] -= s_box[lastByte];
			output[0] <<= 8;
			output[0] += firstByte;

			firstByte = output[0] >>> 24;
			lastByte = mod(output[0], 256);
			output[3] -= s_box[256 + lastByte];
			output[0] <<= 8;
			output[0] += firstByte;

			lastByte = mod(output[0], 256);
			output[3] ^= s_box[lastByte];

			int temp = output[0];
			for (int k = 1; k < 4; k++) {
				output[k - 1] = output[k];
			}
			output[3] = temp;
		}
		for (int i = 0; i < 4; i++) {
			output[i] -= key[36 + i];
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
			for (int k = 3; k > 0; k--) {
				output[k] = output[k - 1];
			}
			output[0] = temp;

			byteOne = mod(output[0], 256);
			output[0] >>= 8;
			output[0] += 0x01000000 * byteOne;

			byteTwo = mod(output[0], 256);
			output[0] >>= 8;
			output[0] += 0x01000000 * byteTwo;

			byteThree = mod(output[0], 256);
			output[0] >>= 8;
			output[0] += 0x01000000 * byteThree;

			// output[3] ^= s_box[byteTwo];
			// output[3] += s_box[256 + byteThree];
			// output[2] += s_box[mod(output[0], 256)];
			// output[1] ^= s_box[256 + byteOne];
			output[3] ^= s_box[byteTwo];
			output[3] += s_box[byteOne + 256];
			output[2] += s_box[byteThree];
			output[1] ^= s_box[256 + mod(output[0], 256)];
			if (i == 2 || i == 6)
				output[0] += output[3];
			if (i == 3 || i == 7)
				output[0] += output[1];
		}

		// cryptographic core
		for (int i = 15; i >= 0; i--) {
			int temp = output[3];
			for (int k = 3; k > 0; k--) {
				output[k] = output[k - 1];
			}
			output[0] = temp;
			output[0] = temp;

			int lastBits = mod(output[0], 0x00002000);
			output[0] >>= 13;
			output[0] += 0x00080000 * lastBits;

			// e-function
			// int[] outs = new int[3];
			// int[] ins = new int[] { output[0], key[2 * i + 4], key[2 * i + 5]
			// };
			//
			// outs[1] = ins[0] + ins[1];
			// int firstBits = ins[0] / 0x00080000;
			// outs[2] = mod(((ins[0] << 13 + firstBits) * ins[2]), (1 << 32));
			// outs[0] = s_box[mod(output[1], 0x00000200)];
			//
			// firstBits = outs[2] / 0x08000000;
			// outs[2] <<= 5;
			// outs[2] += firstBits;
			//
			// int tempo = outs[1] / (1 << (32 - firstBits));
			// outs[1] <<= firstBits;
			// outs[1] += tempo;
			//
			// outs[0] ^= outs[2];
			//
			// firstBits = outs[2] / 0x08000000;
			// outs[2] <<= 5;
			// outs[2] += firstBits;
			//
			// outs[0] ^= outs[2];
			//
			// tempo = outs[0] / (1 << (32 - firstBits));
			// outs[0] <<= firstBits;
			// outs[0] += tempo;

			// end e-function
			int[] outs = efunction(output[0], key[2 * i + 4], key[2 * i + 5]);
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
			for (int k = 3; k > 0; k--) {
				output[k] = output[k - 1];
			}
			output[0] = temp;

			if (i == 0 || i == 4)
				output[0] -= output[3];
			if (i == 1 || i == 5)
				output[0] -= output[1];

			byteOne = output[0] >>> 24;
			output[3] ^= s_box[256 + byteOne];
			output[0] <<= 8;
			output[0] += byteOne;

			byteOne = output[0] >>> 24;
			output[1] -= s_box[byteOne + 256];
			output[0] <<= 8;
			output[0] += byteOne;

			byteOne = output[0] >>> 24;
			output[2] -= s_box[byteOne];
			output[0] <<= 8;
			output[0] += byteOne;

			byteOne = output[0] >>> 24;
			output[1] ^= s_box[byteOne];
		}

		for (int i = 0; i < 4; i++) {
			output[i] -= key[i];
		}

		return output;
	}
}
