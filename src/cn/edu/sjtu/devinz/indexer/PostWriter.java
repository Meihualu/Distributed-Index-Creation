package cn.edu.sjtu.devinz.indexer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


class PostWriter extends PostIO {

	private final String term;
	
	private PostWriter(String term, int postPos, int zoneCode) throws IOException {
		super(postPos, zoneCode, true);
		this.term = term;
	}
	
	public Posting nextPosting() throws IOException {
		if (beyondEnd()) {
			return null;
		} else {
			try {
				String docURL = nextString();
				int numOfPoses = nextInt();
				Posting post = new Posting(term, docURL, Zones.encode(zone),
						new ArrayList<Integer>(numOfPoses));
				for (int i=0; i<numOfPoses; i++) {
					post.poses.add(nextInt());
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
	
	/** Write a Posting to the inverted files, add to TermDict if needed */
	
	public static void writePost(Posting post, TermInfo termInfo) throws IOException {
		Local.log("PostWriter.log", "writePost:\t"+post);
		List<PostWriter> writers = new ArrayList<PostWriter>(16);
		int space = PostPoses.SIZE_LEN+post.lenOfBytes();
		for (int i=0; i<termInfo.postPoses.size(); i++) {
			PostWriter writer = new PostWriter(post.term, termInfo.postPoses.get(i), post.zoneCode);
			if (space+writer.getSlotSize()-PostPoses.SIZE_LEN >= writer.slotVol) {
				writers.add(writer);
				space += writer.getSlotSize()-PostPoses.SIZE_LEN;
			} else {
				try {
					merge(writer, writers, post);
				} finally {
					writer.close();
				}
				break;
			}
		}
		if (writers.size() == termInfo.postPoses.size()) {
			int postPos = newPostPos(post.zoneCode, writers.size());
			termInfo.addPostPos(postPos);	/* modify termInfo */
			PostWriter writer = new PostWriter(termInfo.value, postPos, post.zoneCode);
			while (space >= writer.slotVol) {
				writer.allocate();
				writer.jumpToEnd();
				writers.add(writer);
				postPos = newPostPos(post.zoneCode, writers.size());
				termInfo.addPostPos(postPos);
				writer = new PostWriter(termInfo.value, postPos, post.zoneCode);
			}
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
				int size = heads[i].lenOfBytes();
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
	
	private static int newPostPos(int zoneCode, int partNO) 
			throws FileNotFoundException, IOException {
		
		String fileName = "/home/hadoop/.devin/index/"+partNO+"."+Zones.decode(zoneCode);
		File file = new File(fileName);
		if (!file.exists()) {
			new FileOutputStream(fileName).close();
		}
		if (0 != file.length()%PostPoses.getSlotVolume(partNO)) {
			throw new RuntimeException("0 != file.length()%Posting.getSlotVolume(partNO)");
		} else {
			return PostPoses.encode(fileName, 
					(int)(file.length()/PostPoses.getSlotVolume(partNO)));
		}
	}
	
	private void clearSlot() throws IOException {
		writeSlotSize(PostPoses.SIZE_LEN);
		write(new byte[PostPoses.BLOCK]);
	}
	
	private void allocate() throws IOException {
		writeSlotSize(PostPoses.SIZE_LEN);
		byte[] bytes = new byte[PostPoses.BLOCK];
		for (int i=PostPoses.SIZE_LEN; i<slotVol; i+=PostPoses.BLOCK) {
			write(bytes);
		}
	}
	
	public static void main(String[] args) throws IOException {
		java.util.Random rand = new java.util.Random();
		String term = "term";
		String docURL = "docURL";
		TermInfo termInfo = new TermInfo(term, 0, 1);
		
		for (int time=0; time<500000; time++) {
			System.out.println("time = "+time);
			List<Integer> poses = new ArrayList<Integer>();
			int len = rand.nextInt(100);
			for (int i=0; i<len; i++) {
				poses.add(rand.nextInt(100));
			}
			Posting post = new Posting(term, docURL, rand.nextInt(Zones.NUM_OF_ZONES), poses);
			writePost(post, termInfo);
		}
	}
	
}
