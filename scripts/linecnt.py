#! /usr/bin/env python

import os
import re


if __name__=='__main__':
    SRC_PATTERN = re.compile('\S*\.(java|c|cpp|py)',re.I)
    root_dir = os.path.join(os.path.dirname(os.path.realpath(__file__)), "../src")
    total_cnt = 0
    for dir_name, dirs, files in os.walk(root_dir):
        for file_name in files:
            if SRC_PATTERN.match(file_name):
                file_path = os.path.join(dir_name, file_name)
                cnt = 0
                with open(file_path) as fin:
                    line = fin.readline()
                    while line:
                        if len(line.strip()) > 0:
                            cnt += 1
                        line = fin.readline()
                print file_path,":\t",cnt
                total_cnt += cnt
    print "Totally:",total_cnt,"lines"

