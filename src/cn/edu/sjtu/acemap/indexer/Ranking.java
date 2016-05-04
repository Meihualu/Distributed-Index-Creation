package cn.edu.sjtu.acemap.indexer;


public interface Ranking {
	
	/**
	 * @param record: a pair of term and document
	 * @param queryInfo: information of a query
	 * @return score of the doc with respect to the term & query
	 */
	
	int evalScore(Record record, QueryInfo queryInfo);
	
}
