print("Installing Necessary Packges...")

list_of_packages <- c("caret", "rpart", "precrec", "rattle", "rpart.plot", "naivebayes", "e1071", "rJava")
for (i in 1:length(list_of_packages)) {
  print(list_of_packages[i])
}

new_packages <- list_of_packages[!(list_of_packages %in% installed.packages()[,"Package"])]
if(length(new_packages)) {
  install.packages(new_packages, repos='https://cran.rstudio.com/ 9', dependencies = TRUE)
}

for (i.pack in 1:length(list_of_packages)) {
  suppressMessages(require(list_of_packages[i.pack], character.only = TRUE, quietly = TRUE, warn.conflicts = FALSE))
}

print("Finish Installation. Please check whether the library do existe in R_HOME directory (C:\\R [Version]\\library)")