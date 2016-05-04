package cn.edu.sjtu.acemap.indexer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;



class PostReader extends PostIO {
	
	private final QueryInfo queryInfo;
	
	public PostReader(int postPos, QueryInfo queryInfo) throws IOException {
		super(postPos, false);
		if (null != queryInfo) {
			this.queryInfo = queryInfo;
		} else {
			throw new NullPointerException("queryInfo == null");
		}
	}
	
	public Posting nextPosting() throws IOException {
		Posting post = null;
		if (!beyondEnd()) {
			String docURL = nextString();
			int numOfPoses = nextInt();
			post = new Posting(queryInfo.term, field, docURL, 
					new ArrayList<Integer>(numOfPoses));
			for (int i=0; i<numOfPoses; i++) {
				int pos = nextInt();
				if (pos < queryInfo.getMinPos()) {
					continue;
				} else if (pos > queryInfo.getMaxPos()) {
					break;
				} else {
					post.addPos(pos);
				}
			}
		}
		return post;
	}
	
	public static void main(String[] args) throws IOException {
		java.util.Random rand = new java.util.Random();
		String term = "term";
		String docURL = "docURL";
		TermInfo termInfo = new TermInfo(term);
		for (int time=0; time<1000000; time++) {
			System.out.println("time = "+time);
			List<Integer> poses = new ArrayList<Integer>();
			for (int i=0; i<5; i++) {
				poses.add(rand.nextInt(100));
			}
			Posting post = new Posting(term,
					Field.decode(rand.nextInt(Field.NUM_OF_FIELDS)), docURL, poses);
			PostWriter.writePost(post, termInfo);
		}
		QueryInfo query = new QueryInfo("term", null);
		int cnt = 0;
		for (Integer postPos : termInfo.postPoses) {
			PostReader reader = new PostReader(postPos, query);
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
