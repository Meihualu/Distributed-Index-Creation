package cn.edu.sjtu.devinz.searcher;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Random;
import java.util.Scanner;
import java.util.StringTokenizer;

import cn.edu.sjtu.devinz.indexer.DocInfo;
import cn.edu.sjtu.devinz.indexer.DocMeta;
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
				
				if (null != termInfo) {
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
		}
		return docScores;
	}
	
	public DocResult[] search(QueryInfo queryInfo) throws IOException {
		Map<String,Double> queryScores = calQueryScores(queryInfo);
		Map<String,Map<String,Double>> docScores = calDocScores(queryInfo);
		PriorityQueue<DocResult> q = new PriorityQueue<DocResult>(NUM_TO_RETURN+1);
		for (String docURL : docScores.keySet()) {
			Map<String,Double> map = docScores.get(docURL);
			double dot = 0;
			for (String term : map.keySet()) {
				double docScore = map.get(term);
				
				dot += docScore * queryScores.get(term);
			}
			
			DocInfo docInfo = DocMeta.read(DocMeta.getDocID(docURL));
			if (null != docInfo) {
				int docLen = 0;
				for (int zoneCode=0; zoneCode<Zones.NUM_OF_ZONES; zoneCode++) {
					docLen += docInfo.zoneStats[zoneCode];
				}
				q.add(new DocResult(docURL, dot/docLen));
			} else {
				throw new RuntimeException("null == docInfo for "+docURL);
			}
			
			if (q.size() > NUM_TO_RETURN) {
				q.poll();
			}
		}
		
		DocResult[] docResults = new DocResult[q.size()];
		
		for (int i=q.size()-1; i>=0; i--) {
			docResults[i] = q.poll();
		}
		return docResults;
	}
	
	public static void main(String[] args) throws IOException {
		Random rand = new Random();
		List<String> dict = TermDict.getInstance().getTerms();
		Searcher searcher = new Searcher() {

			protected double evalQuery(String term, QueryInfo queryInfo) {
				double docFreq = 0;
				
				for (int zoneCode=0; zoneCode<Zones.NUM_OF_ZONES; zoneCode++) {
					try {
						TermInfo termInfo = TermDict.getInstance().read(term, zoneCode);
						
						if (null != termInfo) {
							docFreq += termInfo.docFreq;
						}
					} catch(IOException e) {
						e.printStackTrace();
					}
				}
				return docFreq>0? Math.log(1+DocMeta.getNumOfDocs()/docFreq):0;
			}

			protected double evalDoc(String term, String doc, double termFreq) {
				return termFreq>0? 1+Math.log(termFreq):0;
			}
			
		};
		long meanTime = 0;
		
		for (int test=0; test<500; test++) {
			QueryInfo queryInfo = new QueryInfo();
			int len = 1+rand.nextInt(1);
			
			for (int i=0; i<len; i++) {
				queryInfo.addTerm(dict.get(rand.nextInt(dict.size())));
			}
			
			long time  = System.currentTimeMillis();
			DocResult[] docResults = searcher.search(queryInfo);
			time = System.currentTimeMillis() - time;
			meanTime += (time-meanTime)/(test+1);
			
			System.out.println("Test "+test+":"+queryInfo);
			for (DocResult docResult : docResults) {
				System.out.println("\t"+docResult);
			}
			System.out.println();
		}
		System.out.println("Mean Search Time:\t"+meanTime+" ms");
		
		cmdTest(searcher);
	}
	
	public static void cmdTest(Searcher searcher) throws IOException {
		Scanner in = new Scanner(System.in);
		
		try {
			System.out.print("> ");
			System.out.flush();
			String line = in.nextLine();
			
			while (null!=line && line.trim().length()>0) {
				StringTokenizer toks = new StringTokenizer(line);
				QueryInfo queryInfo = new QueryInfo();
				
				while (toks.hasMoreTokens()) {
					queryInfo.addTerm(toks.nextToken());
				}
				DocResult[] docResults = searcher.search(queryInfo);
				for (DocResult docResult : docResults) {
					System.out.println("\t"+docResult);
				}
				System.out.println();
				
				System.out.print("> ");
				System.out.flush();
				line = in.nextLine();
			}
		} finally {
			in.close();
		}
	}
	
}
