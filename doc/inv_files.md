## Inverted Files

### On Slave1:

	hadoop@slave2:~/.devin/index$ du -b *
	7008300	0.abstract
	7303400	0.author
	39292240	0.reference
	2819960	0.title
	1875960	1.abstract
	86464	1.author
	3856140	1.reference
	528820	1.title
	2430324	2.abstract
	64624	2.author
	3669720	2.reference
	309272	2.title
	2676492	3.abstract
	131404	3.author
	3845296	3.reference
	131404	3.title
	1327360	4.abstract
	311100	4.author
	3505060	4.reference
	186636	5.abstract
	62212	5.author
	3048388	5.reference
	2052908	6.reference
	hadoop@slave2:~/.devin/index$ du -b
	86527580	.

## Lengths of Posting Lists (on slave1):

    Mean:   186.49784424
    Median: 8.0
    Mode:   8
    Max:    1139256
    sum:    233191496
    y = -24.4057 + 1./(4.02575e-08*x+1.21388e-06)

![stats](slot_sizes.png)

