__author__ = 'hailunzhu'

path = "/Users/hailunzhu/cmu/course/11676/parseData/output1"
wf = "/Users/hailunzhu/cmu/course/11676/parseData/origin"
trainpath = "/Users/hailunzhu/cmu/course/11676/parseData/train"
testpath = "/Users/hailunzhu/cmu/course/11676/parseData/test"

count = 0

# convert feature value to 0 or 1
def getBinary():
    p = open(path)
    f1 = open(wf,'w')
    sum = [0]*3
    count = 0
    for l in p:
        count+=1
        line = l.split('\t')
        for i in range(1,4):
            tmp = float(line[i])
            sum[i-1] += tmp

    print sum
    p.close()
    t = [0]*3
    for i in range(0,3):
        t[i] = sum[i]/count
        print t[i]

    print t
    p = open(path)
    for l in p:
        line = l.split('\t')
        for i in range(1,4):
            tmp = float(line[i])
            print tmp,
            print " --- ",
            print t[i-1],
            line[i] = "0" if tmp < t[i-1] else "1"
            print line[i]
        print line
        line.pop(0)
        f1.write("\t".join(line))

    p.close()
    f1.close()
    return

# separate file into train data and test data
def getTrainAndTest():
    p = open(wf)
    train = open(trainpath,'w')
    test = open(testpath,'w')
    count = 0
    for line in p:
        if count == 0:
            test.write(line)
        else:
            train.write(line)

        count+=1
        count = count%5

    p.close()
    train.close()
    test.close()

    return


if __name__ == "__main__":
    getBinary()
    getTrainAndTest()