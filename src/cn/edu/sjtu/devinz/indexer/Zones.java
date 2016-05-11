package cn.edu.sjtu.devinz.indexer;

import java.util.Arrays;

public class Zones {
	
	private static final String[] zones = {
		"title",
		"author",
		"abstract",
		"keywords",
		"journal",
		"conference",
		"reference"
	};
	
	public static final int NUM_OF_ZONES = zones.length;
	
	public static final double[] weights = new double[NUM_OF_ZONES];
	
	static {
		Arrays.fill(weights, 1./NUM_OF_ZONES);
	}
	
	public static int encode(String zone) {
		for (int i=0; i<zones.length; i++) {
			if (isSame(zone, zones[i])) {
				return i;
			}
		}
		return -1;
	}
	
	public static String decode(int code) {
		if (code<0 || code>=zones.length) {
			return null;
		} else {
			return zones[code];
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
