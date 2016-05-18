package cn.edu.sjtu.devinz.indexer;

public class DocInfo {

    public final String URL;

    public final int[] zoneStats = new int[Zones.NUM_OF_ZONES];

    public DocInfo(String URL) {
        this.URL = URL;
    }

    public int getZoneLength(String zone) {
        int zoneCode = Zones.encode(zone);

        if (zoneCode >= 0) {
            return zoneStats[zoneCode];
        } else {
            return 0;
        }
    }

    public void setZoneLength(String zone, int length) {
        int zoneCode = Zones.encode(zone);

        if (zoneCode >= 0 && length >= 0) {
            zoneStats[zoneCode] = length;
        }
    }

    @Override public boolean equals(Object o) {
        if (!(o instanceof DocInfo)) {
            return false;
        } else {
            DocInfo other = (DocInfo) o;
            if (!URL.equals(other.URL)) {
                return false;
            } else {
                for (int i=0; i<Zones.NUM_OF_ZONES; i++) {
                    if (zoneStats[i] != other.zoneStats[i]) {
                        return false;
                    }
                }
                return true;
            }
        }
    }

    @Override public int hashCode() {
        return URL.hashCode();
    }

    @Override public String toString() {
        String str = "[DOC] "+URL;
        for (int i=0; i<zoneStats.length; i++) {
            str += " "+zoneStats[i];
        }
        return str;
    }

}
