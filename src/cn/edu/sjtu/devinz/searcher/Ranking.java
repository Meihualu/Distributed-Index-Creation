package cn.edu.sjtu.devinz.searcher;

public interface Ranking {

	int calScore(QueryInfo queryInfo, Result result);
	
}

class MyRanking implements Ranking {
	
	public int calScore(QueryInfo queryInfo, Result result) {
		int score = 0;
		
		return score;
	}
	
}