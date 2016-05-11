package cn.edu.sjtu.devinz.searcher;

public class Result implements Comparable<Result> {

	public final String url;
	
	public final double score;
	
	public Result(String url, double score) {
		this.url = url;
		this.score = score;
	}

	public int compareTo(Result arg0) {
		if (score < arg0.score) {
			return 1;
		} else if (score == arg0.score) {
			return 0;
		} else {
			return -1;
		}
	}
	
}
