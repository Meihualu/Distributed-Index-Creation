package cn.edu.sjtu.acemap.indexer;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.util.Bytes;

/**
 * This class gives you the number of Doc and the average lengths
 * 		of the different fields on the local host.
 * @author Nan Zuo
 *
 */

public class DocSet {
	
	private static int numOfDoc = 0;
	
	private static double[] fieldStats = new double[Field.NUM_OF_FIELDS];
	
	private static final Configuration config = HBaseConfiguration.create();
	
	static {
		/*
		config.set("fs.default.name", "hdfs://master:9000");
		config.set("hadoop.job.user", "hadoop");
		config.set("mapreduce.framework.name", "yarn");
		config.set("yarn.resourcemanager.address", "master:8032");
		config.set("mapred.jar", "TextTransformation.jar");
		config.set("yarn.resourcemanager.hostname", "master");
		*/
		try {
			reset();
		} catch (IOException e) {
			LocalLog.append("DocSet.log", e.toString());
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	private static void reset() throws IOException {
		Connection  connection = ConnectionFactory.createConnection(config);
		try {
			Table table = connection.getTable(TableName.valueOf("DocMeta"));
			try {
				ResultScanner scanner = table.getScanner(new Scan());
				for (Result result : scanner) {
					if (!result.isEmpty()) {
						DocInfo docInfo = new DocInfo(Bytes.toString(result.getValue
								(Bytes.toBytes(IndexReducer.nodeName), result.getRow())));
						for (int i=0; i<Field.NUM_OF_FIELDS; i++) {
							docInfo.setFieldLength(i, Bytes.toInt(result.getValue
									(Bytes.toBytes(IndexReducer.nodeName), Bytes.toBytes(i))));
						}
						addDoc(docInfo);
					}
				}
				scanner.close();
			} finally {
				table.close();
			}
		} finally {
			connection.close();
		}
	}
	
	public static int getNumOfDoc() {
		return numOfDoc;
	}
	
	public static double getFieldLength(String field) {
		return getFieldLength(Field.encode(field));
	}
	
	public static double getFieldLength(int fieldCode) {
		return fieldStats[fieldCode];
	}
	
	public static synchronized void addDoc(DocInfo docInfo) {
		numOfDoc++;
		for (int i=0; i<Field.NUM_OF_FIELDS; i++) {
			fieldStats[i] += (docInfo.getFieldLength(i)-fieldStats[i])/numOfDoc;
		}
	}
	
}
