package cn.edu.sjtu.acemap.indexer;


public class Field {
	
	private static final String[] fields = {
		"title",
		"author",
		"abstract",
		"keywords",
		"journal",
		"conference",
		"reference"
	};
	
	public static final int NUM_OF_FIELDS = fields.length;
	
	public static int encode(String field) {
		for (int i=0; i<fields.length; i++) {
			if (isSame(field, fields[i])) {
				return i;
			}
		}
		return -1;
	}
	
	public static String decode(int code) {
		if (code<0 || code>=fields.length) {
			return null;
		} else {
			return fields[code];
		}
	}
	
	private static boolean isSame(String a, String b) {
		if (a.length() != b.length()) {
			return false;
		} else {
			for (int i=0; i<a.length(); i++) {
				if (a.charAt(i) == b.charAt(i)) {
					continue;
				} else if (a.charAt(i)+'A' == b.charAt(i)+'a'
						&& a.charAt(i)>='a' && a.charAt(i)<='z') {
					continue;
				} else if (a.charAt(i)+'a' == b.charAt(i)+'A'
						&& a.charAt(i)>='A' && a.charAt(i)<='Z') {
					continue;
				} else {
					return false;
				}
			}
			return true;
		}
	}
	
}
