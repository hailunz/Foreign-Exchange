
/**
 * Hailun Zhu
 * ID: hailunz
 * Date: 11/14/15
 */

import java.util.Date

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

object TestRF {
  def main (args: Array[String]) {
    val conf = new SparkConf().set("spark.cassandra.connection.host", "localhost")
      .setAppName("Random Forest Application")

    /** Connect to the Spark cluster: */
    val sc = new SparkContext(conf)
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

    // Evaluate model on test instances and compute test error
    val labelAndPreds = testData.map { point =>
      val prediction = model.predict(point.features)
      (point.label, prediction)
    }

    val testPredictPositive = labelAndPreds.filter(r => r._1==1 && r._2==1).count.toDouble
    val testLabelPositive = labelAndPreds.filter(r => r._1==1).count.toDouble
    val testRecall = testPredictPositive / testLabelPositive
    val testPrecision = labelAndPreds.filter(r => r._1 == r._2).count.toDouble / testData.count()
    println("Test Precision = " + testPrecision)
    println("Test Recall = " + testRecall)
    println("Learned classification forest model:\n" + model.toDebugString)

    val timestamp = new Date
    val collection = sc.parallelize(Seq((timestamp, testPrecision, testRecall)))

    // save performance metrics to database
    collection.saveToCassandra("bigdata","performance",SomeColumns("timestamp","precision", "recall"))

    // Save and load model
    model.save(sc, "forestModel")
    val sameModel = RandomForestModel.load(sc, "forestModel")
  }
}
