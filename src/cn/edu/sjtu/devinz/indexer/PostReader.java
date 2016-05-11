package cn.edu.sjtu.devinz.indexer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class PostReader extends PostIO {
	
	public final String term;
	
	public PostReader(String term, int postPos, int zoneCode) throws IOException {
		super(postPos, zoneCode, false);
		this.term = term;
	}
	
	public Posting nextPosting() throws IOException {
		Posting post = null;
		if (!beyondEnd()) {
			String docURL = nextString();
			int numOfPoses = nextInt();
			post = new Posting(term, docURL, Zones.encode(zone),
					new ArrayList<Integer>(numOfPoses));
			for (int i=0; i<numOfPoses; i++) {
				post.poses.add(nextInt());
			}
		}
		return post;
	}
	
	public static void main(String[] args) throws IOException {
		java.util.Random rand = new java.util.Random();
		String term = "term";
		String docURL = "docURL";
		TermInfo termInfo = new TermInfo(term, 0, 1);
		for (int time=0; time<100000; time++) {
			System.out.println("time = "+time);
			List<Integer> poses = new ArrayList<Integer>();
			for (int i=0; i<5; i++) {
				poses.add(rand.nextInt(100));
			}
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
	}
	
}
