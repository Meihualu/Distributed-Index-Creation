package cn.edu.sjtu.devinz.searcher;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.PriorityQueue;
import java.util.Scanner;
import java.util.Stack;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class SearchClient implements Runnable {

    public final String serverName, query;

    public SearchClient(String serverName, String query) {
        this.serverName = serverName;
        this.query = query;
    }

    public void run() {
        try {
            Socket sock = new Socket(serverName, SearchServer.PORT_NO);

            try {
                BufferedReader in = new BufferedReader
                    (new InputStreamReader(sock.getInputStream()));

                try {
                    PrintWriter out = new PrintWriter
                        (new OutputStreamWriter(sock.getOutputStream()));

                    try {
                        out.println(query);
                        out.flush();
                        int num = Integer.parseInt(in.readLine());
                        out.println("ACK");
                        out.flush();
                        for (int i=0; i<num; i++) {
                            synchronized(queue) {
                                queue.add(new LocalResult(serverName, in.readLine()));
                                if (queue.size() > Searcher.NUM_TO_RETURN) {
                                	queue.poll();
                                }
                            }
                            out.println("ACK");
                            out.flush();
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
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static class LocalResult implements Comparable<LocalResult> {
        public final DocResult docResult;
        public final String serverName;

        public LocalResult(String serverName, String result) {
            this.serverName = serverName;
            docResult = DocResult.valueOf(result);
        }

        public int compareTo(LocalResult o) {
            return docResult.compareTo(o.docResult);
        }

        @Override public String toString() {
            return "["+serverName+"]\t"+docResult;
        }

    }

    private static PriorityQueue<LocalResult> queue = 
        new PriorityQueue<LocalResult>(Searcher.NUM_TO_RETURN+1);

    private static final String[] SLAVES = {
        "slave1",
        "slave2"
    };

    public static void main(String[] args) throws InterruptedException {
        Scanner in = new Scanner(System.in);

        try {
            System.out.print("> ");
            System.out.flush();
            String query = in.nextLine().trim();

            while (query.length() > 0) {
            	long start = System.currentTimeMillis();
                ExecutorService pool = Executors.newFixedThreadPool(SLAVES.length);
                queue.clear();
                for (String slave : SLAVES) {
                    pool.execute(new SearchClient(slave, query));
                }
                pool.shutdown();
                if (pool.awaitTermination(60, TimeUnit.SECONDS)) {
                	System.out.println("Search Time:\t"+(System.currentTimeMillis()-start)+" ms.");
                	Stack<LocalResult> stack = new Stack<LocalResult>();
                	while (!queue.isEmpty()) {
                		stack.add(queue.poll());
                	}
                    while (!stack.isEmpty()) {
                    	System.out.println(stack.pop());
                    }
                }

                System.out.print("> ");
                System.out.flush();
                query = in.nextLine().trim();
            }
        } finally {
            in.close();
        }
    }

}
