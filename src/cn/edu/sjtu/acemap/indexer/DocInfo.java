package cn.edu.sjtu.acemap.indexer;


class DocInfo {
	
	public final String url;
	
	public int[] fieldStats = new int[Field.NUM_OF_FIELDS];
	
	public DocInfo(String url) {
		this.url = url;
	}
	
	@Override public String toString() {
		String str = "[DOC] "+url;
		for (int i=0; i<fieldStats.length; i++) {
			str += "\t"+fieldStats[i];
		}
		return str;
	}
	
	@Override public boolean equals(Object o) {
		if (!(o instanceof DocInfo)) {
			return false;
		} else {
			DocInfo other = (DocInfo) o;
			if (!url.equals(other.url)) {
				return false;
			} else {
				for (int i=0; i<Field.NUM_OF_FIELDS; i++) {
					if (fieldStats[i] != other.fieldStats[i]) {
						return false;
					}
				}
				return true;
			}
		}
	}
	
	public void setFieldLength(String field, int stat) {
		setFieldLength(Field.encode(field), stat);
	}
	
	public void setFieldLength(int fieldCode, int stat) {
		if (fieldCode >= 0) {
			fieldStats[fieldCode] = stat;
		}
	}
	
	public int getFieldLength(String field) {
		return getFieldLength(Field.encode(field));
	}
	
	public int getFieldLength(int fieldCode) {
		if (fieldCode >= 0) {
			return fieldStats[fieldCode];
		} else {
			return 0;
		}
	}
	
	
}
