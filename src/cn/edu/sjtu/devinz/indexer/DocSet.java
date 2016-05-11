package cn.edu.sjtu.devinz.indexer;

import redis.clients.jedis.Jedis;

public class DocSet {
	
	private static int numOfDoc = 0;
	
	private static double[] zoneStats = new double[Zones.NUM_OF_ZONES];
	
	private static Jedis jedis = new Jedis("localhost");
	
	static {
		String result = jedis.get("docSet:numOfSet");
		if (null == result) {
			numOfDoc = 0;
		} else {
			numOfDoc = Integer.valueOf(result);
			for (int i=0; i<Zones.NUM_OF_ZONES; i++) {
				zoneStats[i] = Double.valueOf(jedis.hget("docSet:zoneStats", Zones.decode(i)));
			}
		}
	}
	
	public static synchronized void addZoneStat(int zoneCode, int stat) {
		zoneStats[zoneCode] += (stat-zoneStats[zoneCode])/numOfDoc;
		jedis.hset("docSet:zoneStats", Zones.decode(zoneCode), 
				Double.toString(zoneStats[zoneCode]));
	}
	
	public static synchronized void incNumOfDoc() {
		jedis.set("docSet:numOfSet", Integer.toString(++numOfDoc));
	}
	
	public static double getZoneLength(int zoneCode) {
		if (zoneCode>=0 && zoneCode<Zones.NUM_OF_ZONES) {
			return zoneStats[zoneCode];
		} else {
			return 0;
		}
	}
	
	public static double getZoneLength(String zone) {
		return getZoneLength(Zones.encode(zone));
	}
	
	public static int getNumOfDoc() {
		return numOfDoc;
	}
	
}
