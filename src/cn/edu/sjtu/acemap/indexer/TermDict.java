package cn.edu.sjtu.acemap.indexer;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Admin;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.util.Bytes;


class TermDict implements Closeable {
	
	private final Connection connection;
	private final Table table;
	
	private TermDict() throws IOException {
		connection = ConnectionFactory.createConnection(config);
		table = connection.getTable(TableName.valueOf("TermDict"));
	}
	
	public void close() throws IOException {
		try {
			table.close();
		} finally {
			connection.close();
		}
	}
	
	public TermInfo read(String value) throws IOException {
		TermInfo termInfo = new TermInfo(value);
		Result result = table.get(new Get(Bytes.toBytes(value)));
		if (null == result || result.isEmpty()) {
			return null;
		} else {
			termInfo.numOfDoc = Bytes.toInt(result.getValue(Bytes.toBytes(IndexReducer.nodeName),
					Bytes.toBytes("numOfDoc")));
			int size = Bytes.toInt(result.getValue(Bytes.toBytes(IndexReducer.nodeName), 
					Bytes.toBytes("numOfPostPoses")));
			for (int i=0; i<size; i++) {
				termInfo.addPostPos(Bytes.toInt(result.getValue
						(Bytes.toBytes(IndexReducer.nodeName), Bytes.toBytes(i))));
			}
		}
		return termInfo;
	}
	
	public void writeTerm(TermInfo termInfo) throws IOException {
		Put put = new Put(Bytes.toBytes(termInfo.value));
		put.addColumn(Bytes.toBytes(IndexReducer.nodeName), Bytes.toBytes("numOfDoc"), 
				Bytes.toBytes(termInfo.numOfDoc));
		put.addColumn(Bytes.toBytes(IndexReducer.nodeName), Bytes.toBytes("numOfPostPoses"), 
				Bytes.toBytes(termInfo.postPoses.size()));
		for (int i=0; i<termInfo.postPoses.size(); i++) {
			put.addColumn(Bytes.toBytes(IndexReducer.nodeName), Bytes.toBytes(i),
					Bytes.toBytes(termInfo.postPoses.get(i)));
		}
		table.put(put);
	}
	
	private static final Configuration config = HBaseConfiguration.create();
	private static TermDict instance;
	
	static {
		config.set("fs.default.name", "hdfs://master:9000");
		config.set("hadoop.job.user", "hadoop");
		config.set("mapreduce.framework.name", "yarn");
		config.set("mapred.jar", "TextTransformation.jar");
		config.set("yarn.resourcemanager.address", "master:8032");
		config.set("yarn.resourcemanager.hostname", "master");
	}
	
	public static TermDict getInstance() throws IOException {
		if (null == instance) {
			synchronized (TermDict.class) {
				if (null == instance) {
					instance = new TermDict();
				}
			}
		}
		return instance;
	}
	
	public static void main(String[] args) throws IOException {
		java.util.Random rand = new java.util.Random();
		TermDict dict = getInstance();
		List<TermInfo> terms = new ArrayList<TermInfo>(100);
		for (int i=0; i<100; i++) {
			TermInfo term = new TermInfo("term_"+i);
			term.numOfDoc = i*10;
			int n = rand.nextInt(5);
			for (int j=0; j<n; j++) {
				term.addPostPos(i+j);
			}
			terms.add(term);
			dict.writeTerm(term);
		}
		System.err.println();
		for (int i=0; i<100; i++) {
			TermInfo term = dict.read("term_"+i);
			if (null == term) {
				exit(1);
			} else if (!terms.get(i).equals(term)) {
				exit(2);
			}
		}
		System.out.println("All tests OK.");
		exit(0);
	}
	
	private static void exit(int argv) throws IOException {
		Connection connection = ConnectionFactory.createConnection(config);
		try {
			Admin admin = connection.getAdmin();
			admin.disableTable(TableName.valueOf("TermDict"));
			admin.truncateTable(TableName.valueOf("TermDict"), true);
			if (0 != argv) {
				throw new RuntimeException("Failure "+argv);
			}
		} finally {
			connection.close();
			getInstance().close();
		}
	}
	
}
