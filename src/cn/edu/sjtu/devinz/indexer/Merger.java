package cn.edu.sjtu.devinz.indexer;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

class Merger {

	public static final int BUF_SIZE = (1<<13);
	
	private final byte[] buffer = new byte[BUF_SIZE];
	
	private static class Dumper implements Closeable {
		
		private final String DUMP_FILE;
		private FileOutputStream fout;
		private FileInputStream fin;
		
		public Dumper(String DUMP_FILE) {
			this.DUMP_FILE = DUMP_FILE;
		}
		
		public FileOutputStream getFileOutputStream() throws IOException {
			if (null == fout) {
				synchronized (this) {
					if (null == fout) {
						fout = new FileOutputStream(DUMP_FILE);
					}
				}
			}
			return fout;
		}
		
		public FileInputStream getFileInputStream() throws IOException {
			if (null == fin) {
				synchronized (this) {
					if (null == fin) {
						if (null != fout) {
							fout.close();
							fout = null;
						}
						fin = new FileInputStream(DUMP_FILE);
					}
				}
			}
			return fin;
		}
		
		public void close() throws IOException {
			if (null != fin) {
				fin.close();
				File file = new File(DUMP_FILE);
				if (file.exists()) {
					file.delete();
				}
			}
		}
		
	}
	
	private int len = 0, prevDocID = 0, pos = 0;
	private Dumper dumper;
	
	public synchronized int addPost(Posting post) throws IOException {
		int size = post.lenOfBytes(prevDocID);
		byte[] bytes = post.toBytes(size, prevDocID);
		
		prevDocID = DocMeta.getDocID(post.docURL);
		for (int i=0, num=0; i<size; i+=num) {
			if (len == BUF_SIZE) {
				if (null == dumper) {
					dumper = new Dumper(this+".swap");
				}
				dumper.getFileOutputStream().write(buffer);
				len = 0;
			}
			num = Math.min(PostPoses.BLOCK, bytes.length-i);
			if (0 < len%PostPoses.BLOCK) {
				num = Math.min(PostPoses.BLOCK*(len/PostPoses.BLOCK+1)-len, num);
			}
			System.arraycopy(bytes, i, buffer, len, num);
			len += num;
		}
		return size;
	}
	
	
	public synchronized byte[] nextBlock() throws IOException {
		if (null != dumper) {
			byte[] bytes = new byte[PostPoses.BLOCK];
			
			if (dumper.getFileInputStream().read(bytes) == bytes.length) {
				return bytes;
			} else {
				dumper.close();
				dumper = null;
			}
		}
		byte[] bytes = new byte[Math.min(PostPoses.BLOCK, len-pos)];
		
		System.arraycopy(buffer, pos, bytes, 0, bytes.length);
		pos += bytes.length;
		return bytes;
	}
	
	public boolean hasMoreBlocks() {
		return pos < len;
	}
	
	public static void main(String[] args) throws IOException {
		Random rand = new Random();

		DocMeta.addDoc("test_url");
		try {
			Merger merger = new Merger();
			for (int i=0; i<(1<<20); i++) {
				Posting post = new Posting("test_term", "test_url", 0, 
						new ArrayList<Integer>());
				
				for (int j=0; j<1+rand.nextInt(20); j++) {
					post.poses.add(j);
				}
				merger.addPost(post);
			}
			while (merger.hasMoreBlocks()) {
				merger.nextBlock();
			}
			System.out.println("All tests OK.");
		} finally {
			DocMeta.clear();
		}
	}
	
}
