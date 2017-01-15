package cn.edu.sjtu.devinz.indexer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

class PostWriter extends PostIO {

    private PostWriter(String term, int postPos, int zoneCode) 
        throws IOException {

        super(term, postPos, zoneCode, true);
    }

    /** Write a Posting to the inverted files, add to TermDict if needed */

    public static void writePost(Posting post, TermInfo termInfo) 
        throws IOException {

        int space = post.lenOfBytes(0);	/* space is an upper bound */
        List<PostWriter> writers = new ArrayList<PostWriter>(16);
        PostWriter writer = null;
        for (Integer postPos : termInfo.postPoses) {
            writer = new PostWriter(post.term, postPos, post.zoneCode);
            writers.add(writer);
            if (space+writer.getSlotSize() >= writer.slotVol) {
                space += writer.getSlotSize()-Postings.SIZE_LEN;
            } else {
                space = -1;
                break;
            }
        }
        if (space >= 0) {
            int nextPartNO = writers.size();
            int postPos = newPostPos(post.zoneCode, nextPartNO++);
            termInfo.addPostPos(postPos);
            writer = new PostWriter(termInfo.value, postPos, post.zoneCode);
            while (space+writer.getSlotSize() >= writer.slotVol) {
                writer.close();
                postPos = newPostPos(post.zoneCode, nextPartNO++);
                termInfo.addPostPos(postPos);
                writer = new PostWriter(termInfo.value, postPos, post.zoneCode);
            }
        }
        try {
            merge(writer, writers, post);
        } finally {
            for (PostWriter pw : writers) {
                if (writer != pw) {
                    pw.writeSlotSize(Postings.SIZE_LEN);
                    pw.close();
                }
            }
            writer.close();
        }
    }

    private static void merge(PostWriter to, List<PostWriter> from, Posting post) 
        throws IOException {

        Posting[] heads = new Posting[from.size()+1];
        for (int i=0; i<from.size(); i++) {
            heads[i] = from.get(i).nextPosting();
        }
        heads[from.size()] = post;
        Merger merger = new Merger();
        int space = Postings.SIZE_LEN;
        while (true) {
            int i = -1;
            for (int j=0; j<heads.length; j++) {
                if (null != heads[j] && 
                        (i<0 || DocMeta.getDocID(heads[j].docURL)
                         < DocMeta.getDocID(heads[i].docURL))) {
                    i = j;
                         }
            }
            if (i >= 0) {
                space += merger.addPost(heads[i]);
                if (i < from.size()) {
                    heads[i] = from.get(i).nextPosting();
                } else {
                    heads[i] = null;
                }
            } else {
                break;
            }
        }

        to.writeSlotSize(space);
        while (merger.hasMoreBlocks()) {
            to.write(merger.nextBlock());
        }
    }
    
    private static int newPostPos(int zoneCode, int partNO) 
        throws FileNotFoundException, IOException {

        String fileName = "/home/hadoop/.devin/index/"+partNO+"."+Zones.decode(zoneCode);
        File file = new File(fileName);

        if (!file.exists()) {
            new FileOutputStream(fileName).close();
        }
        if (0 != file.length()%Postings.getSlotVolume(partNO)) {
            throw new RuntimeException("Invalid Inverted File");
        } else {
            return Postings.encode(fileName, 
                    (int)(file.length()/Postings.getSlotVolume(partNO)));
        }
    }

}
