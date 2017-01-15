package cn.edu.sjtu.devinz.indexer;

import java.io.Closeable;
import java.io.EOFException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileLock;
import java.util.ArrayList;

import org.apache.hadoop.hbase.util.Bytes;

abstract class PostIO implements Closeable {

    private final RandomAccessFile rf;
    private final FileLock fl;

    private final byte[] buffer = new byte[6*Postings.BLOCK];
    private int maxPtr = 0, bufPtr = 0;

    protected final int slotVol;
    protected final String zone, term;
    protected final long slotPos;

    protected int prevDocID = 0;

    private int slotSize;

    protected PostIO(String term, int postPos, int zoneCode, boolean write) 
        throws IOException {

        int partNO = Postings.getPartNO(postPos);
        slotVol = Postings.getSlotVolume(partNO);
        slotPos = 1L * Postings.getSlotNO(postPos) * slotVol;
        zone = Zones.decode(zoneCode);
        this.term = term;

        rf = new RandomAccessFile("/home/hadoop/.devin/index/"+partNO+"."+zone, 
                write? "rw" : "r");
        fl = rf.getChannel().lock(slotPos, slotVol, !write);

        readSlotSize();
    }

    @Override public String toString() {
        int partNO = 0, tmp = (slotVol-Postings.SIZE_LEN)/Postings.UNIT;
        while (tmp > 1) {
            tmp /= Postings.RADIX;
            partNO++;
        }
        return "[PostIO] "+partNO+"."+zone+" @ "+slotPos;
    }

    public Posting nextPosting() throws IOException {
        Posting post = null;
        if (!beyondEnd()) {
            String docURL = DocMeta.getURL(prevDocID+=nextInt());
            int numOfPoses = nextInt();

            post = new Posting(term, docURL, Zones.encode(zone),
                    new ArrayList<Integer>(numOfPoses));
            for (int i=0, prevPost=0; i<numOfPoses; i++) {
                post.poses.add(prevPost+=nextInt());
            }
        }
        return post;
    }

    public void close() throws IOException {
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
            rf.seek(slotPos+slotVol-1);	/* allocate space */
            rf.writeByte(0);
            writeSlotSize(Postings.SIZE_LEN);
        }
        bufPtr = maxPtr = 0;
    }

    protected void writeSlotSize(int size) throws IOException {
        rf.seek(slotPos);
        rf.write(Bytes.toBytes(size));
        if ((slotSize = size) > slotVol) {
            throw new RuntimeException("set slotSize "+slotSize+" greater than slotVol "+slotVol);
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
