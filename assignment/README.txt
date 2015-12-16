README

table.txt: table schema, create table command
forest: Json file for example random forest.


data prep:
 	-parse.py
		- use this file to first parse the data and get the original features 
	-merge.py
		- use this file to merge the information got from those three types. Add EURUSD, GBPUSD direction to the AUDUSD’s feature, to obtain the final feature, which is :
	-getBinary.py
		- use this file to reprocess the data.

decisionTree:
	- src
	    - decisionTree:
		- driver (package):
			- Main.java : 
			include the function to generate the tree(getNode) and testTree
			include the function to generate the random forest and testRandomForest.
		- decisionTree(package):
			- TreeNode.java:
				TreeNode class
			- RandomForest.java:
				generate random forest
			- DecisionTree.java:
				generate a single tree
		
		- database (package):
			- Database.java
				for connection and operations with database
			- Util.java
				load data in/out to/from database

		- testClient (package)
			- Test 
			- TestFromFile : test client 

		- MapReduce (package):
			- MapReduce.java
			- NLineInputFormat.java
			- BlockRecordReader.java

		- log4j.properties:
			configuration file

	- db.properties:
		store information about db ip and keyspace


packages:

cassandra-all-1.2.19.jarcassandra-all-2.0.17.jarcassandra-all-3.0.0-rc1.jarcassandra-thrift-1.1.5.jargson-2.3.1.jarhadoop-common-2.7.1.jarhadoop-core-1.2.1.jarhadoop-mapreduce-client-core-2.7.1.jar
slf4j-1.7.12.zipthrift-0.9.3.tar.gz
cassandra-java-driver-2.1.0

		