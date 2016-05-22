package cn.edu.sjtu.devinz.searcher;

public class DocResult implements Comparable<DocResult> {

    public final String url;

    public final double score;

    public DocResult(String url, double score) {
        this.url = url;
        this.score = score;
    }

    public int compareTo(DocResult arg0) {
        if (score < arg0.score) {
            return -1;
        } else if (score == arg0.score) {
            return 0;
        } else {
            return 1;
        }
    }

    @Override public String toString() {
        return url+":\t"+score;
    }

    public static DocResult valueOf(String result) {
        String[] arr = result.trim().split("\t");

        if (2 != arr.length) {
            return null;
        } else {
            return new DocResult(arr[0].substring(0,arr[0].length()-1),
                    Double.valueOf(arr[1]));
        }
    }

}
