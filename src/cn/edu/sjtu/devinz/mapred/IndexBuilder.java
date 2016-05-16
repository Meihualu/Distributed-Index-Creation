package cn.edu.sjtu.devinz.mapred;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;


public class IndexBuilder {

	private static Configuration config = new Configuration();
	
	static {
		config.set("fs.defaultFS", "hdfs://master:9000");
		config.set("hadoop.job.user", "hadoop");
		config.set("mapreduce.framework.name", "yarn");
		config.set("mapreduce.job.jar", "Indexer.jar");
		config.set("yarn.resourcemanager.address", "master:8032");
		config.set("yarn.resourcemanager.hostname", "master");
	}
	
	public static synchronized void build(String inputPath) 
			throws IOException, ClassNotFoundException, InterruptedException {
			
		Job job = Job.getInstance(config);
        job.setJarByClass(IndexBuilder.class);
        job.setJobName("Index Building");
        FileInputFormat.addInputPath(job, new Path(inputPath));
		FileOutputFormat.setOutputPath(job, new Path("/index"));
        job.setMapperClass(IndexMapper.class);
        job.setReducerClass(IndexReducer.class);
        job.setOutputKeyClass(Text.class);
	    job.setOutputValueClass(Text.class);
	    System.out.println("MapReduce starts.");
	    if (job.waitForCompletion(true)) {
	    	System.out.println("All is done.");
	    } else {
	    	System.err.println("Job is aborted.");
	    }
	}
	
	public static void main(String[] args) {
		try {
			build("/TextTransformation/");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}
