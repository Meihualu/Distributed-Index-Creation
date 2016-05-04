package cn.edu.sjtu.acemap.indexer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


class TermInfo {
	
	public final String value;
	
	public int numOfDoc = 0;
	
	public List<Integer> postPoses = new ArrayList<Integer>();
	
	public TermInfo(String value) {
		this.value = value;
	}
	
	@Override public String toString() {
		String str = "[TERM] "+value+" "+numOfDoc;
		for (Integer postPos: postPoses) {
			str += "\t"+Integer.valueOf(postPos);
		}
		return str;
	}
	
	@Override public boolean equals(Object o) {
		if (!(o instanceof TermInfo)) {
			return false;
		} else {
			TermInfo other = (TermInfo)o;
			if (!value.equals(other.value)) {
				return false;
			} else if (numOfDoc != other.numOfDoc) {
				return false;
			} else if (postPoses.size() != other.postPoses.size()) {
				return false;
			} else {
				for (int i=0; i<postPoses.size(); i++) {
					if (postPoses.get(i) != other.postPoses.get(i)) {
						return false;
					}
				}
				return true;
			}
		}
	}
	
	public synchronized void addPostPos(int postPos) {
		int idx = Collections.binarySearch(postPoses, postPos);
		if (idx < 0) {
			postPoses.add(-(idx+1), postPos);
		}
	}
	
	public List<Integer> getPostPoses(String field) {
		List<Integer> lst = new ArrayList<Integer>(postPoses.size());
		int fieldCode = Field.encode(field);
		for (Integer postPos : postPoses) {
			if (fieldCode == Posting.getFieldCode(postPos)) {
				lst.add(postPos);
			}
		}
		return lst;
	}
	
}
