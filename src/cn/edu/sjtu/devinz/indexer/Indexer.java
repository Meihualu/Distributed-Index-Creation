package cn.edu.sjtu.devinz.indexer;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Scanner;

import org.apache.commons.io.FileUtils;

public class Indexer {

    public static void addPost(String term, String url, int zoneCode, String[] poses) 
        throws IOException {

        //Local.log("Indexer.log", "addPost:\t"+term+"\t"+url+"\t"+Zones.decode(zoneCode));

        if (poses.length > 0) {
            TermDict dict = TermDict.getInstance();
            dict.incDocFreq(term);

            TermInfo termInfo = dict.read(term, zoneCode);
            if (null == termInfo) {
                throw new RuntimeException("No such term in HBase TermDict.");
            } else {
                Posting post = new Posting(term, url, zoneCode, new ArrayList<Integer>());

                for (String pos : poses) {
                    post.poses.add(Integer.valueOf(pos));
                }
                Collections.sort(post.poses);
                try {
                    PostWriter.writePost(post, termInfo);
                    dict.addPostPoses(term, zoneCode, termInfo.postPoses);
                } catch (IOException e) {
                    Local.log("Indexer.log", term+"\t"+url+":\t"+e);
                }
            }
        }
    }

    public static void main(String[] args) throws IOException {
        Scanner in = new Scanner(System.in);

        try {
            System.out.println("Are you sure to remove all local indexes? [Y/N]");
            String cmd = in.nextLine().trim();
            
            if (cmd.equals("Y") || cmd.equals("y")) {
            	FileUtils.cleanDirectory(new File("index/"));
				FileUtils.cleanDirectory(new File("logs/"));
                DocMeta.clear();
            }
        } finally {
            in.close();
        }
    }

}

