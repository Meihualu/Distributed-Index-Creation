package cn.edu.sjtu.devinz.searcher;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class QueryInfo {

    private int pos = 0;

    private Map<String,List<Integer>> terms;

    public QueryInfo() {
        terms = new HashMap<String,List<Integer>>();
    }

    public void addTerm(String term) {
        if (!terms.containsKey(term)) {
            terms.put(term, new ArrayList<Integer>(5));
        }
        terms.get(term).add(pos++);
    }

    public int getTermFreq(String term) {
        List<Integer> lst = terms.get(term);

        if (null == lst) {
            return 0;
        } else {
            return lst.size();
        }
    }

    public Iterable<String> list() {
        return terms.keySet();
    }

    @Override public String toString() {
        if (terms.isEmpty()) {
            return "";
        } else {
            StringBuffer sb = new StringBuffer();

            for (String term : terms.keySet()) {
                sb.append(" ");
                sb.append(term);
            }
            return sb.toString();
        }
    }

}
