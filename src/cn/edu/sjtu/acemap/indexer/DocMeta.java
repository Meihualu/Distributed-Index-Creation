package cn.edu.sjtu.acemap.indexer;

import java.io.Closeable;
import java.io.IOException;

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

class DocMeta implements Closeable {
	
	private final Connection connection;
	private final Table table;
	
	private DocMeta() throws IOException {
		connection = ConnectionFactory.createConnection(config);
		table = connection.getTable(TableName.valueOf("DocMeta"));
	}
	
	public void close() throws IOException {
		try {
			table.close();
		} finally {
			connection.close();
		}
	}
	
	public DocInfo read(String url) throws IOException {
		Result result = table.get(new Get(Bytes.toBytes(url)));
		if (null == result || result.isEmpty()) {
			return null;
		} else {
			DocInfo docInfo = new DocInfo(url);
			for (int i=0; i<Field.NUM_OF_FIELDS; i++) {
				docInfo.fieldStats[i] = Bytes.toInt(result.getValue
						(Bytes.toBytes(IndexReducer.nodeName), Bytes.toBytes(i)));
			}
			return docInfo;
		}
	}
	
	public void writeDoc(DocInfo docInfo) throws IOException {
		DocSet.addDoc(docInfo);
		Put put = new Put(Bytes.toBytes(docInfo.url));
		for (int i=0; i<Field.NUM_OF_FIELDS; i++) {
			put.addColumn(Bytes.toBytes(IndexReducer.nodeName), Bytes.toBytes(i),
					Bytes.toBytes(docInfo.getFieldLength(i)));
		}
		table.put(put);
	}
	
	private static final Configuration config = HBaseConfiguration.create();
	private static DocMeta instance;
	
	static {
		config.set("fs.default.name", "hdfs://master:9000");
		config.set("hadoop.job.user", "hadoop");
		config.set("mapreduce.framework.name", "yarn");
		config.set("mapred.jar", "TextTransformation.jar");
		config.set("yarn.resourcemanager.address", "master:8032");
		config.set("yarn.resourcemanager.hostname", "master");
	}
	
	public static DocMeta getInstance() throws IOException {
		if (null == instance) {
			synchronized (DocMeta.class) {
				if (null == instance) {
					instance = new DocMeta();
				}
			}
		}
		return instance;
	}
	
	public static void main(String[] args) throws IOException {
		java.util.Random rand = new java.util.Random();
		DocMeta meta = getInstance();
		DocInfo[] docs = new DocInfo[100];
		for (int i=0; i<100; i++) {
			docs[i] = new DocInfo("url_"+i);
			for (int j=0; j<Field.NUM_OF_FIELDS; j++) {
				docs[i].fieldStats[j] = rand.nextInt(1000);
			}
			meta.writeDoc(docs[i]);
		}
		for (int i=0; i<100; i++) {
			DocInfo result = meta.read("url_"+i);
			if (null == result) {
				exit(1);
			} else if (!result.equals(docs[i])) {
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
			admin.disableTable(TableName.valueOf("DocMeta"));
			admin.truncateTable(TableName.valueOf("DocMeta"), true);
			if (0 != argv) {
				throw new RuntimeException("Failure "+argv);
			}
		} finally {
			connection.close();
			getInstance().close();
		}
	}
	
	
}
