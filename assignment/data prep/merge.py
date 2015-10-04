__author__ = 'hailunzhu'

import sys

p = "/Users/hailunzhu/cmu/course/11676/parseData/prep/"
year = "2013/"
files = ['AUDUSD', 'EURUSD', 'GBPUSD']
path = p + year
basefile = path + files[0]
start = 1

for i in range(start, len(files)):
    comparefile = path + files[i]
    writefile = basefile + '-' + files[i]
    bf = open(basefile)
    cf = open(comparefile)
    wf = open(writefile,'w')

    lines1 =[]
    lines2 = []
    for line in bf:
        lines1.append(line)
    for line in cf:
        lines2.append(line)

    j=0
    k=0
    len1 = len(lines1)
    len2 = len(lines2)

    while j<len1 and k<len2:

        col1 = lines1[j].split('\t')
        # print "q2"
        line2 = lines2[k]
        col2 = line2.split('\t')
        # print col2
        if (col1[0] == col2[0]):
            col1.insert(-1, col2[4])
            wf.write("\t".join(col1))
            j+=1
            k+=1
            # print col1
        elif (col1[0] < col2[0]):
            j+=1
        else:
            k+=1


    wf.close()
    cf.close()
    bf.close()
    basefile = writefile