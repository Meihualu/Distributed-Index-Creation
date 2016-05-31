## Inverted Files

### On Slave1:

	hadoop@slave1:~/.devin/index$ du -sh
	2.8G	.
	hadoop@slave1:~/.devin/index$ du -sh *
	87M	0.abstract
	88M	0.author
	639M	0.reference
	30M	0.title
	16M	1.abstract
	11M	1.author
	119M	1.reference
	6.4M	1.title
	24M	2.abstract
	6.8M	2.author
	122M	2.reference
	9.4M	2.title
	41M	3.abstract
	6.2M	3.author
	151M	3.reference
	16M	3.title
	61M	4.abstract
	5.4M	4.author
	165M	4.reference
	18M	4.title
	80M	5.abstract
	4.9M	5.author
	179M	5.reference
	15M	5.title
	102M	6.abstract
	4.8M	6.author
	202M	6.reference
	8.3M	6.title
	90M	7.abstract
	8.4M	7.author
	279M	7.reference
	2.8M	7.title
	21M	8.abstract
	3.8M	8.author
	141M	8.reference
	13M	9.reference

### Lengths of Posting Lists (on slave1):

	Mean:	327.698632691
	Median:	8.0
	Mode:	8
	Max:	1651686
	sum:	1114498462
	y = -238.909 + 1./(4.99368e-09*x+1.12175e-06)

![stats](slot_sizes.png)

### On Slave2:

	hadoop@slave2:~/.devin/index$ du -sh
	3.7G	.
	hadoop@slave2:~/.devin/index$ du -sh *
	109M	0.abstract
	103M	0.author
	782M	0.reference
	37M	0.title
	19M	1.abstract
	17M	1.author
	154M	1.reference
	7.7M	1.title
	29M	2.abstract
	11M	2.author
	166M	2.reference
	12M	2.title
	50M	3.abstract
	9.3M	3.author
	202M	3.reference
	19M	3.title
	74M	4.abstract
	7.4M	4.author
	214M	4.reference
	24M	4.title
	98M	5.abstract
	6.9M	5.author
	234M	5.reference
	22M	5.title
	134M	6.abstract
	6.7M	6.author
	273M	6.reference
	15M	6.title
	146M	7.abstract
	9.5M	7.author
	305M	7.reference
	6.3M	7.title
	66M	8.abstract
	7.9M	8.author
	374M	8.reference
	1.1M	8.title
	3.3M	9.abstract
	1.6M	9.author
	38M	9.reference

