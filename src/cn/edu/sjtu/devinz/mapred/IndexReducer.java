package cn.edu.sjtu.devinz.mapred;

import java.io.IOException;
import java.util.StringTokenizer;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

import cn.edu.sjtu.devinz.indexer.DocMeta;
import cn.edu.sjtu.devinz.indexer.DocSet;
import cn.edu.sjtu.devinz.indexer.Indexer;
import cn.edu.sjtu.devinz.indexer.Local;
import cn.edu.sjtu.devinz.indexer.Zones;


class IndexReducer extends Reducer<Text, Text, Text, Text> {
	
	private static String[] split(String str) {
		StringTokenizer tokenizer = new StringTokenizer(str);
		String[] toks = new String[tokenizer.countTokens()];
		for (int i=0; i<toks.length; i++) {
			toks[i] = tokenizer.nextToken();
		}
		return toks;
	}
	
	/**
	 * <docURL>		DOC		<zoneName>		<zoneLength>
	 * <docURL>		TERM	<termValue>		<zoneName>		<pos_1>,<pos_2>,<pos_3>,...
	 */
	
	@Override
    public void reduce(Text key, Iterable<Text> values, Context context)
        throws IOException, InterruptedException {

		String url = key.toString();
		DocSet.incNumOfDoc();
		
		for (Text value : values) {
			String[] toks = split(value.toString());
			
			if (4==toks.length && toks[1].equals("DOC")) {
				DocMeta.addZone(url, toks[2], Integer.valueOf(toks[3]));
			} else if (5==toks.length && toks[1].equals("TERM")) {
				int zoneCode = Zones.encode(toks[3]);
				
				if (zoneCode >= 0) {
					try {
						Indexer.addPost(toks[2], url, zoneCode, toks[4].split(","));
					} catch (IOException e) {
						Local.log("IndexReducer.log", value.toString()+":\t"+e);
					}
				}
			}
		}
    }
	
}
