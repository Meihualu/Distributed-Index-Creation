package cn.edu.sjtu.acemap.indexer;

import java.io.Closeable;
import java.io.EOFException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileLock;

import org.apache.hadoop.hbase.util.Bytes;

abstract class PostIO implements Closeable {
	
	private final RandomAccessFile rf;
	private final FileLock fl;
	
	private byte[] buffer = new byte[1024];
	private int maxPtr = 0, bufPtr = 0;
	
	protected final String field;
	protected final int slotVol;
	protected final long slotPos;
	
	private int slotSize;
	
	protected PostIO(int postPos, boolean write) throws IOException {
		int partNO = Posting.getPartNO(postPos);
		slotVol = Posting.getSlotVolume(partNO);
		slotPos = 1L * Posting.getPostNO(postPos) * slotVol;
		field = Field.decode(Posting.getFieldCode(postPos));
		if (null == field) {
			System.out.println("invalid field code");
			System.exit(1);
		}
		
		rf = new RandomAccessFile("/home/hadoop/index/"+partNO+"."+field, 
				write? "rw" : "r");
		fl = rf.getChannel().lock(slotPos, slotVol, !write);
		
		readSlotSize();
	}
	
	@Override public String toString() {
		int partNO = 0, tmp = (slotVol-Posting.SIZE_LEN)/Posting.UNIT;
		while (tmp > 1) {
			tmp /= Posting.RADIX;
			partNO++;
		}
		return "[PostIO] "+partNO+"."+field+" @ "+slotPos;
	}
	
	public abstract Posting nextPosting() throws IOException;
	
	@Override public void close() throws IOException {
		try {
			fl.release();
		} finally {
			rf.close();
		}
	}
	
	protected boolean beyondEnd() throws IOException {
		return rf.getFilePointer()-maxPtr+bufPtr >= slotPos+slotSize;
	}
	
	protected void jumpToEnd() throws IOException {
		rf.seek(slotPos+slotSize);
	}
	
	protected void readSlotSize() throws IOException {
		rf.seek(slotPos);
		try {
			slotSize = rf.readInt();
		} catch (EOFException e) {
			writeSlotSize(Posting.SIZE_LEN);
		}
		bufPtr = maxPtr = 0;
	}
	
	protected void writeSlotSize(int size) throws IOException {
		rf.seek(slotPos);
		rf.write(Bytes.toBytes(size));
		if ((slotSize = size) > slotVol) {
			throw new RuntimeException("set slotSize greater than slotVol");
		}
	}
	
	protected int getSlotSize() {
		return slotSize;
	}
	
	protected String nextString() throws IOException {
		if (!beyondEnd()) {
			Byte b = nextByte();
			StringBuffer sb = new StringBuffer();
			while (null != b) {
				if (b == (byte)'\0') {
					return sb.toString();
				} else {
					sb.append((char)(b&0xFF));
					b = nextByte();
				}
			}
		}
		throw new IOException("Fail to read String");
	}
	
	protected Integer nextInt() throws IOException {
		if (!beyondEnd()) {
			Byte b = nextByte();
			int val = 0;
			while (null != b) {
				val = (val<<7) + (127 & b);
				if (0 != (128 & b)) {	/* last byte starts with 1 */
					return val;
				} else {
					b = nextByte();
				}
			}
		}
		throw new IOException("Fail to read Integer ");
	}
	
	private Byte nextByte() throws IOException {
		if (bufPtr == maxPtr) {
			maxPtr = rf.read(buffer);
			bufPtr = 0;
		}
		if (bufPtr >= maxPtr) {
			return null;
		} else {
			return buffer[bufPtr++];
		}
	}
	
	protected void write(byte[] bytes) throws IOException {
		bufPtr = maxPtr = 0;
		rf.write(bytes);
	}
	
}
