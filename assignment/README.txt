README

data prep:
 	-parse.py
		- use this file to first parse the data and get the original features, which 
	-merge.py
		- use this file to merge the information got from those three types. Add EURUSD, GBPUSD direction to the AUDUSD’s feature, to obtain the final feature, which is :
	-getBinary.py
		- use this file to reprocess the data.

decisionTree:
	- src
	    - decisionTree:
		- Main.java : 
			include the function to generate the tree(getNode) and testTree
			include the function to generate the random forest and testRandomForest.
		- TreeNode.java:
			TreeNode class
	- out