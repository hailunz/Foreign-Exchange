__author__ = 'hailunzhu'


import sys
import fnmatch
from os import walk

root = "data/sessions/"
foldernames = []

fn = "data/sessions.csv"

wf = open(fn,"w")
count = 0


for (dirpath, dirnames, filenames) in walk(root):
    for items in fnmatch.filter(filenames, "*.nxml"):
        filename = dirpath + "/"+ items
        line = str(count) + "," + filename
        wf.writelines(line+'\n')
        count+=1


wf.close()





