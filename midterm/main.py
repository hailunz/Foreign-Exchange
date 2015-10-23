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
from skimage import segmentation
from skimage.morphology import watershed
from skimage import measure
from skimage import morphology
import numpy as np

import mahotas as mh
import pandas as pd
from scipy import ndimage

import warnings
warnings.filterwarnings("ignore")
import time


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

# get the Feature Ratio
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
    filled_area = 0.0
    perimeter = 0.0
    solidity = 0.0
    if ((not maxregion is None) and  (maxregion.major_axis_length != 0.0)):
        ratio = 0.0 if maxregion is None else  maxregion.minor_axis_length*1.0 / maxregion.major_axis_length
    filled_area = 0.0 if maxregion is None else maxregion.filled_area
    perimeter = 0.0 if maxregion is None else maxregion.perimeter
    solidity = 0.0 if maxregion is None else maxregion.solidity

    return ratio, filled_area, perimeter, solidity

# get feature sift
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

# get feature sift count
def getSiftCount(nameFileImage):
    img = cv2.imread(nameFileImage)
    gray = cv2.cvtColor(img,cv2.COLOR_BGR2GRAY)
    sift = cv2.SIFT()
    k, des = sift.detectAndCompute(gray, None)
    if des is None:
        return 0
    else:
        return len(k)

def getMahotasFeature(nameFileImage):
    image2 = mh.imread(nameFileImage, as_grey=True)
    lbp = mh.features.lbp(image2, radius=20, points=7, ignore_zeros=False)
    zernike_moments = mh.features.zernike_moments(image2, radius=20, degree=8)
    return lbp, zernike_moments

# get the images count
def getRows(directory_names):
    numberofImages = 0
    for folder in directory_names:
        for fileNameDir in os.walk(folder):
            for fileName in fileNameDir[2]:
                 # Only read in the images
                if fileName[-4:] != ".jpg":
                  continue
                numberofImages += 1
    return numberofImages

# reading image, extract features and labels
def readingImage(train, namesClasses, directory_names,
                 X,y,files,labels_true=list()):
    i = 0
    label = 0
    print "Reading images"

    # Navigate through the list of directories
    for folder in directory_names:
        # Append the string class name for each class
        currentClass = folder.split(os.pathsep)[-1].split('/')[-1]
        if train:
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

                axisratio, filled_area, perimeter, solidity = getMinorMajorRatio(image)

                des = getSift(nameFileImage)

                image = resize(image, (maxPixel, maxPixel))

                # lbp, moments = getMahotasFeature(nameFileImage)

                # Store the rescaled image pixels and the axis ratio
                X[i, 0:imageSize] = np.reshape(image, (1, imageSize))
                X[i, imageSize] = axisratio
                X[i, imageSize+1] = filled_area
                X[i, imageSize+2] = perimeter
                X[i, imageSize+3] = solidity
                X[i, imageSize+4] = getSiftCount(nameFileImage)
                X[i, imageSize+5:imageSize+5+128] = des
                # X[i, imageSize+5+128:] = moments

                # Store the classlabel
                y[i] = label
                i += 1

                if not train:
                    labels_true.append(currentClass)

                # report progress for each 5% done
                report = [int((j+1)*num_rows/20.) for j in range(20)]
                if i in report: print np.ceil(i *100.0 / num_rows), "% done"
        label += 1


if __name__ == "__main__":
    start_time = time.time()
    # get the classnames from the directory structure
    directory_names = list(set(glob.glob(os.path.join("competition_data","train", "*"))\
     ).difference(set(glob.glob(os.path.join("competition_data","train","*.*")))))

    # Rescale the images and create the combined metrics and training labels
    #get the total training images
    numberofImages = getRows(directory_names)


    # We'll rescale the images to be 25x25
    maxPixel = 50
    imageSize = maxPixel * maxPixel
    num_rows = numberofImages # one row for each image in the training dataset
    num_features = imageSize + 4 + 128 + 1 + 25 # for our ratio

    print num_rows
    # X is the feature vector with one row of features per image
    # consisting of the pixel values and our metric
    X = np.zeros((num_rows, num_features), dtype=float)
    # y is the numeric class label
    y = np.zeros((num_rows))

    files = []
    # List of string of class names
    namesClasses = list()

    # get trainning data
    readingImage(train=True, namesClasses=namesClasses, directory_names= directory_names,
                 X=X, y=y, files=files)

    print "Training"
    # n_estimators is the number of decision trees
    # max_features also known as m_try is set to the default value of the square root of the number of features
    clf = RF(n_estimators=100, n_jobs=3);
    scores = cross_validation.cross_val_score(clf, X, y, cv=5, n_jobs=1);
    print "Accuracy of all classes"
    print np.mean(scores)


    kf = KFold(y, n_folds=5)
    y_pred = y * 0
    for train, test in kf:
        X_train, X_test, y_train, y_test = X[train,:], X[test,:], y[train], y[test]
        clf = RF(n_estimators=100, n_jobs=3)
        clf.fit(X_train, y_train)
        y_pred[test] = clf.predict(X_test)

    print y_pred
    print classification_report(y, y_pred, target_names=namesClasses)

    print "Testing"
    # get the classnames from the directory structure
    directory_names = list(set(glob.glob(os.path.join("competition_data","test", "*"))\
     ).difference(set(glob.glob(os.path.join("competition_data","test","*.*")))))

    num_rows = getRows(directory_names)
    print num_rows

    # X is the feature vector with one row of features per image
    # consisting of the pixel values and our metric
    X_test = np.zeros((num_rows, num_features), dtype=float)
    # y is the numeric class label
    y_test = np.zeros((num_rows))
    labels_true = list()

    files = []
    readingImage(train=False,namesClasses=list(), directory_names=directory_names,
                 X=X_test, y=y_test,files=files, labels_true=labels_true)
    y_pred = y * 0

    clf = RF(n_estimators=100, n_jobs=3)
    clf.fit(X,y)

    y_predict = clf.predict(X_test)

    print len(y_predict)
    size = len(labels_true)
    print size

    wf = open('classification_result','w')
    wf.write("predict\ttrue+\n");

    count = 0
    for i in range(size):
        label_true = labels_true[i]
        label_predict = namesClasses[int(y_predict[i])]

        wf.write(label_predict+'\t'+label_true+'\n')
        if label_true == label_predict:
            count += 1

    wf.close()
    print count
    print float(count)/float(size)
    print "running time:"
    print time.time() - start_time



















