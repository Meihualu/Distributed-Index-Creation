package cn.edu.sjtu.devinz.indexer;

import java.io.IOException;
import java.util.ArrayList;

public class Indexer {
	
	public static void addPost(String term, String url, int zoneCode, String[] poses) 
			throws IOException {
		
		if (poses.length > 0) {
			TermDict dict = TermDict.getInstance();
			dict.incDocFreq(term);
			
			TermInfo termInfo = dict.read(term, zoneCode);
			if (null == termInfo) {
				throw new RuntimeException("No such term in HBase TermDict.");
			} else {
				Posting post = new Posting(term, url, zoneCode, new ArrayList<Integer>());
				
				for (String pos : poses) {
					post.poses.add(Integer.valueOf(pos));
				}
				try {
					PostWriter.writePost(post, termInfo);
				} catch (IOException e) {
					Local.log("Indexer.log", term+"\t"+url+":\t"+e);
				}
			}
		}
	}
	
}

