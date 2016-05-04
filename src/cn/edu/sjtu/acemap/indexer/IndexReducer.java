package cn.edu.sjtu.acemap.indexer;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

class IndexReducer extends Reducer<Text, Text, Text, Text> {
	
	/**
	 * <docURL>		DOC		<fieldName>		<fieldLength>	
	 * <docURL>		TERM	<termValue>		<fieldName>		<pos_1>,<pos_2>,<pos_3>,...
	 */
	@Override
    public void reduce(Text key, Iterable<Text> values, Context context)
        throws IOException, InterruptedException {

		String url = key.toString();
		DocInfo docInfo = new DocInfo(url);
		for (Text value : values) {
			StringTokenizer toks = new StringTokenizer(value.toString());
			try {
				String type = toks.nextToken();
				if (type.equals("TERM")) {
					String tval = toks.nextToken();
					String field = toks.nextToken();
					StringTokenizer posToks = new StringTokenizer(toks.nextToken(), ",");
					List<Integer> poses = new ArrayList<Integer>(posToks.countTokens());
					while (posToks.hasMoreTokens()) {
						poses.add(Integer.valueOf(posToks.nextToken()));
					}
					Collections.sort(poses);
					addPost(new Posting(tval, field, url, poses));
				} else if (type.equals("DOC")) {
					String field = toks.nextToken();
					int stat = Integer.valueOf(toks.nextToken());
					docInfo.setFieldLength(field, stat);
				} else {
					throw new IOException("Unrecognized Type "+type);
				}
			} catch (Exception e) {
				e.printStackTrace();
				LocalLog.append("IndexReducer.log", url+"\t"+value.toString()+":\t"+e);
			}
		}
		DocMeta.getInstance().writeDoc(docInfo);
    }
	
	public static final String nodeName;
	
	static {
		String line = null;
		try {
			BufferedReader reader = new BufferedReader
					(new FileReader("/etc/hostname"));
			try {
				line = reader.readLine();
			} finally {
				reader.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (null == (nodeName = line)) {
				System.exit(1);
			}
		}
	}
	
	private static void addPost(Posting post) throws IOException {
		if (post.getNumOfPoses() > 0) {
			TermDict dict = TermDict.getInstance();
			TermInfo termInfo = dict.read(post.term);
			if (null == termInfo) {
				termInfo = new TermInfo(post.term);
			}
			PostWriter.writePost(post, termInfo);
			dict.writeTerm(termInfo);
		}
	}
	
	public static void main(String[] args) throws IOException {
		String[] terms = new String[10];
		for (int i=0; i<10; i++) {
			terms[i] = "term_"+i;
			TermDict.getInstance().writeTerm(new TermInfo(terms[i]));
		}
		java.util.Random rand = new java.util.Random();
		for (int i=0; i<100000; i++) {
			System.out.println("i = "+i);
			int j = rand.nextInt(10);
			List<Integer> poses = new ArrayList<Integer>();
			for (int k=0; k<5; k++) {
				poses.add(rand.nextInt(100));
			}
			addPost(new Posting(terms[j], 
					Field.decode(rand.nextInt(Field.NUM_OF_FIELDS)), "docURL", poses));
		}
		TermDict.getInstance().close();
	}
	
}
