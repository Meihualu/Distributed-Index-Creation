package cn.edu.sjtu.devinz.indexer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PostReader extends PostIO {
	
	public PostReader(String term, int postPos, int zoneCode) 
			throws IOException {
		
		super(term, postPos, zoneCode, false);
	}
	
	public int getSlotSize() {
		return super.getSlotSize();
	}
	
	@Override protected void writeSlotSize(int size) throws IOException {
		throw new IOException("PostReader could not write slot size.");
	}
	
	@Override protected void write(byte[] bytes) throws IOException {
		throw new IOException("PostReader could not write bytes to the file.");
	}
	
	public static void main(String[] args) throws IOException {
		DocMeta.clear();
		
		java.util.Random rand = new java.util.Random();
		String term = "term";
		String docURL = "docURL";
		TermInfo termInfo = new TermInfo(term, 0, 1);
		DocMeta.addDoc(docURL);
		for (int time=0; time<50000; time++) {
			if (0 == time%100) {
				System.out.println("time = "+time);
			}
			List<Integer> poses = new ArrayList<Integer>();
			for (int i=0; i<5; i++) {
				poses.add(rand.nextInt(100));
			}
			Collections.sort(poses);
			Posting post = new Posting(term,
					docURL, rand.nextInt(Zones.NUM_OF_ZONES), poses);
			PostWriter.writePost(post, termInfo);
		}
		
		int cnt = 0;
		for (Integer postPos : termInfo.postPoses) {
			PostReader reader = new PostReader(termInfo.value, postPos, 0);
			try {
				Posting post = reader.nextPosting();
				while (null != post) {
					System.out.println("Get "+(cnt++)+": "+post);
					post = reader.nextPosting();
				}
			} finally {
				reader.close();
			}
		}
		
		DocMeta.clear();
	}
	
}
