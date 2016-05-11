package cn.edu.sjtu.devinz.indexer;

import java.util.Random;

import redis.clients.jedis.Jedis;

public class DocMeta {
	
	private static final Jedis jedis = new Jedis("localhost");
	
	public static DocInfo read(String url) {
		DocInfo docInfo = new DocInfo(url);
		for (int i=0; i<Zones.NUM_OF_ZONES; i++) {
			String stat = jedis.hget("doc:"+url, Zones.decode(i));
			
			if (null == stat) {
				return null;
			} else {
				docInfo.zoneStats[i] = Integer.valueOf(stat);
			}
		}
		return docInfo;
	}
	
	public static void addZone(String url, String zone, int stat) {
		int zoneCode = Zones.encode(zone);
		
		if (zoneCode >= 0) {
			jedis.hset("doc:"+url, zone, Integer.toString(stat));
			DocSet.addZoneStat(zoneCode, stat);
		}
	}
	
	public static void main(String[] args) {
		Random rand = new Random();
		DocInfo[] docInfos = new DocInfo[1000];
		
		for (int i=0; i<docInfos.length; i++) {
			docInfos[i] = new DocInfo("URL_"+i);
			for (int j=0; j<Zones.NUM_OF_ZONES; j++) {
				docInfos[i].zoneStats[j] = rand.nextInt(10000);
				addZone(docInfos[i].URL, Zones.decode(j), docInfos[i].zoneStats[j]);
			}
		}
		for (int i=0; i<docInfos.length; i++) {
			DocInfo tmp = read(docInfos[i].URL);
			
			if (!tmp.equals(docInfos[i])) {
				throw new RuntimeException("Failure.");
			} else {
				jedis.del(docInfos[i].URL);
			}
		}
		System.out.println("All tests OK.");
	}
	
}
