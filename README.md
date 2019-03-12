# Research in Machine Learning algorithms in Credit Card Fraud Detection

This is a research project collaborated with Cardzone Sdn Bhd (Comapny at KL), which aims to investigate Data Mining and Machine Learning techniques and identifies potential solutions in detecting fraudulent transactions

As a result of the literature study and discussion, Naïve Bayesian, Decision Tree, Frequent Pattern-growth (FP-growth) and Classification using Frequent Patterns have been selected as Data Mining and Machine Learning algorithms for fraud detection. 

The justification for this is:
* Decision Tree -- Able to provide fraud rules, which can be embedded into the rule engine
* Naïve Bayesian -- Able to provide probability, such as how probable is the transaction is a fraud
* FP-growth -- To mine frequent patterns out from the fraud dataset. 
* Classification using Frequent Patterns -- From all of the fraud and non-fraud frequent patterns, generate Association Rules, which then use it as a rule classifier model to classify fraudulent transaction. 

:warning: Due to the Privacy and Confidentiality of customers data, the original dataset that primarily used for train and test Machine Learning models will not be share out. 

### Outlines
* Java  -- Contains training and testing for all of the algorithms mentioned. Deployment of models also in this directory
* Python -- Contains Data Visualization for both benchmark and original dataset. A preprocessing script also for the original dataset
* R --  Contains training and testing for all of the algorithms mentioned. R code is the primary code base for this research project

### Dataset used
* [creditcard.csv](https://www.kaggle.com/mlg-ulb/creditcardfraud) -- Taken from Kaggle as a benchmark dataset
* cz_authtxn.csv -- Original dataset that gotten from Cardzone Sdn Bhd
* preprocessed_dataset.csv -- Preprocessed dataset from the original dataset

## Built With
* [PyCharm](https://www.jetbrains.com/pycharm/) - The IDE used
* [Jupyter Notebook](https://jupyter.org/) - For Data Visualization
* Python 3.6 programming language
* R programming language
* Java with [Apache Spark](https://spark.apache.org/) framework and [JRI (Java/R Interface)](http://www.rforge.net/JRI/) 

## Deployment
[IBM Watson](https://www.ibm.com/watson) was chosen as it allows building custom Machine Learning models from scratch and also provide APIs for calling the model to make on-demand online predictions.

## Authors
* **Lim Kha Shing** - [kslim888](https://github.com/kslim888)
