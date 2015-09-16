README

1. parse.py
	- use this file to first parse the data and get the original features, which include:
 feature[timestamp, type, maxBid, minBid, avgBid, avgSpread, direction compare to previous time], label[predicted direction]

I first use this program to get original dataset for AUDUSD, EURUSD and GBPUSD individually. 
2. merge.py
	- use this file to merge the information got from those three types. Add EURUSD, GBPUSD direction to the AUDUSD’s feature, to obtain the final feature, which is :

feature[timestamp, type, maxBid, minBid, avgBid, avgSpread, direction compare to previous time,  EURUSD direction, GBPUSD direction], label[predicted direction] 