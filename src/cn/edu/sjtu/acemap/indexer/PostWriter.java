package cn.edu.sjtu.acemap.indexer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


class PostWriter extends PostIO {

	private final String term;
	
	private PostWriter(String term, int postPos) throws IOException {
		super(postPos, true);
		this.term = term;
	}
	
	public Posting nextPosting() throws IOException {
		if (beyondEnd()) {
			return null;
		} else {
			try {
				String docURL = nextString();
				int numOfPoses = nextInt();
				Posting post = new Posting(term, field, docURL, 
						new ArrayList<Integer>(numOfPoses));
				for (int i=0; i<numOfPoses; i++) {
					post.addPos(nextInt());
				}
				return post;
			} catch (IOException e) {
				if (e.getMessage().equals("Fail to read String")) {
					return null;
				} else {
					throw e;
				}
			}
		}
	}
	
	/**
	 * Write a Posting to the inverted files, and at the same time
	 * 		update the state of the TermInfo
	 */
	public static void writePost(Posting post, TermInfo termInfo) throws IOException {
		List<Integer> postPoses = termInfo.getPostPoses(post.field);
		List<PostWriter> writers = new ArrayList<PostWriter>(16);
		int space = Posting.SIZE_LEN+post.getSize();
		for (int i=0; i<postPoses.size(); i++) {
			PostWriter writer = new PostWriter(post.term, postPoses.get(i));
			if (space+writer.getSlotSize()-Posting.SIZE_LEN >= writer.slotVol) {
				writers.add(writer);
				space += writer.getSlotSize()-Posting.SIZE_LEN;
			} else {
				try {
					merge(writer, writers, post);
				} finally {
					writer.close();
				}
				break;
			}
		}
		if (writers.size() == postPoses.size()) {
			int postPos = newPostPos(post.field, postPoses.size());
			termInfo.addPostPos(postPos);	/* modify termInfo */
			PostWriter writer = new PostWriter(termInfo.value, postPos);
			try {
				writer.allocate();	/* allocate slot space first */
				merge(writer, writers, post);
			} finally {
				writer.close();
			}
		}
		for (PostWriter writer : writers) {
			if (null != writer) {
				writer.clearSlot();
				writer.close();
			}
		}
		termInfo.numOfDoc++;	/* modify termInfo */
	}
	
	private static void merge(PostWriter to, List<PostWriter> from, Posting post) 
			throws IOException {
		
		Posting[] heads = new Posting[from.size()+1];
		for (int i=0; i<from.size(); i++) {
			heads[i] = from.get(i).nextPosting();
		}
		heads[from.size()] = post;
		to.jumpToEnd();
		int space = to.getSlotSize();
		while (true) {
			int i = -1;
			for (int j=0; j<heads.length; j++) {
				if (null != heads[j] && 
						(i<0 || heads[j].docURL.compareTo(heads[i].docURL)<0)) {
					i = j;
				}
			}
			if (i >= 0) {
				int size = heads[i].getSize();
				space += size;
				to.write(heads[i].toBytes(size));
				if (i < from.size()) {
					heads[i] = from.get(i).nextPosting();
				} else {
					heads[i] = null;
				}
			} else {
				break;
			}
		}
		to.writeSlotSize(space);
	}
	
	private static int newPostPos(String field, int partNO) 
			throws FileNotFoundException, IOException {
		
		String fileName = "/home/hadoop/index/"+partNO+"."+field;
		File file = new File(fileName);
		if (!file.exists()) {
			new FileOutputStream(fileName).close();
		}
		if (0 != file.length()%Posting.getSlotVolume(partNO)) {
			throw new RuntimeException("0 != file.length()%Posting.getSlotVolume(partNO)");
		} else {
			return Posting.encode(fileName, 
					(int)(file.length()/Posting.getSlotVolume(partNO)));
		}
	}
	
	private void clearSlot() throws IOException {
		writeSlotSize(Posting.SIZE_LEN);
		write(new byte[Posting.BLOCK]);
	}
	
	private void allocate() throws IOException {
		writeSlotSize(Posting.SIZE_LEN);
		byte[] bytes = new byte[Posting.BLOCK];
		for (int i=Posting.SIZE_LEN; i<slotVol; i+=Posting.BLOCK) {
			write(bytes);
		}
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
					Field.decode(rand.nextInt(Field.NUM_OF_FIELDS)),docURL,poses);
			writePost(post, termInfo);
		}
	}
	
}
