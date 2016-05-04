package cn.edu.sjtu.acemap.indexer;

import java.io.*;
import java.nio.channels.FileLock;
import java.sql.Timestamp;

class LocalLog {
	
	private static java.util.Date date = new java.util.Date();

	public static void append(String fileName, String line) {
		System.out.println(line);
		try {
			FileOutputStream fos = new FileOutputStream("/home/hadoop/log/"+fileName, true);	/* append */
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
			append("test.log", "This is test "+test+".");
		}
	}
	
}
