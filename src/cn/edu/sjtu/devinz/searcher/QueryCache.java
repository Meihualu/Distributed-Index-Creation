package cn.edu.sjtu.devinz.searcher;

import java.io.IOException;

import redis.clients.jedis.Jedis;

class QueryCache {

    private static Jedis jedis = new Jedis("localhost");

    static {
        jedis.select(6);
    }

    public static DocResult[] load(QueryInfo queryInfo) {
        String key = queryInfo.toString();
        int len = jedis.llen(key).intValue();

        if (0 == len) {
            return null;
        } else {
            DocResult[] results = new DocResult[len];

            for (int i=0; i<len; i++) {
                results[i] = DocResult.valueOf(jedis.lindex(key, i));
            }
            return results;
        }
    }

    public static void store(QueryInfo queryInfo, DocResult[] results) {
        String key = queryInfo.toString();

        if (jedis.exists(key)) {
            jedis.del(key);
        }
        for (DocResult docResult : results) {
            jedis.rpush(key, docResult.toString());
        }
    }

    public static void main(String[] args) throws IOException {
        jedis.flushDB();
    }

}
