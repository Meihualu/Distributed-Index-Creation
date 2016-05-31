#! /usr/bin/env python

from random import random
from random import randint
from numpy.linalg import inv
from numpy.random import rand
from scipy.optimize import leastsq
import numpy as np
import matplotlib.pylab as pl


def plot(stats,w):
    pl.title("Stats of Posting Lists")
    pl.ylabel("Sizes of PostList")
    pl.xlabel("Ranking of PostList Sizes")
    pl.xlim(-100,5000)
    pl.ylim(-1000,max(stats))
    pl.plot(range(1,len(stats)+1),stats, label='Measured Vals')
    y = []
    for x in range(1, len(stats)+1):
        y.append(w[2]+1./(w[0]*x+w[1]))
    pl.plot(range(1,len(stats)+1),y, label='Predicted Vals')
    pl.legend()
    pl.show()

def regress(stats):
    return leastsq(lambda w,y,x:y-w[2]-1./(w[0]*x+w[1]),
            [5e-7, 0,  0],
            args = (stats,np.array(range(0,len(stats)))+1))[0]

if __name__=='__main__':
    stats = []
    with open("data/stats.out", 'r') as fin:
        line = fin.readline()
        while line:
            size = int(line.split()[2])
            if size > 0:
                stats.append(size)
            line = fin.readline()
    stats.sort()
    print "Mean:\t"+str(np.mean(stats))
    print "Median:\t"+str(np.median(stats))
    print "Mode:\t"+str(np.argmax(np.bincount(stats)))
    print "Max:\t"+str(np.max(stats))
    print "sum:\t"+str(sum(stats))
    stats.reverse()
    w = regress(stats)
    print "y = %g + 1./(%g*x+%g)" %(w[2],w[0],w[1])
    plot(stats, w)

