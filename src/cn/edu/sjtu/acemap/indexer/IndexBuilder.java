package cn.edu.sjtu.acemap.indexer;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

public class IndexBuilder {

	private static Configuration config = new Configuration();
	
	static {
		config.set("fs.default.name", "hdfs://master:9000");
		config.set("hadoop.job.user", "hadoop");
		config.set("mapreduce.framework.name", "yarn");
		config.set("mapred.jar", "TextTransformation.jar");
		config.set("yarn.resourcemanager.address", "master:8032");
		config.set("yarn.resourcemanager.hostname", "master");
	}
	
	public static synchronized void build(String inputPath) 
			throws IOException, ClassNotFoundException, InterruptedException {
			
		Job job = Job.getInstance(config);
        job.setJarByClass(IndexBuilder.class);
        job.setJobName("Index Building");
        Path output = new Path("/Index");
        FileInputFormat.addInputPath(job, new Path(inputPath));
		FileOutputFormat.setOutputPath(job, output);
        job.setMapperClass(IndexMapper.class);
        job.setReducerClass(IndexReducer.class);
        job.setOutputKeyClass(Text.class);
	    job.setOutputValueClass(Text.class);
        try {
        	try {
        		try {
	    		    System.exit(job.waitForCompletion(true) ? 0 : 1);
	    			System.out.println("All is done.");
        		} finally {
        			TermDict.getInstance().close();
        
        		}
        	} finally {
        		DocMeta.getInstance().close();
        	}
        } finally {
        	FileSystem fs = FileSystem.get(config);
		    fs.delete(output, true);
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
