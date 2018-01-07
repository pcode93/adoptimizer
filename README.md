#Adoptomizer
A distributed system built on Akka which inserts ads on websites in places with content that best fits the ads category

##How to train classifiers
`curl -X POST -d '<PATH TO CSV FILE>' http://localhost:8090/system/train`  
An example csv is located in resources/data.csv
