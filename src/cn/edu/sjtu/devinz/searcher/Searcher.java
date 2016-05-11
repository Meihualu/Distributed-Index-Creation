package cn.edu.sjtu.devinz.searcher;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;

import cn.edu.sjtu.devinz.indexer.PostReader;
import cn.edu.sjtu.devinz.indexer.Posting;
import cn.edu.sjtu.devinz.indexer.TermDict;
import cn.edu.sjtu.devinz.indexer.TermInfo;
import cn.edu.sjtu.devinz.indexer.Zones;

public abstract class Searcher {

	public static final int NUM_TO_RETURN = 20;
	
	protected abstract double evalQuery(String term, QueryInfo queryInfo);
	
	private Map<String,Double> calQueryScores(QueryInfo queryInfo) {
		Map<String, Double> queryScores = new HashMap<String, Double>();
		
		for (String term : queryInfo.list()) {
			queryScores.put(term, evalQuery(term, queryInfo));
		}
		return queryScores;
	}
	
	protected abstract double evalDoc(String term, String doc, double termFreq);
	
	private static Map<String,Map<String,Double>> getTermFreqs(QueryInfo queryInfo) 
			throws IOException {
		
		Map<String, Map<String, Double>> termFreqs = new HashMap<String, Map<String, Double>>();
		
		for (String term : queryInfo.list()) {
			for (int zoneCode=0; zoneCode<Zones.NUM_OF_ZONES; zoneCode++) {
				TermInfo termInfo = TermDict.getInstance().read(term, zoneCode);
				
				for (Integer postPos : termInfo.postPoses) {
					PostReader reader = new PostReader(term, postPos, zoneCode);
					
					try {
						Posting post = reader.nextPosting();
						
						while (null != post) {
							double freq = 0;
							
							if (!termFreqs.containsKey(post.docURL)) {
								Map<String, Double> map = new HashMap<String, Double>();
								
								for (String termKey : queryInfo.list()) {
									map.put(termKey, .0);
								}
								termFreqs.put(post.docURL, map);
							} else {
								freq = termFreqs.get(post.docURL).get(term);
							}
							termFreqs.get(post.docURL).put(term, 
									freq+Zones.weights[zoneCode]*post.poses.size());
							post = reader.nextPosting();
						}
					} finally {
						reader.close();
					}
				}
			}
		}
		for (String docURL : termFreqs.keySet()) {
			Map<String,Double> map = termFreqs.get(docURL);
			
			for (String term : map.keySet()) {
				map.put(term, map.get(term)*Zones.NUM_OF_ZONES);
			}
		}
		return termFreqs;
	}
	
	private Map<String,Map<String,Double>> calDocScores(QueryInfo queryInfo) 
			throws IOException {
		
		Map<String,Map<String,Double>> docScores = getTermFreqs(queryInfo);
		
		for (String docURL : docScores.keySet()) {
			Map<String,Double> map = docScores.get(docURL);
			
			for (String term : map.keySet()) {
				map.put(term, evalDoc(docURL, term, map.get(term)));
			}
			for (String term : map.keySet()) {
				map.put(term, map.get(term));
			}
		}
		return docScores;
	}
	
	public PriorityQueue<Result> search(QueryInfo queryInfo) 
			throws IOException {
		
		Map<String,Double> queryScores = calQueryScores(queryInfo);
		Map<String,Map<String,Double>> docScores = calDocScores(queryInfo);
		PriorityQueue<Result> q = new PriorityQueue<Result>();
		
		for (String docURL : docScores.keySet()) {
			Map<String,Double> map = docScores.get(docURL);
			double len = 0, dot = 0;
			
			for (String term : map.keySet()) {
				double docScore = map.get(term);
				
				dot += docScore * queryScores.get(term);
				len += docScore * docScore;
			}
			q.add(new Result(docURL, dot/Math.sqrt(len)));
		}
		return q;
	}
	
}
