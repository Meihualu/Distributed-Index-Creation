package cn.edu.sjtu.devinz.mapred;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Partitioner;

class IndexPartitioner extends Partitioner<Text,Text> {

    @Override
    public int getPartition(Text key, Text value, int num) {
        return (key.toString().hashCode()&Integer.MAX_VALUE) % num;
    }

}
