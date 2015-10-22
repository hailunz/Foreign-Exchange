__author__ = 'hailunzhu'

import cv2
import sys
from os import walk
#Import libraries for doing image analysis
from skimage.io import imread
from skimage.transform import resize
from sklearn.ensemble import RandomForestClassifier as RF
import glob
import os
from sklearn import cross_validation
from sklearn.cross_validation import StratifiedKFold as KFold
from sklearn.metrics import classification_report
from pylab import cm
from skimage import segmentation
from skimage.morphology import watershed
from skimage import measure
from skimage import morphology
import numpy as np
import pandas as pd
from scipy import ndimage
from skimage.feature import peak_local_max

import warnings
warnings.filterwarnings("ignore")
# get the classnames from the directory structure
directory_names = list(set(glob.glob(os.path.join("competition_data","train", "*"))\
 ).difference(set(glob.glob(os.path.join("competition_data","train","*.*")))))

# find the largest nonzero region
def getLargestRegion(props, labelmap, imagethres):
    regionmaxprop = None
    for regionprop in props:
        # check to see if the region is at least 50% nonzero
        if sum(imagethres[labelmap == regionprop.label])*1.0/regionprop.area < 0.50:
            continue
        if regionmaxprop is None:
            regionmaxprop = regionprop
        if regionmaxprop.filled_area < regionprop.filled_area:
            regionmaxprop = regionprop
    return regionmaxprop

def getMinorMajorRatio(image):
    image = image.copy()
    # Create the thresholded image to eliminate some of the background
    imagethr = np.where(image > np.mean(image),0.,1.0)

    #Dilate the image
    imdilated = morphology.dilation(imagethr, np.ones((4,4)))

    # Create the label list
    label_list = measure.label(imdilated)
    label_list = imagethr*label_list
    label_list = label_list.astype(int)

    region_list = measure.regionprops(label_list)
    maxregion = getLargestRegion(region_list, label_list, imagethr)

    # guard against cases where the segmentation fails by providing zeros
    ratio = 0.0
    if ((not maxregion is None) and  (maxregion.major_axis_length != 0.0)):
        ratio = 0.0 if maxregion is None else  maxregion.minor_axis_length*1.0 / maxregion.major_axis_length
    return ratio

def getSift(nameFileImage):
    img = cv2.imread(nameFileImage)
    gray = cv2.cvtColor(img,cv2.COLOR_BGR2GRAY)
    sift = cv2.SIFT()
    k, des = sift.detectAndCompute(gray, None)
    if des is None:
        return [0]*128
    else:
        res = [0]*128
        for d in des:
            for i in range(len(d)):
                res[i] += d[i]
        return res

def getSiftCount(nameFileImage):
    img = cv2.imread(nameFileImage)
    gray = cv2.cvtColor(img,cv2.COLOR_BGR2GRAY)
    sift = cv2.SIFT()
    k, des = sift.detectAndCompute(gray, None)
    if des is None:
        return 0
    else:
        return len(k)

# Rescale the images and create the combined metrics and training labels
#get the total training images
numberofImages = 0
for folder in directory_names:
    for fileNameDir in os.walk(folder):
        for fileName in fileNameDir[2]:
             # Only read in the images
            if fileName[-4:] != ".jpg":
              continue
            numberofImages += 1

# We'll rescale the images to be 25x25
maxPixel = 50
imageSize = maxPixel * maxPixel
num_rows = numberofImages # one row for each image in the training dataset
num_features = imageSize + 1 + 128 + 1 # for our ratio

# X is the feature vector with one row of features per image
# consisting of the pixel values and our metric
X = np.zeros((num_rows, num_features), dtype=float)
# y is the numeric class label
y = np.zeros((num_rows))

files = []
# Generate training data
i = 0
label = 0
# List of string of class names
namesClasses = list()

print "Reading images"
# Navigate through the list of directories
for folder in directory_names:
    # Append the string class name for each class
    currentClass = folder.split(os.pathsep)[-1].split('/')[-1]
    namesClasses.append(currentClass)
    for fileNameDir in os.walk(folder):
        for fileName in fileNameDir[2]:
            # Only read in the images
            if fileName[-4:] != ".jpg":
               continue

            # Read in the images and create the features
            nameFileImage = "{0}{1}{2}".format(fileNameDir[0], os.sep, fileName)
            image = imread(nameFileImage, as_grey=True)

            files.append(nameFileImage)

            axisratio = getMinorMajorRatio(image)

            des = getSift(nameFileImage)

            image = resize(image, (maxPixel, maxPixel))

            # Store the rescaled image pixels and the axis ratio
            X[i, 0:imageSize] = np.reshape(image, (1, imageSize))
            X[i, imageSize] = axisratio
            X[i, imageSize+1] = getSiftCount(nameFileImage)
            X[i, imageSize+2:] = des

            # Store the classlabel
            y[i] = label
            i += 1
            # report progress for each 5% done
            report = [int((j+1)*num_rows/20.) for j in range(20)]
            if i in report: print np.ceil(i *100.0 / num_rows), "% done"
    label += 1

print "Training"
# n_estimators is the number of decision trees
# max_features also known as m_try is set to the default value of the square root of the number of features
# clf = RF(n_estimators=100, n_jobs=3);
# scores = cross_validation.cross_val_score(clf, X, y, cv=5, n_jobs=1);
# print "Accuracy of all classes"
# print np.mean(scores)

#
# kf = KFold(y, n_folds=5)
# y_pred = y * 0
# for train, test in kf:
#     X_train, X_test, y_train, y_test = X[train,:], X[test,:], y[train], y[test]
#     clf = RF(n_estimators=100, n_jobs=3)
#     clf.fit(X_train, y_train)
#     y_pred[test] = clf.predict(X_test)
#
# print y_pred
# print classification_report(y, y_pred, target_names=namesClasses)

print "Testing"
y_pred = y * 0

clf = RF(n_estimators=100, n_jobs=3)
clf.fit(X,y)

featureFileName = "testFeature"
labelFileName = "testLabel"

featureFile = open(featureFileName)
labelFile = open(labelFileName)

X_test = list()
for line in featureFile:
    tmp = line.split('\n')[0].split('\t')
    feature = list()
    for f in tmp:
        feature.append(float(f))
    X_test.append(feature)

featureFile.close()
X_TEST = np.zeros((num_rows, num_features), dtype=float)

true_labels = list()
for line in labelFile:
    true_labels.append(line.split('\n')[0])

labelFile.close()

y_predict = clf.predict(X_test)

print len(y_predict)

size = len(true_labels)
print size

count = 0
for i in range(size):
    label_true = true_labels[i].split('/')[-1]
    print label_true

    label_predict = namesClasses[int(y_predict[i])].split('/')[-1]
    print label_predict

    if label_true == label_predict:
        count += 1

print count
print float(count)/float(size)



















