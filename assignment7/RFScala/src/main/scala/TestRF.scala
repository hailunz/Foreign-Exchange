
/**
 * Hailun Zhu
 * ID: hailunz
 * Date: 11/20/15
 */

import java.util.Date

import _root_.org.apache.spark.mllib.util.MLUtils
import org.apache.spark.SparkConf
import org.apache.spark.SparkContext
import org.apache.spark.mllib.linalg.Vectors
import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.mllib.tree.RandomForest
import org.apache.spark.mllib.tree.RandomForest
import org.apache.spark.mllib.tree.model.RandomForestModel
import org.apache.spark.mllib.util.MLUtils
import org.apache.spark.SparkContext
import org.apache.spark.SparkContext._
import org.apache.spark.SparkConf

import com.datastax.spark.connector._
import com.datastax.spark.connector.rdd._
import org.apache.spark.rdd.RDD


import org.apache.spark.ml.evaluation.RegressionEvaluator
import org.apache.spark.ml.regression.LinearRegression
import org.apache.spark.ml.tuning.{ParamGridBuilder, TrainValidationSplit}
import org.apache.spark.mllib.util.MLUtils

import org.apache.spark.sql.cassandra.CassandraSQLContext
import org.apache.spark.sql.DataFrame
import org.apache.spark.sql._
import org.apache.spark.sql.execution.datasources

//gbt
import org.apache.spark.mllib.tree.GradientBoostedTrees
import org.apache.spark.mllib.tree.configuration.BoostingStrategy
import org.apache.spark.mllib.tree.model.GradientBoostedTreesModel


import org.apache.spark.ml.classification.LogisticRegression
import org.apache.spark.ml.param.ParamMap
import org.apache.spark.ml.evaluation.BinaryClassificationEvaluator
import org.apache.spark.mllib.linalg.{Vector, Vectors}

import org.apache.spark.ml.feature.DCT
import org.apache.spark.ml.feature.PCA

object TestRF {
  def main (args: Array[String]) {
    val conf = new SparkConf().set("spark.cassandra.connection.host", "localhost")
      .setAppName("Random Forest Application")

    /** Connect to the Spark cluster: */
    val sc = new SparkContext(conf)
    val sqlContext = new org.apache.spark.sql.SQLContext(sc)

    import sqlContext.implicits._

    // Transformers
    // Load training data

    val trainRdd = sc.cassandraTable("bigdata", "train")

    // transform trainningData
    val trainData: RDD[LabeledPoint]  = trainRdd
      .map{row => {
        // Convert nested map to Seq so it can be passed to Vector
        val features = Vectors.sparse(6, Array(0,1,2,3,4,5), Array(row.getInt("f0"),
          row.getInt("f1"),
          row.getInt("f2"),
          row.getInt("f3"),
          row.getInt("f4"),
          row.getInt("f5")))
        // Convert label to Double so it can be used for LabeledPoint
        val label = row.getInt("label").toDouble
        ( LabeledPoint(label, features))
      }}

    // get data frame
    val trainDF = trainData.toDF()

    // read and transform test Data
    val testRdd = sc.cassandraTable("bigdata", "test")
    val testData: RDD[LabeledPoint]  = testRdd
      .map{row => {
        // Convert nested map to Seq so it can be passed to Vector
        val features = Vectors.sparse(6, Array(0,1,2,3,4,5), Array(row.getInt("f0"),
          row.getInt("f1"),
          row.getInt("f2"),
          row.getInt("f3"),
          row.getInt("f4"),
          row.getInt("f5")))
        // Convert label to Double so it can be used for LabeledPoint
        val label = row.getInt("label").toDouble
        ( LabeledPoint(label, features))
      }}

    val testDF = testData.toDF()


    // Estimators : training models
    // Create a LogisticRegression instance.  This instance is an Estimator.
    val lr = new LogisticRegression()

    // We may set parameters using setter methods.
    lr.setMaxIter(10)
      .setRegParam(0.01)

    // Learn a LogisticRegression model.  This uses the parameters stored in lr.
    val model1 = lr.fit(trainDF)

    // We may alternatively specify parameters using a ParamMap,
    // which supports several methods for specifying parameters.
    val paramMap = ParamMap(lr.maxIter -> 20)
  .put(lr.maxIter, 30) // Specify 1 Param.  This overwrites the original maxIter.
  .put(lr.regParam -> 0.1, lr.threshold -> 0.55) // Specify multiple Params.

    // One can also combine ParamMaps.
    val paramMap2 = ParamMap(lr.probabilityCol -> "myProbability") // Change output column name
    val paramMapCombined = paramMap ++ paramMap2

    val trainPca = new PCA()
      .setInputCol("features")
      .setOutputCol("pcaFeatures")
      .setK(3)
      .fit(trainDF)
    val trainPcaDF = trainPca.transform(trainDF)

    val testPca = new PCA()
      .setInputCol("features")
      .setOutputCol("pcaFeatures")
      .setK(3)
      .fit(testDF)
    val testPcaDF = testPca.transform(testDF)

    // Now learn a new model using the paramMapCombined parameters.
    // paramMapCombined overrides all parameters set earlier via lr.set* methods.
    val model2 = lr.fit(trainPcaDF, paramMapCombined)


    // Make predictions on test data using the Transformer.transform() method.
    // LogisticRegression.transform will only use the 'features' column.
    var predictPositive = 0
    var labelPositive = 0
    var predictRight = 0
    var count = 0;

    val res =  model2.transform(testPcaDF)
      .select("pcaFeatures", "label", "myProbability", "prediction")
      .collect()
      .foreach { case Row(features: Vector, label: Double, prob: Vector, prediction: Double) =>
          println(s"($features, $label) -> prob=$prob, prediction=$prediction")
          count+=1;
          if (label == prediction){
            predictRight+=1;
          }
          if (label==1 && prediction==1.0){
            predictPositive+=1;
          }
          if (label==1){
            labelPositive+=1;
          }
      }

    println("pca Test Precision = " + predictRight/count.toDouble)
    println("Test Recall = " + predictPositive/labelPositive.toDouble)

    // Train a RandomForest model.
    //  Empty categoricalFeaturesInfo indicates all features are continuous.
    val numClasses = 2
    val categoricalFeaturesInfo = Map[Int, Int]()
    val numTrees = 5 // Use more in practice.
    val featureSubsetStrategy = "auto" // Let the algorithm choose.
    val impurity = "gini"
    val maxDepth = 4
    val maxBins = 32

    val model = RandomForest.trainClassifier(trainData, numClasses, categoricalFeaturesInfo,
      numTrees, featureSubsetStrategy, impurity, maxDepth, maxBins)


    // Train a GradientBoostedTrees model.
    //  The defaultParams for Classification use LogLoss by default.
    val boostingStrategy = BoostingStrategy.defaultParams("Classification")
    boostingStrategy.numIterations = 3 // Note: Use more iterations in practice.
    boostingStrategy.treeStrategy.numClasses = 2
    boostingStrategy.treeStrategy.maxDepth = 5
    //  Empty categoricalFeaturesInfo indicates all features are continuous.
    boostingStrategy.treeStrategy.categoricalFeaturesInfo = Map[Int, Int]()

    val modelGBT = GradientBoostedTrees.train(trainData, boostingStrategy)

    // Evaluate model on test instances and compute test error
    val labelAndPreds = testData.map { point =>
      val prediction = modelGBT.predict(point.features)
      (point.label, prediction)
    }

    val testPredictPositive = labelAndPreds.filter(r => r._1==1 && r._2==1).count.toDouble
    val testLabelPositive = labelAndPreds.filter(r => r._1==1).count.toDouble
    val testRecall = testPredictPositive / testLabelPositive
    val testPrecision = labelAndPreds.filter(r => r._1 == r._2).count.toDouble / testData.count()
    println("RF Test Precision = " + testPrecision)
    println("RF Test Recall = " + testRecall)


    val timestamp = new Date
    val collection = sc.parallelize(Seq((timestamp, testPrecision, testRecall)))

    // save performance metrics to database
    collection.saveToCassandra("bigdata","performance",SomeColumns("timestamp","precision", "recall"))

    // Save and load model
    model.save(sc, "forestModel")
    val sameModel = RandomForestModel.load(sc, "forestModel")
  }
}
