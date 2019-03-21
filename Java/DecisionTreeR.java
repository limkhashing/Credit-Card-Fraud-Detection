import org.rosuda.JRI.Rengine;

/**
 * Cardzone Fraud Detection project using Decision Tree Learning CART algorithm to detect fradulent transaction.
 * Please provide LOAD_DATASET_PATH for the pre-processed dataset that ouputted from Python
 * You can provide SAVE_MODEL_PATH to output the final trained model in your desired directory
 * You can provide EXPORT_RULE_PATH to output the decision rules in your desired directory
 *
 * @author Lim Kha Shing
 */
public class DecisionTreeR {

    private static Rengine engine;
    private static final int K_FOLD_NUMBER = 10;
    private static final String LOAD_DATASET_PATH = "src/main/resources/preprocessed_dataset.csv";
    private static final String EXPORT_TREE_RULE_PATH = "src/main/resources";
    private static final String SAVE_MODEL_PATH = "src/main/resources";

    public static void main(String[] args)  {

        engine = new Rengine(new String[] { "--vanilla" }, false, null);

        System.out.println("Rengine created, waiting for R");
        // the engine creates R is a new thread, so we should wait until it's ready
        if (!engine.waitForR()) {
            System.out.println("Cannot load R");
            return;
        }
        System.out.println("R is ready now\n");

        // Enables debug traces
    	// Rengine.DEBUG = 1;

        // Disable debug traces
    	// Rengine.DEBUG = 0;

        System.out.println("==================== Start ====================");
        System.out.println("Reading dataset...");
        engine.eval(String.format("df <- read.csv(\"%s\")", LOAD_DATASET_PATH));

        System.out.println("Read done");
        System.out.println(engine.eval("sprintf(\"Rows: %d Columns: %d\",nrow(df), length(names(df)))").asString());

        plotPieChart();

        // change some columns type to discrete / strings / categorial value
        engine.eval("cols <- c(\"AUTHTXN_FRAUD_CHECK\", \"AUTHTXN_MERCHANT_ID\", \"AUTHTXN_POS_ENTRY_MODE\",\n" +
                "          \"AUTHTXN_CURRENCY_CODE\", \"AUTHTXN_COUNTRY_CODE\", \"AUTHTXN_MCC_ID\",\n" +
                "          \"AUTHTXN_CUST_ID\", \"AUTHTXN_CRDACCT_NO\")");
        engine.eval("df[cols] <- lapply(df[cols], factor)");

        // print out the summary of each columns information
        System.out.println("\nSummary of each columns information");
        System.out.println("=======================================");
        String[] summary = engine.eval("capture.output(summary(df))").asStringArray();
        for (String s1 : summary) {
            System.out.println(s1);
        }

        System.out.println("Each columns type");
        System.out.println("==============================================");
        String[] columnsType = engine.eval("capture.output(sapply(df, class))").asStringArray();
        for (String s : columnsType) {
            System.out.println(s);
        }

        trainTestSplit();
        printTrainTestSetInfo();
        startTraining();

        testModelPredictions();
        printEvaluationScore();
//        plotPrecisionRecallCurve();
        generateDecisionTreeRules(EXPORT_TREE_RULE_PATH);

//        performCrossValidation(K_FOLD_NUMBER);

        testSingleTransaction();
        saveAsPMML(SAVE_MODEL_PATH);

//        saveModel(SAVE_MODEL_PATH);
//        loadModel();

        System.out.println("\n==================== End ====================");

//        engine.end();
    }

    /**
     * Load the trained model
     */
    private static void loadModel() {
        // the loaded variable name is model, based on save() method
        System.out.println("Loading back the saved Decision Tree model from RData...");
        engine.eval("load(\"src/main/resources/DecisionTree.RData\")");
        System.out.println("Load finish");
    }

    /**
     * Save the trained model
     */
    private static void saveModel(String path) {
        System.out.println("Saving Decision Tree Learning using CART Model as local disk...");
        engine.eval(String.format("save(model, file=\"%s/DecisionTree.RData\")", path));
        System.out.println("Finished Save..\n");
    }

    /**
     * Save the trained model as PMML
     */
    private static void saveAsPMML(String path) {
        System.out.println("Saving Decision Tree Model as PMML for deployment...");
        engine.eval("library(r2pmml)");
        engine.eval(String.format("r2pmml(model, \"%s/CardzoneDecisionTree.pmml\")", path));
        System.out.println("Finished Save..\n");
    }

    /**
     * On-Demand Predictions / Test prediction on a single transaction
     * First, reset the index of testing set first, then do prediction on a single row of test set
     * Second index of the string array is the final result for the prediction
     */
    private static void testSingleTransaction() {
        System.out.println("\nTest prediction on a single transaction");
        engine.eval("rownames(test_set) <- 1:nrow(test_set)");
        long startTime = System.currentTimeMillis(); // get the start time
        String[] predictSingleTransaction = engine.eval("capture.output(predict(model, newdata=test_set[12, ], type = 'class', na.action = na.omit))").asStringArray();
        System.out.println("Result: " + predictSingleTransaction[1]);
        System.out.println("Testing time on a single transaction takes " + getExecutionTime(startTime) + " milliseconds \n");
    }

    /**
     * Get the execution time for training and testing
     * @param startTime
     */
    private static long getExecutionTime(long startTime) {
        long stopTime = System.currentTimeMillis(); // get the end time
        return (stopTime - startTime);
    }

    /**
     * Plot precision-recall relationship
     */
    private static void plotPrecisionRecallCurve() {
        engine.eval("library(precrec)");
        engine.eval("autoplot(evalmod(scores = as.numeric(test_set$predictions), " +
                "labels = test_set$AUTHTXN_FRAUD_CHECK), 'PRC')");
    }

    /**
     * Perform cross validation by k-fold,
     * and print out the cross validation evaluation scores
     * @param kFoldNumber
     * An integer number that specify the number of k-fold
     */
    private static void performCrossValidation(int kFoldNumber) {
        System.out.println("Start perform cross-validation... ");
        long xp1 = engine.rniPutIntArray(new int[] { kFoldNumber });
        engine.rniAssign("k", xp1, 0);
        engine.eval("folds <- createFolds(train_set$AUTHTXN_FRAUD_CHECK, k = k, list=FALSE)");
        engine.eval("cross_accuracy <- list()");
        engine.eval("cross_precision <- list()");
        engine.eval("cross_recall <- list()");
        engine.eval("cross_f1 <- list()");

        engine.eval("for(i in 1:k) {\n" +
                "  # extract out the train and test set of fold\n" +
                "  testIndexes <- which(folds==i,arr.ind=TRUE)\n" +
                "  trainData   <- train_set[-testIndexes, ] \n" +
                "  testData    <- train_set[testIndexes, ] \n" +
                "  \n" +
                "  # perform cross validation of training and testing\n" +
                "  model <- rpart(AUTHTXN_FRAUD_CHECK~., data=trainData)\n" +
                "  cross_validation_result <- predict(model, testData, type='class') \n" +
                "  confusion_matrix <- confusionMatrix(cross_validation_result, testData$AUTHTXN_FRAUD_CHECK, \n" +
                "                                      dnn = c(\"Predictions\", \"Actuals\"), positive = '1' , mode = 'prec_recall')\n" +
                "\n" +
                "  # store the fold's metrics score in list\n" +
                "  cross_accuracy[[i]]   <- confusion_matrix$overall['Accuracy']\n" +
                "  cross_precision[[i]]  <- confusion_matrix$byClass['Precision']\n" +
                "  cross_recall[[i]]     <- confusion_matrix$byClass['Recall']\n" +
                "  cross_f1[[i]]         <- confusion_matrix$byClass['F1']\n" +
                "}");

        System.out.println("Finish cross-validation");

        // Average out the metrics score
        System.out.println("Results of cross-validation");
        System.out.println("==============================");
        System.out.println(engine.eval("sprintf(\"Accuracy of cross-validation is: %f\" , Reduce(\"+\",cross_accuracy)  / length(cross_accuracy))").asString());
        System.out.println(engine.eval("sprintf(\"Precision of cross-validation is: %f\", Reduce(\"+\",cross_precision) / length(cross_precision))").asString());
        System.out.println(engine.eval("sprintf(\"Recall of cross-validation is: %f\"   , Reduce(\"+\",cross_recall)    / length(cross_recall))").asString());
        System.out.println(engine.eval("sprintf(\"F1 of cross-validation is: %f\"       , Reduce(\"+\",cross_f1)        / length(cross_f1))").asString());
    }

    /**
     * Generate rules from builed Decision Tree
     * @param exportRulePath
     */
    private static void generateDecisionTreeRules(String exportRulePath) {
        System.out.println("\nGenerating Decision Tree rules to text file (DT_Rules.txt)...");
        engine.eval("library(rattle)");
        engine.eval(String.format("sink(\"%s/DT_Rules.txt\")", exportRulePath));
        engine.eval("rattle::asRules(model, FALSE)");
        engine.eval("sink()");
        System.out.println("Done generate rules to text file\n");
    }

    /**
     * Print evaluation score of tested model
     * First, it will get the Confusion Matrix from predictions and actual fraud label
     * then it will get the metric score from it
     */
    private static void printEvaluationScore() {
        engine.eval("confusion_matrix <- confusionMatrix(test_set$predictions, test_set$AUTHTXN_FRAUD_CHECK," +
                " dnn = c('Predictions', 'Actuals'), positive = '1' , mode = 'prec_recall')");

        System.out.println("\nConfusion Matrix");
        System.out.println("==============================");
        String[] confusionMatrix = engine.eval("capture.output(confusion_matrix$table)").asStringArray();
        for (int i = 0; i < confusionMatrix.length; i++) {
            if(i == 0) {
                System.out.println(confusionMatrix[i]);
                continue;
            } else {
                System.out.print(confusionMatrix[i]);
            }
            System.out.println();
        }

        System.out.println("\nMetrics Score");
        System.out.println("=================");
        System.out.println("Accuracy: "  + engine.eval("confusion_matrix$overall['Accuracy']").asDouble());
        System.out.println("Precision: " + engine.eval("confusion_matrix$byClass['Precision']").asDouble());
        System.out.println("Recall: "    + engine.eval("confusion_matrix$byClass['Recall']").asDouble());
        System.out.println("F1: "        + engine.eval("confusion_matrix$byClass['F1']").asDouble());
    }

    /**
     * Test model's prediction on testing set
     */
    private static void testModelPredictions() {
        System.out.println("Testing the Decision Tree performance by predicting onto testing set...");
        long startTime = System.currentTimeMillis(); // get the start time
        engine.eval("system.time(test_set$predictions <- predict(model, test_set, type = 'class'))");
        System.out.println("Test Finish");
        System.out.println("Testing time for test set takes " + getExecutionTime(startTime) + " milliseconds \n");
    }

    /**
     * start training onto Decision Tree model
     */
    private static void startTraining() {
        engine.eval("library(rpart)");
        long startTime = System.currentTimeMillis(); // get the start time
        System.out.println("\nStart Training...");

        engine.eval("model <- rpart(AUTHTXN_FRAUD_CHECK~., method=\"class\", data=train_set)");
        System.out.println("Train Finish");

        System.out.println("Training time takes " + getExecutionTime(startTime) + " milliseconds \n");
    }

    /**
     * Print train and test set information about the frauds distribution
     */
    private static void printTrainTestSetInfo() {
        System.out.println("\nTraining Set Information");
        System.out.println("---------------------------");
        String[] trainSetInfo = engine.eval("capture.output(table(train_set$AUTHTXN_FRAUD_CHECK))").asStringArray();
        for (int i = 0; i < trainSetInfo.length; i++) {
            if(i == 0) {
                continue;
            }
            System.out.println(trainSetInfo[i]);
        }

        System.out.println("\nTesting Set Information");
        System.out.println("---------------------------");
        String[] testSetInfo = engine.eval("capture.output(table(test_set$AUTHTXN_FRAUD_CHECK))").asStringArray();
        for (int i = 0; i < testSetInfo.length; i++) {
            if(i == 0) {
                continue;
            }
            System.out.println(testSetInfo[i]);
        }
    }

    /**
     * Perform splitting of training and testing set by Stratification sampling
     * set seed is for reproduceable results
     * without set seed, it will randomly assigns rows to train and test set each time it generated
     */
    private static void trainTestSplit() {
        System.out.println("\nTrain Test Split by Stratification");
        System.out.println("=====================================");

        engine.eval("library(caret)");
        engine.eval("set.seed(42)");
        engine.eval("train_test_split <- createDataPartition(df$AUTHTXN_FRAUD_CHECK, p = .7, list = FALSE)");
        engine.eval("train_set <- df[ train_test_split,]");
        engine.eval("test_set  <- df[-train_test_split,]");
    }

    /**
     * Plot pie chart for the frauds and legitimate transaction
     */
    private static void plotPieChart() {
        System.out.println("Plotting Pie Chart...");
        engine.eval("count <- table(df$AUTHTXN_FRAUD_CHECK)");
        engine.eval("pie_percent<- round(100*table(df$AUTHTXN_FRAUD_CHECK)/sum(table(df$AUTHTXN_FRAUD_CHECK)), 1)\n");
        engine.eval("pie(count, labels = pie_percent, main = \"Fraud Percentage\", col = table(df$AUTHTXN_FRAUD_CHECK))\n");
        engine.eval("legend(\"topright\", c(\"0\",\"1\"), cex = 0.8, fill = table(df$AUTHTXN_FRAUD_CHECK))\n");
        System.out.println("Done");
    }
}
