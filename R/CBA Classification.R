options(java.parameters = "-Xmx8g")
library("rCBA")
library(arulesCBA)

df = read.csv("D:/R Projects/preprocessed_dataset.csv")

# Change AUTHTXN_FRAUD_CHECK as Factor (String / categorial / discrete value)
df[] <- lapply(df, factor)

set.seed(42)

# Train Test Split
require(caret)
train_test_split <- createDataPartition(df$AUTHTXN_FRAUD_CHECK,
                                        p = .7, list = FALSE)
# temp_df <- df[-train_test_split,]
# train_test_split <- createDataPartition(temp_df$AUTHTXN_FRAUD_CHECK,
#                                         p = .4, list = FALSE)

train_set <- df[ train_test_split,]
test_set  <- df[-train_test_split,]

# Print train and test set numbers of non-fraud and fraud 
sprintf("Splitted number of training set %d", nrow(train_set))
table(train_set$AUTHTXN_FRAUD_CHECK)

sprintf("Splitted number of testing set %d", nrow(test_set))
table(test_set$AUTHTXN_FRAUD_CHECK)

# change to market basket transaction
transactions <- as(discretizeDF.supervised(AUTHTXN_FRAUD_CHECK~., train_set), "transactions")

library(arc)
appearance <- list(rhs =  c("AUTHTXN_FRAUD_CHECK=1", "AUTHTXN_FRAUD_CHECK=0"), default="lhs")

rules <- apriori(transactions, parameter = list(supp = 0.02, conf = 0.5, target = "rules", minlen=2, maxlen=5), 
                 appearance=appearance)

# rules <- topRules(transactions, appearance, minlen = 2, init_maxlen = 5,
#                                iteration_timeout = 10, target_rule_count = 1000,
#                                init_support = 0.02, init_conf = 0.5)

summary(rules)
rulesFrame <- as(rules, "data.frame")

table(test_set$predictions)
test_set$predictions <- classification(test_set, rulesFrame)
sum(test_set$AUTHTXN_FRAUD_CHECK==test_set$predictions, na.rm=TRUE) / length(test_set$predictions)

sink("CBA_Rules.txt")
inspect(rules)
sink()

