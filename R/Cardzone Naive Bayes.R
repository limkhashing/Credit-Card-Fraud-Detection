df = read.csv("D:/R Projects/preprocessed_dataset.csv")
sprintf("Rows: %d Columns: %d",nrow(df), length(names(df)))
count <- table(df$AUTHTXN_FRAUD_CHECK)

# Plot pie chart  
pie_percent<- round(100*table(df$AUTHTXN_FRAUD_CHECK)/sum(table(df$AUTHTXN_FRAUD_CHECK)), 1)
pie(count, labels = pie_percent, main = "Fraud Percentage", col = table(df$AUTHTXN_FRAUD_CHECK))
legend("topright", c("0","1"), cex = 0.8, fill = table(df$AUTHTXN_FRAUD_CHECK))

# list types for each attribute
sapply(df, class)

# Change some features as Factors (String / categorial / discrete value)
cols <- c("AUTHTXN_FRAUD_CHECK", "AUTHTXN_MERCHANT_ID", "AUTHTXN_POS_ENTRY_MODE",
          "AUTHTXN_CURRENCY_CODE", "AUTHTXN_COUNTRY_CODE", "AUTHTXN_MCC_ID",
          "AUTHTXN_CUST_ID", "AUTHTXN_CRDACCT_NO")
df[cols] <- lapply(df[cols], factor)

# summarize each columns information
summary(df)

# set seed is for reproduceable results
# without this, it will randomly assigns rows to train and test set
# each time it generated
set.seed(42)

# Train Test Split
require(caret)
train_test_split <- createDataPartition(df$AUTHTXN_FRAUD_CHECK, 
                                        p = .7, 
                                        list = FALSE)
train_set <- df[ train_test_split,]
test_set  <- df[-train_test_split,]

# Print train and test set numbers of non-fraud and fraud 
sprintf("Splitted number of training set %d", nrow(train_set))
table(train_set$AUTHTXN_FRAUD_CHECK)

sprintf("Splitted number of testing set %d", nrow(test_set))
table(test_set$AUTHTXN_FRAUD_CHECK)

print("====================================================")

# Train the Naive Bayes with proprocessed data from python
require(naivebayes) # naive_bayes (Multinomial)
require(e1071) # naiveBayes (Gaussian) 

system.time(model <- naiveBayes(AUTHTXN_FRAUD_CHECK~., data=train_set))

# Test the Naive Bayes
system.time(test_set$predictions <- predict(model, test_set))

# print confusion matrix
confusion_matrix <- confusionMatrix(test_set$predictions, test_set$AUTHTXN_FRAUD_CHECK, 
                                    dnn = c("Predictions", "Actuals"), positive = '1' , mode = 'prec_recall')

confusion_matrix$table

# Accuracy
sprintf("Accuracy: %f %%", confusion_matrix$overall['Accuracy']*100)

# Precision
sprintf("Precision: %f %%", confusion_matrix$byClass['Precision']*100)

# Recall
sprintf("Recall: %f %%", confusion_matrix$byClass['Recall']*100)

# F1
sprintf("F1: %f %%", confusion_matrix$byClass['F1']*100)

# plot precision-recall curve
library(precrec)
autoplot(evalmod(scores = as.numeric(test_set$predictions), 
                 labels = test_set$AUTHTXN_FRAUD_CHECK), 'PRC')

# Cross-validation to check over-fitting
k <- 10
folds <- createFolds(train_set$AUTHTXN_FRAUD_CHECK, k = k, list=FALSE)
cross_accuracy <- list()
cross_precision <- list()
cross_recall <- list()
cross_f1 <- list()

# Start of k-fold cross valiation
for(i in 1:k) {
  # extract out the train and test set of fold
  testIndexes <- which(folds==i,arr.ind=TRUE)
  trainData   <- train_set[-testIndexes, ] 
  testData    <- train_set[testIndexes, ] 
  
  # perform cross validation of training and testing
  model <- naiveBayes(AUTHTXN_FRAUD_CHECK~., data=trainData)
  cross_validation_result <- predict(model, testData, type='class') 
  confusion_matrix <- confusionMatrix(cross_validation_result, testData$AUTHTXN_FRAUD_CHECK, 
                                      dnn = c("Predictions", "Actuals"), positive = '1' , mode = 'prec_recall')
  
  # store the fold's metrics score in list
  cross_accuracy[[i]]   <- confusion_matrix$overall['Accuracy']
  cross_precision[[i]]  <- confusion_matrix$byClass['Precision']
  cross_recall[[i]]     <- confusion_matrix$byClass['Recall']
  cross_f1[[i]]         <- confusion_matrix$byClass['F1']
}

# Print the confusion matrix for cross-validated model
confusion_matrix$table

sprintf("Accuracy of cross-validation is: %f" , Reduce("+",cross_accuracy)  / length(cross_accuracy))
sprintf("Precision of cross-validation is: %f", Reduce("+",cross_precision) / length(cross_precision))
sprintf("Recall of cross-validation is: %f"   , Reduce("+",cross_recall)    / length(cross_recall))
sprintf("F1 of cross-validation is: %f"       , Reduce("+",cross_f1)        / length(cross_f1))

# test prediction
# reset index of dataframe
rownames(test_set) <- 1:nrow(test_set) 
start_time <- Sys.time()
predict(model, newdata=test_set[12, ], type = 'class', na.action = na.omit)
end_time <- Sys.time()
time_taken <- end_time - start_time
time_taken

# save the Naive Bayes model to disk and load it back
save(model, file="NaiveBayesGaussian.RData")
load("NaiveBayesGaussian.RData")

