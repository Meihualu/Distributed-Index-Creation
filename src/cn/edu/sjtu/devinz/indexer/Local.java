package cn.edu.sjtu.devinz.indexer;

import java.io.*;
import java.nio.channels.FileLock;
import java.sql.Timestamp;

public class Local {
	
	private static java.util.Date date = new java.util.Date();

	public static final String NODE_NAME;
	
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
			if (null == (NODE_NAME = line)) {
				System.exit(1);
			}
		}
		Local.log("Local.log", NODE_NAME);
	}
	
	public static void log(String fileName, String line) {
		//System.out.println(line);
		try {
			FileOutputStream fos = new FileOutputStream("/home/hadoop/.devin/logs/"+fileName, true);	/* append */
			try {
				PrintWriter out = new PrintWriter(fos);
				try {
					FileLock fl = fos.getChannel().lock();	/* blocking */
					try {
						out.write("["+new Timestamp(date.getTime())+"]\t"+line+"\n");
					} finally {
						fl.release();
					}
				} finally {
					out.close();
				}
			} finally {
				fos.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		for (int test=0; test<100; test++) {
			log("test.log", "This is test "+test+".");
		}
	}
	
}
