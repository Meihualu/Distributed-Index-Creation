package cn.edu.sjtu.devinz.mapred;

import java.io.IOException;
import java.util.StringTokenizer;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

import cn.edu.sjtu.devinz.indexer.Local;


class IndexMapper extends Mapper<LongWritable, Text, Text, Text> {

    @Override public void map(LongWritable key, Text value, Context context)
        throws IOException, InterruptedException {

        String val = value.toString().replace('\t', ' ').trim();
        StringTokenizer toks = new StringTokenizer(val);

        try {
            context.write(new Text(toks.nextToken()), new Text(val));
        } catch (Exception e) {
            Local.log("IndexMapper.log", val+":\t"+e);
        }
    }

}
