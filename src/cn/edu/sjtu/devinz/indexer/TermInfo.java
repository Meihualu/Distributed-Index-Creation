package cn.edu.sjtu.devinz.indexer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class TermInfo {
	
	public final String value;
	
	public final int zoneCode, docFreq;
	
	public List<Integer> postPoses = new ArrayList<Integer>();
	
	public TermInfo(String value, int zoneCode, int docFreq) {
		this.value = value;
		this.zoneCode = zoneCode;
		this.docFreq = docFreq;
	}
	
	public synchronized void addPostPos(int postPos) {
		int idx = Collections.binarySearch(postPoses, postPos);
		
		if (idx < 0) {
			postPoses.add(-(idx+1), postPos);
		}
	}
	
	@Override public boolean equals(Object o) {
		if (!(o instanceof TermInfo)) {
			return false;
		} else {
			TermInfo other = (TermInfo)o;
			if (!value.equals(other.value)) {
				return false;
			} else if (zoneCode != other.zoneCode) {
				return false;
			} else if (docFreq != other.docFreq) {
				return false;
			} else if (postPoses.size() != other.postPoses.size()) {
				return false;
			} else {
				for (int i=0; i<postPoses.size(); i++) {
					if (!postPoses.get(i).equals(other.postPoses.get(i))) {
						return false;
					}
				}
				return true;
			}
		}
	}
	
	@Override public int hashCode() {
		return 31*value.hashCode()+zoneCode;
	}
	
	@Override public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("[TERM] "+value+" "+Zones.decode(zoneCode)+" "+docFreq);
		for (Integer postPos: postPoses) {
			sb.append(" ");
			sb.append(postPos);
		}
		return sb.toString();
	}
	
}
