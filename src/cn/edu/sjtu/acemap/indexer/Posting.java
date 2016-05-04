package cn.edu.sjtu.acemap.indexer;

import java.util.List;
import java.util.StringTokenizer;


class Posting {
	
	public final String term, field, docURL;
	
	private final List<Integer> poses;
	
	public Posting(String term, String field, String docURL, List<Integer> poses) {
		if (null != poses) {
			this.term = term;
			this.field = field;
			this.docURL = docURL;
			this.poses = poses;
		} else {
			throw new NullPointerException("null == poses");
		}
	}
	
	@Override public String toString() {
		String str = "[Posting] "+term+" "+field+" "+docURL;
		for (Integer pos : poses) {
			str += "\t"+pos;
		}
		return str;
	}
	
	@Override public boolean equals(Object o) {
		if (!(o instanceof Posting)) {
			return false;
		} else {
			Posting other = (Posting) o;
			if (!term.equals(other.term)) {
				return false;
			} else if (!field.equals(other.field)) {
				return false;
			} else if (!docURL.equals(other.docURL)) {
				return false;
			} else if (poses.size() != other.poses.size()) {
				return false;
			} else {
				for (int i=0; i<poses.size(); i++) {
					if (poses.get(i) != other.poses.get(i)) {
						return false;
					}
				}
				return true;
			}
		}
	}
	
	public synchronized void addPos(int pos) {
		poses.add(pos);
	}
	
	public int getNumOfPoses() {
		return poses.size();
	}

	public int getSize() {
		int size = docURL.length()+1;
		size += getCodeLength(poses.size());
		for (Integer pos : poses) {
			size += getCodeLength(pos);
		}
		return size;
	}
	
	private static int getCodeLength(int nonNeg) {
		int len = 1;
		nonNeg >>>= 7;
		while (nonNeg > 0) {
			len++;
			nonNeg >>>= 7;
		}
		return len;
	}
	
	public byte[] toBytes(int size) {
		byte[] bytes = new byte[size];
		for (int i=0; i<docURL.length(); i++) {
			bytes[i] = (byte) docURL.charAt(i);
		}
		int j = bytes.length-1;
		for (int i=poses.size()-1; i>=0; i--) {
			j = toBytes(poses.get(i), bytes, j);
		}
		j = toBytes(poses.size(), bytes, j);
		bytes[j] = (byte)'\0';
		return bytes;
	}
	
	/**
	 * Write an non-negative integer to bytes, with ending index j
	 * @return the starting index
	 */
	private static int toBytes(int nonNeg, byte[] bytes, int j) {
		bytes[j--] = (byte) (128 | (nonNeg & 127));
		nonNeg >>>= 7;
		while (nonNeg > 0) {
			bytes[j--] = (byte) (nonNeg & 127);
			nonNeg >>>= 7;
		}
		return j;
	}
	
	/** PostPos = 4-bit partition # + 4-bit field code + 24-bit slot # */
	
	public static final int SIZE_LEN = 4;
	public static final int RADIX = 3;
	public static final int BLOCK = 1024;
	public static final int UNIT = (RADIX-1)*BLOCK;
	
	public static int getSlotVolume(int partNO) {
		return UNIT*getVolHelp(partNO) + SIZE_LEN;
	}
	
	private static int getVolHelp(int partNO) {
		if (0 == partNO) {
			return 1;
		} else {
			int tmp = getVolHelp(partNO/2);
			if (0 == partNO%2) {
				return tmp * tmp;
			} else {
				return RADIX * tmp * tmp;
			}
		}
	}
	
	public static int encode(String filename, int slotNO) {
		StringTokenizer toks = new StringTokenizer(filename, ".");
		try {
			StringTokenizer filetoks = new StringTokenizer(toks.nextToken(), "/");
			while (filetoks.countTokens() > 1) {
				filetoks.nextToken();
			}
			int partNO = Integer.valueOf(filetoks.nextToken());
			if (partNO<0 || partNO>=16) {
				throw new Exception("partition # out of range.");
			}
			String fieldName = toks.nextToken();
			int fieldCode = Field.encode(fieldName);
			if (fieldCode < 0) {
				throw new Exception("field name "+fieldName+" unrecognized.");
			}
			if (slotNO<0 || slotNO>=(1<<24)) {
				throw new Exception("slot position "+slotNO+" out of range.");
			}
			return (partNO<<28) | (fieldCode<<24) | slotNO;
		} catch (Exception e) {
			e.printStackTrace();
			LocalLog.append("Posting.log", e.toString());
			return -1;
		}
	}
	
	public static int getPartNO(int postPos) {
		return (postPos & (-(1<<28)))>>>28;
	}
	
	public static int getFieldCode(int postPos) {
		return (postPos & ((1<<28)-(1<<24)))>>>24;
	}
	
	public static int getPostNO(int postPos) {
		return postPos & ((1<<24)-1);
	}
	
	public static void main(String[] args) {
		for (int i=0; i<5; i++) {
			System.out.println("slotVolume("+i+") = "+getSlotVolume(i));
		}
		java.util.Random rand = new java.util.Random();
		for (int test=0; test<100; test++) {
			int partNO = rand.nextInt(16);
			int fieldCode = rand.nextInt(Field.NUM_OF_FIELDS);
			int slotNO = rand.nextInt(1<<24);
			int code = encode(Integer.toString(partNO)+"."+Field.decode(fieldCode), slotNO);
			if (code != (partNO<<28) + (fieldCode<<24) + slotNO) {
				throw new RuntimeException("Failure.");
			}
		}
		System.out.println("All tests OK.");
	}
	
}
