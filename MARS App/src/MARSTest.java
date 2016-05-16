import static java.lang.System.out;

import java.util.Arrays;

public class MARSTest {

	public static void main(String[] args) {
		Transmit personOne = new Transmit(new int[] { 0x0509F700, 0x001159F0, 0x6F86FF23, 0x090CD70D });
		Receive personTwo = new Receive(new int[] { 0x0509A745, 0x3946C1D2, 0x00033870, 0x51BB8583 });
		int[] firstEncryption = personOne.encrypt(new int[] { 1, 2, 3, 4 });
		int[] secondEncryption = personTwo.encrypt(firstEncryption);
		int[] firstDecryption = personOne.decrypt(secondEncryption);
		int[] finalData = personTwo.decrypt(firstDecryption);
		out.println(Arrays.toString(finalData));
	}

}
