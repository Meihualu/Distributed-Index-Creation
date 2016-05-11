package cn.edu.sjtu.devinz.indexer;

public class ZoneRec {

	private TermInfo termInfo;
	private DocInfo docInfo;
	private Posting posting;
	
	public ZoneRec(TermInfo termInfo, DocInfo docInfo, Posting posting) {
		this.termInfo = termInfo;
		this.docInfo = docInfo;
		this.posting = posting;
	}
	
	/** @return the number of documents containing the term */
	
	public int getDocFreq() {
		return termInfo.docFreq;
	}
	
	/** @return the frequency of the term in the given zone of the doc */
	
	public int getTermFreq() {
		return posting.poses.size();
	}
	
	/** @return the length of the given zone of the doc */
	
	public int getZoneLength() {
		return docInfo.zoneStats[termInfo.zoneCode];
	}
	
}
