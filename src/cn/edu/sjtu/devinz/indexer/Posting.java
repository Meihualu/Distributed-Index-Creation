package cn.edu.sjtu.devinz.indexer;

import java.util.List;

public class Posting {
	
	public final String term, docURL;
	
	public final int zoneCode;
	
	public final List<Integer> poses;
	
	public Posting(String term, String docURL, int zoneCode, List<Integer> poses) {
		if (null != poses) {
			this.term = term;
			this.docURL = docURL;
			this.zoneCode = zoneCode;
			this.poses = poses;
		} else {
			throw new NullPointerException("null == poses");
		}
	}
	
	@Override public boolean equals(Object o) {
		if (!(o instanceof Posting)) {
			return false;
		} else {
			Posting other = (Posting) o;
			if (!term.equals(other.term)) {
				return false;
			} else if (!docURL.equals(other.docURL)) {
				return false;
			} else if (zoneCode != other.zoneCode) {
				return false;
			} else if (poses.size() != other.poses.size()) {
				return false;
			} else {
				for (int i=0; i<poses.size(); i++) {
					if (!poses.get(i).equals(other.poses.get(i))) {
						return false;
					}
				}
				return true;
			}
		}
	}
	
	@Override public int hashCode() {
		int val = term.hashCode();
		val = 31*val+docURL.hashCode();
		return 31*val+zoneCode;
	}
	
	@Override public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("[Posting] "+term+" "+docURL+" "+Zones.decode(zoneCode));
		for (Integer pos : poses) {
			sb.append(" ");
			sb.append(pos);
		}
		return sb.toString();
	}
	
	public int lenOfBytes() {
		int size = docURL.length()+1;
		size += getCodeLength(poses.size());
		int prevPos = 0;
		for (Integer pos : poses) {
			size += getCodeLength(pos-prevPos);
			prevPos = pos;
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
		if (poses.size() > 0) {
			for (int i=poses.size()-1; i>0; i--) {
				j = toBytes(poses.get(i)-poses.get(i-1), bytes, j);
			}
			j = toBytes(poses.get(0), bytes, j);
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
	
}
