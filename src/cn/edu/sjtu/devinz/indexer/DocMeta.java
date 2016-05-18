package cn.edu.sjtu.devinz.indexer;

import java.util.Random;

import redis.clients.jedis.Jedis;

public class DocMeta {

    private static final Jedis jedis = new Jedis("localhost");

    static {
        jedis.select(5);
        if (null == jedis.get("docSet:numOfDocs")) {
            clear();
        }
    }

    public static DocInfo read(int docID) {
        String url = getURL(docID);

        if (null == url) {
            return null;
        } else {
            DocInfo docInfo = new DocInfo(url);

            for (int i=0; i<Zones.NUM_OF_ZONES; i++) {
                String stat = jedis.hget("doc:"+docID, Zones.decode(i));

                if (null != stat) {
                    docInfo.zoneStats[i] = Integer.valueOf(stat);
                }
            }
            return docInfo;
        }
    }

    public synchronized static void addDoc(String url) {
        jedis.set("docSet:numOfDocs", Integer.toString(getNumOfDocs()+1));
        jedis.hset("doc:"+getDocID(url), "URL", url);
    }

    public synchronized static void addZone(String url, String zone, int stat) {
        int zoneCode = Zones.encode(zone);

        if (zoneCode >= 0) {
            double avg = Double.valueOf(jedis.hget("docSet:zoneStats", zone));

            jedis.hset("docSet:zoneStats", zone, Double.toString(avg+(stat-avg)/getNumOfDocs()));
            jedis.hset("doc:"+getDocID(url), zone, Integer.toString(stat));
        }
    }

    public static double getAvgZoneLength(String zone) {
        return getAvgZoneLength(Zones.encode(zone));
    }

    public static double getAvgZoneLength(int zoneCode) {
        if (zoneCode>=0 && zoneCode<Zones.NUM_OF_ZONES) {
            return Integer.valueOf(jedis.hget("docSet:zoneStats", Zones.decode(zoneCode)));
        } else {
            return 0;
        }
    }

    public static int getNumOfDocs() {
        return Integer.valueOf(jedis.get("docSet:numOfDocs"));
    }

    public static int getDocID(String url) {
        return Integer.MAX_VALUE & url.hashCode();
    }

    public static String getURL(int docID) {
        return jedis.hget("doc:"+docID, "URL");
    }

    public synchronized static void clear() {
        jedis.flushDB();
        jedis.set("docSet:numOfDocs", "0");
        for (int zoneCode=0; zoneCode<Zones.NUM_OF_ZONES; zoneCode++) {
            jedis.hset("docSet:zoneStats", Zones.decode(zoneCode), "0");
        }
    }

    public static void main(String[] args) {
        clear();

        Random rand = new Random();
        DocInfo[] docInfos = new DocInfo[1000];

        for (int i=0; i<docInfos.length; i++) {
            docInfos[i] = new DocInfo("URL_"+i);
            addDoc(docInfos[i].URL);
            for (int j=0; j<Zones.NUM_OF_ZONES; j++) {
                docInfos[i].zoneStats[j] = rand.nextInt(10000);
                addZone(docInfos[i].URL, Zones.decode(j), docInfos[i].zoneStats[j]);
            }
        }
        for (int i=0; i<docInfos.length; i++) {
            DocInfo tmp = read(getDocID(docInfos[i].URL));

            if (null==tmp || !tmp.equals(docInfos[i])) {
                throw new RuntimeException("Failure.");
            }
        }
        if (getNumOfDocs() != docInfos.length) {
            throw new RuntimeException("Failure.");
        } else {
            clear();
            System.out.println("All tests OK.");
        }
    }

}
