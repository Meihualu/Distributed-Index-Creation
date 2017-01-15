package cn.edu.sjtu.devinz.searcher;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.StringTokenizer;

import cn.edu.sjtu.devinz.indexer.DocMeta;
import cn.edu.sjtu.devinz.indexer.TermDict;
import cn.edu.sjtu.devinz.indexer.TermInfo;
import cn.edu.sjtu.devinz.indexer.Zones;

class SearchServer {

	private SearchServer() {}
	
    public static final int PORT_NO = 10086;

    private static final Searcher searcher = new Searcher() {

        protected double evalQuery(String term, QueryInfo queryInfo) {
            double docFreq = 0;

            for (int zoneCode=0; zoneCode<Zones.NUM_OF_ZONES; zoneCode++) {
                try {
                    TermInfo termInfo = TermDict.getInstance().read(term, zoneCode);

                    if (null != termInfo) {
                        docFreq += termInfo.docFreq;
                    }
                } catch(IOException e) {
                    e.printStackTrace();
                }
            }
            return docFreq>0? Math.log(1+DocMeta.getNumOfDocs()/docFreq):0;
        }

        protected double evalDoc(String term, String doc, double termFreq) {
            return termFreq>0? 1+Math.log(termFreq):0;
        }

    };

    private static DocResult[] search(QueryInfo queryInfo) throws IOException {
        DocResult[] results = QueryCache.load(queryInfo);

        if (null != results) {
            return results;
        } else {
            long start = System.currentTimeMillis();

            results = searcher.search(queryInfo);
            if (System.currentTimeMillis()-start > 2000) {
                QueryCache.store(queryInfo, results);
            }
            return results;
        }
    }

    public static void main(String[] args) throws IOException {
        TermDict dict = TermDict.getInstance();

        try {
            ServerSocket serverSock = new ServerSocket(PORT_NO);

            try {
                while (true) {
                    Socket sock = serverSock.accept();

                    try {
                        BufferedReader in = new BufferedReader(new InputStreamReader(sock.getInputStream()));

                        try {
                            String query = in.readLine();
                            System.out.println("query:\t"+query);
                            StringTokenizer toks = new StringTokenizer(query);
                            QueryInfo queryInfo = new QueryInfo();

                            while (toks.hasMoreTokens()) {
                                queryInfo.addTerm(toks.nextToken());
                            }

                            DocResult[] docResults = search(queryInfo);
                            PrintWriter out = new PrintWriter(new OutputStreamWriter(sock.getOutputStream()));

                            try {
                                out.println(docResults.length);
                                out.flush();
                                System.out.println(in.readLine());
                                for (DocResult docResult : docResults) {
                                    out.println(docResult);
                                    out.flush();
                                    System.out.println(docResult);
                                    System.out.println(in.readLine());
                                }
                            } finally {
                                out.close();
                            }
                        } finally {
                            in.close();
                        }
                    } finally {
                        sock.close();
                    }
                }
            } finally {
                serverSock.close();
            }
        } finally {
            dict.close();
        }
    }

}
