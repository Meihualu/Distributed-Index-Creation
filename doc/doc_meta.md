## 'DocMeta' in Redis:

### On slave1:

	127.0.0.1:6379> select 5
	OK
	127.0.0.1:6379[5]> get docSet:numOfDocs
	"3371762"
	127.0.0.1:6379[5]> hgetAll docSet:zoneStats
	1) "title"
	2) "8.569056665271196"
	3) "author"
	4) "5.571364466513457"
	5) "abstract"
	6) "100.5840511818807"
	7) "reference"
	8) "137.36630333714146"
	127.0.0.1:6379[5]> 

### On slave2:

	127.0.0.1:6379> select 5
	OK
	127.0.0.1:6379[5]> get docSet:numOfDocs
	"3363568"
	127.0.0.1:6379[5]> hgetAll docSet:zoneStats
	1) "title"
	2) "5.764083303397696"
	3) "author"
	4) "6.613572244605935"
	5) "abstract"
	6) "66.51221216523327"
	7) "reference"
	8) "228.448978194355"
	127.0.0.1:6379[5]> 


