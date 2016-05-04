package cn.edu.sjtu.acemap.indexer;


/**
 * This interface shall be implemented by Indexer Group
 * 		in package cn.edu.sjtu.acemap.indexer.
 * It shall provide a bunch of getters returning info about
 * 		a term itself, a doc itself, and the posting of
 * 		the term in the doc.
 * @author Nan Zuo
 *
 */

public class Record {	/** Facade Pattern */
	
	private final TermInfo termInfo;
	private final DocInfo docInfo;
	private final Posting posting;
	
	public Record(TermInfo termInfo, DocInfo docInfo, Posting posting) {
		this.termInfo = termInfo;
		this.docInfo = docInfo;
		this.posting = posting;
	}
	
	/**
	 * @return the field name
	 */
	public String getFieldName() {
		return posting.field;
	}
	
	/**
	 * @return the number of documents that contain the term desired
	 */
	public int getNumOfDoc() {
		return termInfo.numOfDoc;
	}
	
	/**
	 * @return the number of words in the given field of the document
	 */
	public int getFieldLength() {
		return docInfo.getFieldLength(posting.field);
	}
	
	/**
	 * @return the frequency of the term in the given field of the document
	 */
	public int getFreq() {
		return posting.getNumOfPoses();
	}
	
}
