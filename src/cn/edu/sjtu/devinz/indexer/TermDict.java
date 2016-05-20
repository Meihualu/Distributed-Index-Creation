package cn.edu.sjtu.devinz.indexer;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Admin;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.util.Bytes;

public class TermDict implements Closeable {

    private final Connection connection;

    private final Table table;

    private TermDict() throws IOException {
        connection = ConnectionFactory.createConnection(config);
        table = connection.getTable(TableName.valueOf("TermDict"));
    }

    public void close() throws IOException {
        try {
            table.close();
        } finally {
            connection.close();
        }
    }

    public TermInfo read(String value, int zoneCode) throws IOException {
        Result result = table.get(new Get(Bytes.toBytes(value)));
        if (null == result || result.isEmpty()) {
            return null;
        } else {
            TermInfo termInfo = new TermInfo(value, zoneCode, Bytes.toInt
                    (result.getValue(Bytes.toBytes("common"), Bytes.toBytes("docFreq"))));
            String postPoses = Bytes.toString(result.getValue
                    (Bytes.toBytes(Local.NODE_NAME), 
                     Bytes.toBytes(Zones.decode(zoneCode))));

            if (null != postPoses) {
                StringTokenizer postToks = new StringTokenizer(postPoses);
                while (postToks.hasMoreTokens()) {
                    termInfo.addPostPos(Integer.valueOf(postToks.nextToken()));
                }
            }
            return termInfo;
        }
    }

    public synchronized void incDocFreq(String term) throws IOException {
        Put put = new Put(Bytes.toBytes(term));

        Result result = table.get(new Get(Bytes.toBytes(term)));
        int docFreq = result.isEmpty()? 1 : Bytes.toInt(result.getValue(Bytes.toBytes("common"),
                    Bytes.toBytes("docFreq")))+1;

        put.addColumn(Bytes.toBytes("common"), Bytes.toBytes("docFreq"), Bytes.toBytes(docFreq));
        table.put(put);
    }

    public synchronized void addPostPoses(String term, int zoneCode, List<Integer> postPoses) throws IOException {
        if (postPoses.size() > 0) {
            StringBuffer sb = new StringBuffer();
            sb.append(postPoses.get(0));
            for (int i=1; i<postPoses.size(); i++) {
                sb.append(" ");
                sb.append(postPoses.get(i));
            }
            Put put = new Put(Bytes.toBytes(term));
            put.addColumn(Bytes.toBytes(Local.NODE_NAME), Bytes.toBytes(Zones.decode(zoneCode)), 
                    Bytes.toBytes(sb.toString()));
            table.put(put);
        }
    }

    public synchronized void clear() throws IOException {
        Admin admin = connection.getAdmin();
        admin.disableTable(TableName.valueOf("TermDict"));
        admin.truncateTable(TableName.valueOf("TermDict"), true);
    }

    private static final Configuration config = HBaseConfiguration.create();

    static {
        config.set("hbase.master", "hdfs://master:9000");
        config.set("hbase.zookeeper.quorum", "master,slave1,slave2");
    }

    private static TermDict instance;

    public static TermDict getInstance() throws IOException {
        if (null == instance) {
            synchronized (TermDict.class) {
                if (null == instance) {
                    instance = new TermDict();
                }
            }
        }
        return instance;
    }

    public static void main(String[] args) throws IOException {
        final int SIZE = 100;
        java.util.Random rand = new java.util.Random();
        TermDict dict = getInstance();

        try {
            dict.clear();
            List<TermInfo> terms = new ArrayList<TermInfo>(SIZE);
            for (int i=0; i<SIZE; i++) {
                TermInfo term = new TermInfo("term_"+i, rand.nextInt(Zones.NUM_OF_ZONES), 1+rand.nextInt(99));
                int n = 1+rand.nextInt(5);
                for (int j=0; j<n; j++) {
                    term.addPostPos(rand.nextInt(1000));
                }
                terms.add(term);
                for (int j=0; j<term.docFreq; j++) {
                    dict.incDocFreq(term.value);
                }
                dict.addPostPoses(term.value, term.zoneCode, term.postPoses);
                System.out.println(term);
            }
            System.out.println();

            for (int i=0; i<SIZE; i++) {
                TermInfo term = dict.read("term_"+i, terms.get(i).zoneCode);

                if (null == term) {
                    exit(1);
                } else if (!terms.get(i).equals(term)) {
                    exit(2);
                }
            }

            dict.clear();
            System.out.println("All tests OK.");
        } finally {
            dict.close();
        }
    }

    private static void exit(int argv) throws IOException {
        if (0 != argv) {
            throw new RuntimeException("Failure "+argv);
        } else {
            System.exit(0);
        }
    }

    public List<String> getTerms() throws IOException {
        ResultScanner scanner = table.getScanner(new Scan());

        try {
            List<String> lst = new ArrayList<String>();
            Result result = scanner.next();

            while (null != result) {
                String term = Bytes.toString(result.getRow());

                lst.add(term);
                result = scanner.next();
            }
            return lst;
        } finally {
            scanner.close();
        }
    }

}
