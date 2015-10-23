__author__ = 'hailunzhu'

import os
import shutil
path = '/Users/hailunzhu/cmu/course/11676/midterm/'
r1 = './competition_data/train'
r2 = './competition_data/test'
rootdir = './train'
pattern = '*.jpg'
k = 0
for dir, dirname, filenames in os.walk(rootdir):
    prefix = dir[8:]
    for file in filenames:
        k += 1
        dest1 = os.path.join(r1, prefix)
        dest2 = os.path.join(r2, prefix)

        if k % 5 == 0:
            shutil.copy2(os.path.join(dir, file), dest2)
        else:
            shutil.copy2(os.path.join(dir, file), dest1)