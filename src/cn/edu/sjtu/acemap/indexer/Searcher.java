package cn.edu.sjtu.acemap.indexer;

import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.util.Bytes;


/**
 * This interface shall be implemented by Indexer Group
 * 		in package cn.edu.sjtu.acemap.indexer.
 * @author Nan Zuo
 *
 */

public class Searcher {
	
	private int minScore = Integer.MIN_VALUE;
	private int maxScore = Integer.MAX_VALUE;
	
	/**
	 * Limit the score range of search results, so as to improve search efficiency.
	 * @param minScore: set as Integer.MIN_VALUE if no lower bound
	 * @param maxScore: set as Integer.MAX_VALUE if no upper bound
	 */
	public void setThld(int minScore, int maxScore) {
		if (minScore > maxScore) {
			throw new IllegalArgumentException("minScore > maxScore");
		} else {
			this.minScore = minScore;
			this.maxScore = maxScore;
		}
	}

	/**
	 * @param ranking: a ranking strategy
	 * @param queryInfo: information of a query
	 * @return a list of Records within the score range
	 */
	public List<Record> search(Ranking ranking, QueryInfo queryInfo) {
		try {
			List<Record> lst = new LinkedList<Record>();
			TermInfo termInfo = TermDict.getInstance().read(queryInfo.term);
			if (null != termInfo) {
				for (Integer postPos : termInfo.postPoses) {
					PostReader reader = new PostReader(postPos, queryInfo);
					try {
						Posting post = reader.nextPosting();
						while (null != post) {
							DocInfo docInfo = DocMeta.getInstance().read(post.docURL);
							Record record = new Record(termInfo, docInfo, post);
							int score = ranking.evalScore(record, queryInfo);
							if (score>=minScore && score<=maxScore) {
								lst.add(record);
							}
							post = reader.nextPosting();
						}
					} finally {
						reader.close();
					}
				}
			}
			return lst;
		} catch (IOException e) {
			e.printStackTrace();
			LocalLog.append("Searcher.log", e.toString());
			return null;
		}
	}
	
	public static void main(String[] args) throws IOException {
		Searcher searcher = new Searcher();
		Ranking rank = new Ranking() {
			public int evalScore(Record record, QueryInfo queryInfo) {
				return 0;
			}
		};
		Set<String> terms = getTerms(1000);
		int test = 0;
		for (String term : terms) {
			System.out.println("Test "+(test++)+": "+term+"\t"+searcher.search(rank, 
					new QueryInfo(term, null)).size());
		}
		System.out.println("\nAll tests OK.");
	}
	
	
	private static Set<String> getTerms(int num) throws IOException {
		Configuration config = HBaseConfiguration.create();
		Connection conn = ConnectionFactory.createConnection(config);
		try {
			Table table = conn.getTable(TableName.valueOf("TermDict"));
			try {
				ResultScanner scanner = table.getScanner(new Scan());
				try {
					Result[] res = scanner.next(num);
					Set<String> set = new HashSet<String>();
					for (int i=0; i<num; i++) {
						if (res[i] != null) {
							set.add(Bytes.toString(res[i].getRow()));
						}
					}
					return set;
				} finally {
					scanner.close();
				}
			} finally {
				table.close();
			}
		} finally {
			conn.close();
		}
	}
	
}
