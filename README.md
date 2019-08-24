# Research in Machine Learning algorithms in Credit Card Fraud Detection
![FYP](https://img.shields.io/badge/Machine%20Learning-Final%20Year%20Project-brightgreen.svg)
> This is a research project collaborated with Cardzone Sdn Bhd (Comapny at KL), which aims to investigate Data Mining and Machine Learning techniques and identifies potential solutions in detecting fraudulent transactions

## Outcomes
As a result of the literature study and discussion, Naïve Bayesian, Decision Tree, Frequent Pattern-growth (FP-growth) and Classification using Frequent Patterns have been selected as Data Mining and Machine Learning algorithms for fraud detection. 

The justification for selected algorithm also take into the account of the requirements by the company, such as:
* **Decision Tree** -- Able to provide fraud rules, which can be embedded into the rule engine
* **Naïve Bayesian** -- Able to provide probability, such as how probable is the transaction is a fraud
* **FP-growth** -- To mine frequent patterns out from the fraud dataset. 
* **Classification using Frequent Patterns** -- From all of the fraud and non-fraud frequent patterns, generate Association Rules, which then use it as a rule classifier model to classify fraudulent transaction. 

:warning: Due to the Privacy and Confidentiality of customers data, the original dataset that primarily used for train and test Machine Learning models will not be share out. 

### Outlines
* **Java**  -- Contains training and testing for all of the algorithms mentioned. Deployment of models also in this directory
* **Python** -- Contains Exploratory Data Analysis and Visualization for both benchmark and original dataset. A preprocessing script also available for the original dataset
* **R** --  Contains training and testing for all of the algorithms mentioned. R code is the primary code base for this research project

### Dataset used
* **[creditcard.csv](https://www.kaggle.com/mlg-ulb/creditcardfraud)** -- Taken from Kaggle as a benchmark dataset
* **cz_authtxn.csv** -- Original dataset that gotten from Cardzone Sdn Bhd
* **preprocessed_dataset.csv** -- Preprocessed dataset from the original dataset

## Built With
* [PyCharm](https://www.jetbrains.com/pycharm/) - The IDE used
* [Jupyter Notebook](https://jupyter.org/) - For Data Visualization
* Python 3.6 with [Sci-kit learn](https://scikit-learn.org/) framework
* R programming language. Packages that used please see [install_packages.R](https://github.com/kslim888/Credit-Card-Fraud-Detection/blob/master/R/install_packages.R) 
* Java with [Apache Spark](https://spark.apache.org/) framework and [JRI (Java/R Interface)](http://www.rforge.net/JRI/) 

### Sample Machine Learning model test performance
![Sample Machine Learning model test performance](https://user-images.githubusercontent.com/30791939/54373092-9daf9480-46b7-11e9-9fe0-e22d94574749.png)

### Inspect test set with truth value and predicted value
![Inspect test set](https://user-images.githubusercontent.com/30791939/54373208-d3547d80-46b7-11e9-8e51-648df985c188.png)

## Deployment
[IBM Watson](https://www.ibm.com/watson) was chosen as it allows building custom Machine Learning models from scratch and also provide APIs for calling the model to make on-demand online predictions.
Once the model deployed as PMML on IBM watson, it will return a JSON result states its probability fraud value

### Live Online Prediction
![Live Online Prediction](https://user-images.githubusercontent.com/30791939/54372585-8de38080-46b6-11e9-87c9-0fe18ad0452c.png)

## Authors
* [**Lim Kha Shing**](https://www.linkedin.com/in/lim-kha-shing-836a24120/)
