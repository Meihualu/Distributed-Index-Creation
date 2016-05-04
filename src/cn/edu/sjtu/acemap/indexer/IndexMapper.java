package cn.edu.sjtu.acemap.indexer;

import java.io.IOException;
import java.util.StringTokenizer;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;


class IndexMapper extends Mapper<LongWritable, Text, Text, Text> {
	
	@Override
	public void map(LongWritable key, Text value, Context context)
        throws IOException, InterruptedException {
		
		String valueString = value.toString().trim();
		StringTokenizer toks = new StringTokenizer(valueString);
		try {
			String url = toks.nextToken();
			String val = valueString.substring(url.length()+1);
			context.write(new Text(url), new Text(val));
		} catch (Exception e) {
			e.printStackTrace();
			LocalLog.append("IndexMapper.log", valueString+":\t"+e);
		}
	}
	
}
