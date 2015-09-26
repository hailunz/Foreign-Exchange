__author__ = 'hailunzhu'

import sys

path = "/Users/hailunzhu/cmu/course/11676/parseData/prep/2015-01/"

from os import walk
for (dirpath, dirnames, filenames) in walk(path):
    start = 0
    if filenames[0].startswith('.'):
        start += 1
    basefile = path + filenames[start]
    start+=1
    for i in range(start,len(filenames)):
        comparefile = path + filenames[i]
        writefile = basefile + '-' + filenames[i].split("-")[0]
        bf = open(basefile)
        cf = open(comparefile)
        wf = open(writefile,'w')

        for line in bf:
            col1 = line.split('\t')
            print col1[0]
            # print "q2"
            for line2 in cf:
                col2 = line2.split('\t')
                # print col2
                if (col1[0] == col2[0]):
                    col1.insert(-1,col2[6])
                    wf.write("\t".join(col1))
                    # print col1
                    break
                else:
                    continue

        wf.close()
        cf.close()
        bf.close()
        basefile = writefile