
# Midterm Project Notebook

Hailun Zhu       ID: hailunz


## Problem Statement


This project is to predict ocean health, one plankton at a time. Details about the requirement can be seen at [Kaggle Link](https://www.kaggle.com/c/datasciencebowl).

There are total 121 classes of plankton of the training data. I only used the training data. I select approximately 80% of the training set as my training data, 20% as the test data. Training set: 24269, test set: 6067.


## Feature Extraction 

I select features step by step. All the new feature is tested based on the prevous ones. For this part I use Random Forest classifier provided by sklean, decision tree number 100.

#### Rescale image 
* 
    * Hypotheses  
    The information from the original image could provide inofrmation to help classification.
        
    * Feature  
    Because the image may have different size and the original image could be too large to use, I rescale the image to 25 x 25 size and transfer it to a 1d array of size 25 x 25.
    
    * Method   
    I first read in the image from the folder and then using resize to fix the image size of 25 x 25. Then I use np.reshape to make a 1d array.


```python
# Read in the images and create the features (in readingImage methods)
    nameFileImage = "{0}{1}{2}".format(fileNameDir[0], os.sep, fileName)
    image = imread(nameFileImage, as_grey=True)
    
    image = resize(image, (maxPixel, maxPixel))
    
    # Store the rescaled image pixels and the axis ratio
    X[i, 0:imageSize] = np.reshape(image, (1, imageSize)) 
```

  
  * Result:   
    Accuracy of all classes(cross validation): 0.451280580405
    
    Accuracy of the test: 0.460853799242
    
    Running time: 856.844261885s
    
    
   * Optimazation  
    
    I tried to increase the size of the image to see whether the size of the rescale image will affect the result. 
    I changed the size of the image to 50 X 50.
    
    The Optimazation results:
    
    Accuracy of all classes: 0.461760744425
    
    Accuracy of the test: 0.46958958299
    
    Running time: 1080.51703596
    
    
  * Conclusion  
    Adding the rescale image feature could help classification. Because the it provides the original information about the image. And increasing the size of the rescale image could improve the result because it provides more information about the original image.
    

#### Max Region Properties Feature
* 
    * Hypotheses  
    The largest segment is more likely to be the object of interest for classfication.
    The information of the largest segment could provide inofrmation to help classification.
        
    * Feature  
    Using measure region properties. I only tested some of the properties:
       * Ratio: float  
        maxregion.minor_axis_length*1.0 / maxregion.major_axis_length
       * filled_area : int  
        Number of pixels of filled region
       * major_axis_length : float  
        The length of the major axis of the ellipse that has the same normalized second central moments as the region.
       * minor_axis_length : float  
        The length of the minor axis of the ellipse that has the same normalized second central moments as the region.
       * perimeter : float  
        Perimeter of object which approximates the contour as a line through the centers of border pixels using a 4-connectivity.
       * solidity : float  
        Ratio of pixels in the region to pixels of the convex hull image.
    
    * Method   
    First I do some preprocess actions on the image, including: thresholding the images, segmenting the images, and extracting region properties. I choose the largest nonzero region.
    


```python
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
```

After finding the largest region, I extract the properties of this region.


```python
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
```

 * Result:   
    1. Only use Ratio property.
    
    Accuracy of all classes(cross validation): 0.469759054329
    
    Accuracy of the test: 0.467776495797
    
    Running time: 1097.83636689
    
  
  * Optimazation  
    
    I added the filled_area, perimeter and solidity. 
    
    The Optimazation results:
    
    Accuracy of all classes: 0.480298560882
    
    Accuracy of the test: 0.481457062799
    
    Running time: 1060.66653109
    
    
  * Conclusion  
    Finding the target interest object area and adding the properties of this area could help improve the feature vector. They provide only the information regarding to the target object. Different plankton may have different target size and  perimeter and solidity.
      

#### SIFT feature
* 
    * Hypotheses    
    Scale-invariant feature transform (or SIFT) is an algorithm in computer vision to detect and describe local features in images. SIFT is rotational invariance. Thus this may be helpful for the classification because the same plankton could be of different orientation. SIFT could help to decrease the effect of the orientation of the image.
        
    * Feature  
    SIFT feature originally is composed of a M*128 array. M is the number of the key points int the image. It varies from image to image.
    In order to make the feature to be a constant size 1d array, I use the sum of each column to form a 1*128 1d array.
    
    * Method  
    I use the opencv lib of python to extract the key point descriptor of the SIFT.
    


```python
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
```

 * Result:   
    Only use SIFT feature.
    
    Accuracy of all classes(cross validation): 0.497728131152
    
    Accuracy of the test: 0.501895500247
    
    Running time: 1118.5202539
    
  
  * Optimazation  
    
    I added the SIFT feature key point counts as an additional feature. The hypothese is that the number of the key points also provides information because the SIFT feature does not include this information. 
    
    The Optimazation results:
    
    Accuracy of all classes: 0.505686500742
    
    Accuracy of the test: 0.508653370694
    
    Running time: 1150.40613484
    
  * Conclusion  
  
    Adding SIFT and the key point could improve the result. The SIFT rotation-invariant feature helps to improve the result.
  


```python
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
```

#### Mahotas feature
* 
    * Hypotheses    
    There are also some other existing image feature extraction algorithms that could yield good results. I use mahotas library to test some of them.
        
    * Feature  
       * Local binary patterns (LBP) is a type of feature used for classification in computer vision.  
       The LBP feature vector, in its simplest form, is created in the following manner:

       Divide the examined window into cells (e.g. 16x16 pixels for each cell).
       For each pixel in a cell, compare the pixel to each of its 8 neighbors (on its left-top, left-middle, left-bottom, right-top, etc.). Follow the pixels along a circle, i.e. clockwise or counter-clockwise.
       Where the center pixel's value is greater than the neighbor's value, write "1". Otherwise, write "0". This gives an 8-digit binary number (which is usually converted to decimal for convenience).
       Compute the histogram, over the cell, of the frequency of each "number" occurring (i.e., each combination of which pixels are smaller and which are greater than the center).
       Optionally normalize the histogram.
       Concatenate (normalized) histograms of all cells. This gives the feature vector for the window.
       (From: https://en.wikipedia.org/wiki/Local_binary_patterns)
      
       * Zernike moments through degree. These are computed on a circle of radius radius centered around cm (or the center of mass of the image, if the cm argument is not used).
       Zernike moments are utilized as shape descriptors to classify benign and malignant breast masses.
       (From: https://en.wikipedia.org/wiki/Zernike_polynomials)
    
    * Method  
    I use the mahotas lib of python to extract the key point descriptor of the lbp/Zernike moments.


```python
def getMahotasFeature(nameFileImage):
    image2 = mh.imread(nameFileImage, as_grey=True)
    lbp = mh.features.lbp(image2, radius=20, points=7, ignore_zeros=False)
    zernike_moments = mh.features.zernike_moments(image2, radius=20, degree=8)
    return lbp, zernike_moments
```

* Result:   
    1. Only use LBP feature.
    
    Accuracy of all classes(cross validation): 0.497933914394
    
    Accuracy of the test: 0.503543761332
    
    Running time: 1279.74228287
   
    2. Only use Zernike moments  
    
    Accuracy of all classes: 0.500700147492
    
    Accuracy of the test: 0.501521674633
    
    Running time: 1350.40613484
    
    
  * Conclusion   
    Adding LBP/Zernike moments will not improve the results. That maybe because texture information are not important and the information provided by Zernike moments is redundant that is already provided by SIFT. 
    And as the running time increased by addthing these features, I decide not to include this in the final feature vectors.
      

## Summary of feature extraction

    feature        Accuracy of cross validation    Accuracy of the test       running time
    rescale_image(25)   0.451280580405                0.460853799242         856.844261885
    rescale_image(50)   0.461760744425                0.469589582999         1080.51703596
    ratio               0.469759054329                0.467776495797         1097.83636689
    ratio+prop          0.480298560882                0.481457062799         1060.66653109
    SIFT                0.497728131152                0.501895500247         1118.52025390
    SIFT+count          0.505686500742                0.508653370694         1150.40613484
    LBP                 0.497933914394                0.503543761332         1279.74228287         Zernike moments     0.500700147492                0.501521674633         1350.40613484

## Feature vector
rescale_image, ratio, filled_area, perimeter, solidity, sift_count, sift

## Difficulties in classification(error analysis)

1. The features do not include all the information.
    Most of the features only provide the shape information of the image and only consider the rotation of the image. For example, I do not use feature to compute holes and sharp curves in the image, which might be an important characteristic of the image. 
    
2. The image has noise.
    The noise in the image may result in error.
    
3. Some of the shapes are difficult to distinguish.
    1. Images in same class looks not the same   
    acantharia_protist 3721.jpg vs 3733.jpg
    
    2. Images in different class look similar
    Usually, there are some classes that have general similarities, such as acantharia_protist, and acantharia_protist_big_center etc. Only using the shape or outline of the images may misclassify one to another. 
    For example, the accuracy of the acantharia_protist is 41%, whereas the accuracy of the acantharia_protist_big_center is 0% in the first experiment.
    acantharia_protist 337.jpg, acantharia_protist_big_center  29759.jpg

## Classifier 

1. Random Forest  
    * Hypotheses  
    The parameter of the random forest may have influence on the classification result.
    
    * Method  
    Change the number of the decision tree used.
    
    1. N = 100 (baseline)  
    
    Accuracy of all classes: 0.505686500742
    
    Accuracy of the test: 0.508653370694
    
    Running time: 1150.40613484
    
    2. N = 200   
    
    Accuracy of all classes(cross validation): 0.506615930376
    
    Accuracy of the test: 0.512114718971
    
    Running time: 1750.24850917
    
    * Conclusion
    Increasing the number of the decision tree used could impove the classification result.
   

## Summary of classifier

    feature        Accuracy of cross validation    Accuracy of the test       running time
    N=100            0.505686500742                 0.508653370694            1150.40613484
    N=200            0.506615930376                 0.512114718971            1750.24850917

## Conclusion

The final feature vector is [rescale_image, ratio, filled_area, perimeter, solidity, sift_count, sift], rescle_image size is 50 X 50. Decision tree number is 200. The best accuracy that I got is 51.21%. 


## Future work

1. Adding features that could provide information that focus on edge cases like curves and holes, so that features could cover more cases.

2. Using a more state-of-art classifier, or test with other classifier like SVM. 


