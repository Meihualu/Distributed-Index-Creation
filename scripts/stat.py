#! /usr/bin/env python

from random import random
from random import randint
from numpy.linalg import inv
from numpy.random import rand
import numpy as np
import matplotlib.pylab as pl


def plot(stats, w):
    pl.title("Stats of Slot Sizes")
    pl.xlabel("size")
    pl.ylabel("density")
    pl.xlim(0,5000)
    pl.ylim(0,1000)
    pl.plot(stats, label='real slot size')
    y = []
    for x in range(1,len(stats)):
        y.append(w[0,0]+w[1,0]/x)
    pl.plot(range(1,len(stats)),y, label='predictive value')
    pl.legend()
    pl.show()

def regress(stats):
    LAMBDA = 0
    X = [[], []]
    y = []
    for idx in range(1, len(stats)):
        X[0].append(1)
        X[1].append(1./idx)
        y.append(stats[idx])
    X = np.mat(X)
    y = np.mat(y)
    return inv(LAMBDA*np.eye(X.shape[0])+X*X.transpose())*X*y.transpose()


if __name__=='__main__':
    stats = [0]*1000000
    line_cnt = 0
    total_size = 0
    fin = open("doc/test_stats.out", 'r')
    try:
        line = fin.readline()
        while line:
            line_cnt += 1
            size = int(line.split()[2])
            total_size += size
            stats[size] += 1
            line = fin.readline()
    finally:
        fin.close()
    print "Average Slot Size:\t"+str(total_size/line_cnt)
    print "Mode of Slot Sizes:\t"+str(stats.index(max(stats)))
    while not stats[-1]:
        del stats[-1]
    print "Maximum Slot Size:\t"+str(len(stats)-1)
    m_sum = 0
    for idx in range(len(stats)):
        m_sum += stats[idx]
        if m_sum >= line_cnt/2:
            print "Median of Slot Sizes:\t"+str(idx)
            break
    w = regress(stats)
    print "density = "+str(w[0,0])+" + "+str(w[1,0])+" / slot_size"
    plot(stats, w)

