import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;

public class ISA {
	static LinkedList<Integer> fetching = new LinkedList<Integer>();
	static LinkedList<Integer[]> decoding = new LinkedList<Integer[]>();
	static LinkedList<Integer> executing = new LinkedList<Integer>();
	int timer = 0;
	static byte[] dataMemory = new byte[2048];
	static byte[] regs = new byte[64];
	static short[] instructions = new short[1024];
	static short pc = 0;
	static byte SREG = 0b00000000;
	int n = 0;
	static boolean branch = false;

	public ISA() {
		for (int i = 0; i < regs.length; i++) {
			regs[i] = (byte) i;
		}
		for (int i = 0; i < dataMemory.length; i++) {
			dataMemory[i] = (byte) Math.floor(Math.random() * (127 + 1));
		}
	}

	public String toString(byte[] arr) {
		String s = "[ ";
		for (byte a : arr) {
			s += a + " ";
		}
		return s + " ]";
	}

	public String toString(short[] arr) {
		String s = "[ ";
		for (short a : arr) {
			s += a + " ";
		}
		return s + " ]";
	}

	public void run() throws IOException {
		parse();
		int j = 0;
		while (((3 + (n - 1)) != timer) && j < n + 2) {
			if (pc + 2 == n) {
				j = pc;
			}
			j++;
			System.out.println(j);
			System.out.println("-----CYCLE " + timer + "-----");
			System.out.println("Program Counter =" + pc);
			if (executing.isEmpty()) {
				if (decoding.isEmpty()) {
					if (fetching.isEmpty()) {
						fetch();
						System.out.println("Fetching Instruction:" + fetching.getLast() + "\n");
					} else {
						fetch();
						System.out.println("Fetching Instruction:" + fetching.getLast() + "\n");

						decoding.add(decode(fetching.removeFirst()));
						short dec = (short) (decoding.getLast()[0]  << 12| decoding.getLast()[1] << 6
								| decoding.getLast()[2]);
						System.out.println("Decoding Instruction:" + dec + "\n");
					}
				} else {						
					execute(decoding.removeFirst());
					if (branch == false) {

						fetch();
						System.out.println("Fetching Instruction:" + fetching.getLast() + "\n");

						decoding.add(ISA.decode(fetching.removeFirst()));
						short dec = (short) (decoding.getLast()[0] << 12| decoding.getLast()[1] << 6
								| decoding.getLast()[2]);
						System.out.println("Decoding Instruction:" + dec + "\n");
					} else {
						branch = false;
					}
				}
			} else {
				if (branch == false) {
					execute(decoding.removeFirst());
					fetch();
					decoding.add(decode(fetching.removeFirst()));
				} else {
					execute(decoding.removeFirst());
					branch = false;
				}
			}

			if (SREG == 0)
				System.out.println("Status Register: " + "00000000" + "\n");
			else if (SREG >>> 4 == 1)
				System.out.println("Status Register: " + "000" + Integer.toBinaryString(SREG) + "\n");
			else if (SREG >>> 3 == 1)
				System.out.println("Status Register: " + "0000" + Integer.toBinaryString(SREG) + "\n");
			else if (SREG >>> 2 == 1)
				System.out.println("Status Register: " + "00000" + Integer.toBinaryString(SREG) + "\n");
			else if (SREG >>> 1 == 1)
				System.out.println("Status Register: " + "000000" + Integer.toBinaryString(SREG) + "\n");
			else if (SREG == 1)
				System.out.println("Status Register: " + "0000000" + Integer.toBinaryString(SREG) + "\n");

			timer++;
			SREG = 0;

		}
		System.out.println("All purpose Registers: " + toString(regs));
		System.out.println("Instruction Memory: " + toString(instructions));
		System.out.println("Data Registers: " + toString(dataMemory));

	}

	public void parse() throws IOException {
		BufferedReader br = new BufferedReader(new FileReader("instructions"));
		String currentLine = br.readLine();
		int r1;
		int r2;
		int opcode = 0;
		short instruction = 0;
		while (currentLine != null) {

			String[] content = currentLine.split(" ");
			switch (content[0]) {
			case "ADD":
				opcode = 0b0000000000000000;
				break;
			case "SUB":
				opcode = 0b0001000000000000;
				break;
			case "MUL":
				opcode = 0b0010000000000000;
				break;
			case "MOVI":
				opcode = 0b0011000000000000;
				break;
			case "BEQZ":
				opcode = 0b0100000000000000;
				break;
			case "ANDI":
				opcode = 0b0101000000000000;
				break;
			case "EOR":
				opcode = 0b0110000000000000;
				break;
			case "BR":
				opcode = 0b0111000000000000;
				break;
			case "SAL":
				opcode = 0b1000000000000000;
				break;
			case "SAR":
				opcode = 0b1001000000000000;
				break;
			case "LDR":
				opcode = 0b1010000000000000;
				break;
			case "STR":
				opcode = 0b1011000000000000;
				break;

			}

			r1 = Integer.parseInt(content[1].substring(1));

			if (content[2].length() == 3 && content[2].charAt(0) == 'R') {
				r2 = Integer.parseInt(content[2].substring(1));

			} else if (content[2].length() == 2 && content[2].charAt(0) == 'R') {
				r2 = Integer.parseInt(content[2].substring(1));
			} else {
				r2 = Integer.parseInt(content[2]);
			}
			instruction = (short) (opcode | (r1 << 6) | r2);
			instructions[pc] = instruction;
			pc++;
			n++;
			currentLine = br.readLine();
		}
		pc = 0;
		System.out.println("Total Number of Instructions: " + n);
		br.close();
	}

	public void fetch() {

		int instruction;
		instruction = instructions[pc];
		System.out.println();
		fetching.add(instruction);
		if (instructions[pc + 1] != 0)
			pc++;

	}

	public static Integer[] decode(int instruction) {

		int opcode = (instruction & 0b1111000000000000) >>> 12;
		int r1 = (instruction & 0b0000111111000000) >>> 6;
		int r2 = (instruction & 0b0000000000111111);
		Integer[] inst = new Integer[3];
		inst[0] = opcode;
		inst[1] = r1;
		inst[2] = r2;

		return inst;

	}

	public static void execute(Integer[] inst) {

		int opcode = inst[0];
		int r1 = inst[1];
		int r2 = inst[2];

		int valueR1 = regs[r1];
		int valueR2 = regs[r2];
		int r1Sign = (valueR1 & 0b00000000000000000000000010000000) >>> 7;
		int r2valSign = (valueR2 & 0b00000000000000000000000010000000) >>> 7;
		int valueR1c = regs[r1];
		int valueR2c = regs[r2];

		switch (opcode) {
		case 0:
			System.out.println("Executing Instruction: ADD");
			System.out.println("Param 1: " + valueR1c + '\n' + "Param 2: " + valueR2c);
			valueR1 = (valueR1 + valueR2);
			int r1MSB = (valueR1 & 0b00000000000000000000000100000000) >>> 8;
			regs[r1] = (byte) valueR1;
			System.out.println("Result : " + (byte) valueR1);

			if (r1Sign == r2valSign && ((valueR1 & 0b00000000000000000000000010000000) >>> 7) != r2valSign) {
				SREG |= 0b00001000;
			} else {
				SREG &= 0b11110111;
			}
			if (r1MSB == 1) {
				SREG |= 0b00010000;
			} else {
				SREG &= 0b11101111;
			}

			if (valueR1 < 0)
				SREG |= 0b00000100;
			else
				SREG &= 0b11111011;

			if (valueR1 == 0)
				SREG |= 0b00000001;
			else
				SREG &= 0b11111110;

			int N = (SREG & 0b00000100) >>> 2;
			int V = (SREG & 0b00001000) >>> 2;
			int s = N ^ V;

			if (s == 1)
				SREG |= 0b00000010;
			else
				SREG &= 0b11111101;
			break;
		case 1:
			System.out.println("Executing Instruction: SUB");
			System.out.println("Param 1: " + valueR1c + '\n' + "Param 2: " + valueR2c);
			valueR1 -= valueR2;
			regs[r1] = (byte) valueR1;
			System.out.print("Result :" + valueR1);

			if (r1Sign != r2valSign && ((valueR1 & 0b00000000000000000000000010000000) >>> 7) == r2valSign) {
				SREG |= 0b00001000;
			} else {
				SREG &= 0b11110111;
			}
			if (valueR1 < 0)
				SREG |= 0b00000100;
			else
				SREG &= 0b11111011;

			if (valueR1 == 0)
				SREG |= 0b00000001;
			else
				SREG &= 0b11111110;

			N = (SREG & 0b00000100) >>> 2;
			V = (SREG & 0b00001000) >>> 2;
			s = N ^ V;

			if (s == 1)
				SREG |= 0b00000010;
			else
				SREG &= 0b11111101;
			break;

		case 2:
			System.out.println("Executing Instruction: MUL");
			System.out.println("Param 1: " + valueR1c + '\n' + "Param 2: " + valueR2c);
			valueR1 *= valueR2;
			regs[r1] = (byte) valueR1;
			System.out.print("Result : " + valueR1);

			if (valueR1 < 0)
				SREG |= 0b00000100;
			if (valueR1 == 0)
				SREG |= 0b00000001;

			break;
		case 3:
			System.out.println("Executing Instruction: MOVI");
			System.out.println("Param 1: " + valueR1c + '\n' + "Param 2: " + valueR2c);
			valueR1 = r2;
			regs[r1] = (byte) valueR1;
			System.out.print("Result : " + valueR1);
			break;
		case 4:
			System.out.println("Executing Instruction: BEQZ");
			System.out.println("Param 1: " + valueR1c + '\n' + "Param 2: " + valueR2c);
			System.out.println(valueR1);

			if (valueR1 == 0) {
				pc = (short) (pc + r2 - 2);
				branch = true;
			}
			decoding.removeAll(decoding);
			executing.removeAll(executing);
			fetching.removeAll(fetching);

			break;
		case 5:
			System.out.println("Executing Instruction: ANDI");
			System.out.println("Param 1: " + valueR1c + '\n' + "Param 2: " + valueR2c);
			valueR1 &= r2;
			regs[r1] = (byte) valueR1;
			System.out.print("Result : " + valueR1);
			if (valueR1 < 0)
				SREG |= 0b00000100;
			if (valueR1 == 0)
				SREG |= 0b00000001;

			break;
		case 6:
			System.out.println("Executing Instruction: EOR");
			System.out.println("Param 1: " + valueR1c + '\n' + "Param 2: " + valueR2c);

			valueR1 ^= valueR2;
			regs[r1] = (byte) valueR1;
			System.out.print("Result: " + valueR1);
			if (valueR1 < 0)
				SREG |= 0b00000100;
			if (valueR1 == 0)
				SREG |= 0b00000001;

			break;
		case 7:
			System.out.println("Executing Instruction: BR");
			System.out.println("PC: " + valueR1c + '\n' + "Param 2: " + valueR2c);

			pc = (short) ((valueR1 << 8) | valueR2);
			branch = true;
			decoding.removeAll(decoding);
			executing.removeAll(executing);
			fetching.removeAll(fetching);
			break;
		case 8:
			System.out.println("Executing Instruction: SAL");
			System.out.println("Param 1: " + valueR1c + '\n' + "Param 2: " + valueR2c);

			valueR1 = valueR1 << r2;
			regs[r1] = (byte) valueR1;
			System.out.print("Result : " + (byte) valueR1);
			if ((byte) valueR1 < 0)
				SREG |= 0b00000100;
			if ((byte) valueR1 == 0)
				SREG |= 0b00000001;

			break;
		case 9:
			System.out.println("Executing Instruction: SAR");
			System.out.println("Param 1: " + valueR1c + '\n' + "Param 2: " + valueR2c);

			valueR1 = valueR1 >> r2;
			regs[r1] = (byte) valueR1;

			System.out.print("Result : " + (byte) valueR1);
			if ((byte) valueR1 < 0)
				SREG |= 0b00000100;
			if ((byte) valueR1 == 0)
				SREG |= 0b00000001;

			break;
		case 10:
			System.out.println("Executing Instruction: LDR");
			System.out.println("Load into register: " + r1 + '\n' + "With value in address: " + r2);

			valueR1 = dataMemory[r2];
			regs[r1] = (byte) valueR1;
			System.out.print("Value in address " + valueR2 + "= " + dataMemory[r2]);
			break;
		case 11:
			System.out.println("Executing Instruction: STR");
			System.out.println("Param 1: " + valueR1c + '\n' + "Param 2: " + valueR2c);
			dataMemory[r2] = (byte) valueR1;
			System.out.println("Result : " + valueR1);
			break;

		}

	}

	public static void main(String[] args) throws IOException {
		ISA isa = new ISA();
		isa.run();

	}

}
