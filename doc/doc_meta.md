## 'DocMeta' in Redis:

### On slave1:

	hadoop@slave1:~/.devin$ redis-cli
	127.0.0.1:6379> select 5
	OK
	127.0.0.1:6379[5]> get docSet:numOfDocs
	"1375184"
	127.0.0.1:6379[5]> hgetAll docSet:zoneStats
	1) "title"
	2) "10.67539582928134"
	3) "author"
	4) "7.6759536018522425"
	5) "abstract"
	6) "119.32954557404453"
	7) "reference"
	8) "289.4109379135587"
	127.0.0.1:6379[5]> 

### On slave2:

	hadoop@slave2:~/.devin/index$ redis-cli
	127.0.0.1:6379> select 5
	OK
	127.0.0.1:6379[5]> get docSet:numOfDocs
	"2027752"
	127.0.0.1:6379[5]> hgetAll docSet:zoneStats
	1) "title"
	2) "10.646250251496316"
	3) "author"
	4) "7.636815707016239"
	5) "abstract"
	6) "119.19937099621332"
	7) "reference"
	8) "286.5369775750765"
	127.0.0.1:6379[5]> 


