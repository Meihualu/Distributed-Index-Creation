package cn.edu.sjtu.devinz.indexer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Scanner;

public class Indexer {
	
	public static void addPost(String term, String url, int zoneCode, String[] poses) 
			throws IOException {
		
		//Local.log("Indexer.log", "addPost:\t"+term+"\t"+url+"\t"+Zones.decode(zoneCode));
		
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
				Collections.sort(post.poses);
				try {
					//Local.log("Indexer.log", "try to write the posting");
					PostWriter.writePost(post, termInfo);
					//Local.log("Indexer.log", "try to add postPoses");
					dict.addPostPoses(term, zoneCode, termInfo.postPoses);
					//Local.log("Indexer.log", "done");
				} catch (IOException e) {
					Local.log("Indexer.log", term+"\t"+url+":\t"+e);
				}
			}
		}
	}
	
	public static void main(String[] args) throws IOException {
		Scanner in = new Scanner(System.in);
		
		try {
			System.out.println("Are you sure to remove all indexes? [Y/N]");
			if (in.nextLine().trim().equals("Y") || in.nextLine().trim().equals("y")) {
				TermDict dict = TermDict.getInstance();
				
				try {
					Runtime.getRuntime().exec("rm -f index/*");
					Runtime.getRuntime().exec("rm -f logs/*.log");
					DocMeta.clear();
					dict.clear();
				} finally {
					dict.close();
				}
			}
		} finally {
			in.close();
		}
	}
	
}

