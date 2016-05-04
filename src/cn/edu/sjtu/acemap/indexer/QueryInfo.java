package cn.edu.sjtu.acemap.indexer;

public class QueryInfo {
	
	public final String term, query;
	
	private int termMinPos = Integer.MIN_VALUE;
	private int termMaxPos = Integer.MAX_VALUE;
	
	public QueryInfo(String term, String query) {
		this.term = term;
		this.query = query;
	}
	
	/**
	 * Set the range of the term position in the doc
	 * @param termMinPos: set as Integer.MIN_VALUE if no lower bound;
	 * @param termMaxPos: set as Integer.MAX_VALUE if no upper bound;
	 */
	
	public void setPosRange(int termMinPos, int termMaxPos) {
		if (termMinPos <= termMaxPos) {
			this.termMinPos = termMinPos;
			this.termMaxPos = termMaxPos;
		} else {
			throw new IllegalArgumentException("termMinPos > termMaxPos");
		}
	}
	
	public int getMinPos() {
		return termMinPos;
	}
	
	public int getMaxPos() {
		return termMaxPos;
	}
	
}
