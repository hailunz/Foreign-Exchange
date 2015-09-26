__author__ = 'hailunzhu'

import sys
import zipfile
from os import walk

currentRow = None
currentTime = None
currentType = None

bidMax = sys.float_info.max  # max min average
bidMin = 0.0
bidAverage = 0.0
prevbid = 0.0
count = 0
feature = list()
spread = 0.0
path = "/Users/hailunzhu/cmu/course/11676/data/2015/"
wpath = "/Users/hailunzhu/cmu/course/11676/parseData/prep/2015-01/"

# for year in walk("/Users/hailunzhu/cmu/course/11676/data/2015/"):
for (dirpath, dirnames, filenames) in walk("/Users/hailunzhu/cmu/course/11676/data/2015"):
    currentRow = None
    currentTime = None
    currentType = None

    bidMax = sys.float_info.max  # max min average
    bidMin = 0.0
    bidAverage = 0.0
    prevbid = 0.0
    count = 0
    feature = list()
    spread = 0.0
    for f in filenames:
        if (f.endswith("01.zip")):
            f = dirpath+"/" + f
            print f
        else:
            continue
        print f
        zf = zipfile.ZipFile(f, 'r')
        ww = f.split("/")[-1].split(".zip")[0]
        wf = open(wpath+ww,'w')
        for zfile in zf.infolist():
            subfile = zf.open(zfile)

            for line in subfile:
                (money, datetime,bid,ask) = line.split(',')
                date = datetime.split()[0]
                time = datetime.split()[1].split(':')[0]
                f1 = date + '-' + time

                try:
                    bid = float(bid)
                except ValueError:
                    continue

                try:
                    ask = float(ask)
                except ValueError:
                    continue

                if currentRow == None:
                    currentRow = f1
                    currentType = money
                    bidMax = bid
                    bidMin = bid

                # old article
                if currentRow == f1:
                    bidMax = max(bidMax, bid)
                    bidMin = min(bidMin, bid)
                    bidAverage += bid
                    count += 1
                    spread += (ask - bid)/bid

                else:
                    #print currentRow
                    bidAverage = bidAverage/float(count)
                    spread = spread/float(count)

                    if feature:
                        if (bidAverage >= prevbid):
                            feature.append(1)
                        else:
                            feature.append(0)

                        tmpline = ''
                        #print feature
                        for a in feature:
                            if tmpline:
                                tmpline += '\t' + str(a)
                            else:
                                tmpline += str(a)
                            #print a,
                        #print
                        print tmpline
                        wf.writelines(tmpline+'\n')
                        feature = []

                    feature.append(currentRow)
                    feature.append(currentType)
                    feature.append(bidMax)
                    feature.append(bidMin)
                    feature.append(bidAverage)
                    feature.append(spread)

                    if (bidAverage >= prevbid):
                        feature.append(1)
                    else:
                        feature.append(0)

                    prevbid = bidAverage
                    # clear data

                    bidMax = bid  # max min average
                    bidMin = bid
                    bidAverage = bid
                    count = 1
                    currentType = money
                    currentRow = f1
                    spread = (ask - bid)/bid


            # add last row
            bidAverage = bidAverage/float(count)
            spread = spread/float(count)
            if (bidAverage >= prevbid):
                    feature.append(1)
            else:
                    feature.append(0)

            tmpline = ''
            for a in feature:
                if tmpline:
                    tmpline += '\t' + str(a)
                else:
                    tmpline += str(a)
                #print a,
            #print
            print tmpline

            subfile.close()
        wf.writelines(tmpline+'\n')
        wf.close()
        zf.close()





