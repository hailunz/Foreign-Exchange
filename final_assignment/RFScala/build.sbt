name := "Simple Project"
version := "1.0"
scalaVersion := "2.10.4"

libraryDependencies += "org.apache.spark" %% "spark-core" % "1.5.2"
libraryDependencies += "org.apache.spark" %% "spark-mllib" % "1.5.2"
libraryDependencies += "com.datastax.spark" %% "spark-cassandra-connector" % "1.4.0-M3"
libraryDependencies += "org.apache.spark" %% "spark-sql" % "1.5.2"