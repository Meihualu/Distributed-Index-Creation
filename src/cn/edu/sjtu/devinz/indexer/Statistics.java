package cn.edu.sjtu.devinz.indexer;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

public class Statistics {

	public static void main(String[] args) throws IOException {
		PrintWriter writer = new PrintWriter("logs/Statistics.out");
		
		try {
			TermDict dict = TermDict.getInstance();
			
			try {
				List<String> terms = dict.getTerms();
				
				for (String term : terms) {
					for (int zoneCode=0; zoneCode<Zones.NUM_OF_ZONES; zoneCode++) {
						TermInfo termInfo = dict.read(term, zoneCode);
						
						if (null != termInfo) {
							int totalSize = 0;
							
							for (Integer postPos : termInfo.postPoses) {
								PostReader reader = new PostReader(term, postPos, zoneCode);
								
								try {
									int slotSize = reader.getSlotSize();
									
									totalSize += slotSize-PostPoses.SIZE_LEN;
								} finally {
									reader.close();
								}
							}
							if (!termInfo.postPoses.isEmpty()) {
								writer.println(term+"\t"+Zones.decode(zoneCode)+"\t"+totalSize);
							}
						}
					}
				}
			} finally {
				dict.close();
			}
		} finally {
			writer.close();
		}
	}
	
}
