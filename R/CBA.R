# Classification using Frequent Patterns
options(java.parameters = "-Xmx4g")

library(readxl)
library(arulesCBA)

df <- read_excel("D:/R Projects/fraud_set.xlsx")

# Change all features as Factors (String / categorial / discrete value)
df[] <- lapply(df, factor)

# change the dataset into transaction type (Market Basket)
trans <- as(discretizeDF.supervised(AUTHTXN_FRAUD_CHECK~., df), "transactions")
summary(trans)

# Create an item frequency plot for the top 20 items
if (!require("RColorBrewer")) {
  # install color package of R
  install.packages("RColorBrewer")
  #include library RColorBrewer
  library(RColorBrewer)
}
par(mar=c(5,7,1,1))
itemFrequencyPlot(trans,topN=20,type="absolute",col=brewer.pal(8,'Pastel2'), 
                  main="Absolute Item Frequency Plot")

# ========================================================

library(rCBA)
rules <- fpgrowth(trans, support=0.5, confidence=0.5, maxLength=5, 
                              consequent="AUTHTXN_FRAUD_CHECK", parallel=FALSE)
summary(rules)

# put in dataframe for easier to see
rulesFrame <- as(rules,"data.frame")

sink("CBA_Rules.txt")
inspect(rules)
sink()

# ========================================================

library(arc)
# Tune the init_support and init_conf in topRules()
# default is init_support = 0, init_conf = 0.5
# Run on Apriori
appearance <- list(rhs =  c("AUTHTXN_FRAUD_CHECK=D"), default="lhs")
system.time(rules2 <- topRules(trans, appearance, minlen = 1, init_maxlen = 5, 
                               iteration_timeout = 10, target_rule_count = 2000,
                               init_support = 0.5, init_conf = 0.5))

summary(rules2)

# # put in dataframe for easier to see
rulesFrame2 <- as(rules2,"data.frame")

sink("CBA_Rules.txt")
inspect(rules2)
sink()



