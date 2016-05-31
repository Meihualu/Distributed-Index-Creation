# Distributed Inverted Index Creation Module

A Distributed Inverted Index Creation Module for an academic text search engine, 
which adopts the idea of [Geometrical Partitioning by Lester et al.](http://dl.acm.org/citation.cfm?id=1099739) 
and takes advantage of _Apache Hadoop_, _Apache HBase_ and _Redis_.

## Contents

	.
	├── bin
	├── data
	│   └── stats.out
	├── doc
	│   ├── doc_meta.md
	│   ├── inv_files.md
	│   ├── mapred.log
	│   ├── my_thesis.pdf
	│   ├── query_test.md
	│   ├── search_engine_db.gliffy
	│   ├── search_engine_db.jpg
	│   ├── slot_sizes.png
	│   └── term_dict.md
	├── lib  (myriad *.jar files from HBase and Jedis packages)
	├── README.md
	├── scripts
	│   └── stat.py
	└── src
		├── cn
		│   └── edu
		│       └── sjtu
		│           └── devinz
		│               ├── indexer
		│               │   ├── DocInfo.java
		│               │   ├── DocMeta.java
		│               │   ├── Indexer.java
		│               │   ├── Local.java
		│               │   ├── Merger.java
		│               │   ├── Posting.java
		│               │   ├── PostIO.java
		│               │   ├── PostPoses.java
		│               │   ├── PostReader.java
		│               │   ├── PostWriter.java
		│               │   ├── Statistics.java
		│               │   ├── TermDict.java
		│               │   ├── TermInfo.java
		│               │   └── Zones.java
		│               ├── mapred
		│               │   ├── IndexBuilder.java
		│               │   ├── IndexMapper.java
		│               │   └── IndexReducer.java
		│               └── searcher
		│                   ├── DocResult.java
		│                   ├── QueryCache.java
		│                   ├── QueryInfo.java
		│                   ├── SearchClient.java
		│                   ├── Searcher.java
		│                   └── SearchServer.java
		└── log4j.properties

(1) Package _cn.edu.sjtu.devinz.mapred_ launches a Hadoop MapReduce job to take in text analysis data and distribute them to multiple index servers.

(2) Package _cn.edu.sjtu.devinz.indexer_ manages the local index creation process on a single index server, where the local inverted files, the document info in Redis database, and the vocabulary in an HBase table would be updated incrementally.

(3) Package _cn.edu.sjtu.devinz.searcher_ implements a naive client-server system which could execute distributed ranked queries and return up to 20 results.

## Run the Tests

The distributed indexing module is designed to work on a Hadoop cluster in the laboratory of IIOT, SJTU.
At the time we launched the first round of index creation tests, the cluster only comprised one master node and two slave nodes, all of which possess 32 Intel(R) Xeon(R) CPU E5-2630 v3 @ 2.40GHz processors.
All the tests were run in Java Runtime Environment 1.8.0 and Hadoop Platform 2.5.1, along with HBase 1.4.1.
Text analysis data is stored in the HDFS directory _/TT/_, where each line contains a piece of info about a text document, just like the following picture.

![tt_data](doc/tt_data.png)

To create indexes, one should guarantee the host name of each index server is stored as a single line in _/etc/hostname_, 
and that the directory _/home/hadoop/.devin/_ exists and contains two subdirectories named "index" and "logs".
Local Redis server should be in place with DB 5 available, 
and an HBase table called "TermDict" should have been built with certain schema.
You can compress the binary code and all the necessary library files (HBase and Jedis) into one Jar file, 
and start the MapReduce job with a command as follow.

	hadoop jar Indexer.jar  cn.edu.sjtu.devinz.mapred.IndexBuilder 2> logs/mapred.log

Since index construction is time-consuming, running the program in background is strongly recommended.
You can open _logs/mapred.log_ on a regular basis to check the status of the MapReduce job.

To test the indexes you have built, you should first copy the Jar file from master to all slave nodes 
so that the programs could be run on each index server.
Then run the following command on each slave node to start all search servers.

	java -classpath Indexer.jar:${HADOOP_CLASSPATH} cn.edu.sjtu.devinz.searcher.SearchServer

After doing that, you can start a search client on the master node with the following command.

	java -classpath Indexer.jar:${HADOOP_CLASSPATH}  cn.edu.sjtu.devinz.searcher.SearchClient

Then an interpreter will get your query line by line and return up to 20 results for each query.

## Some Limits

(1) The interface between text analysis module and index creation module has not been wisely designed.
Text analysis data must be uploaded to HDFS before index construction, which constitutes a waste of disk storage.

(2) By adopting geometrical partitioning, we have increased the efficiency of index construction 
at the expense of worse time performance of query execution,
 due to the more disk seek operations caused by the fragmentation of posting lists.
Also, more disk storage is allocated but turns out to be idle, which is the cost of fast index updates.

(3) The distributed vocabulary is stored in HBase instead of in-memory databases, which can compromise time performance of both index construction and query execution.
At least, some caching strategies should be exploited to improve the situation.

(4) As regards phrase queries, skipping approach has not been supported by the current design, and shall be considered in the future.
