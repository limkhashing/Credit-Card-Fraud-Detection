import org.rosuda.JRI.Rengine;

/**
 * Classification Using Frequent Patterns
 * with Associative Classification, by Classification Based on Associations algorithm (CBA algorithm)
 *
 * Please provide LOAD_FRAUD_DATASET_PATH for the fraud set that ouputted from Python pre-process
 * You can provide EXPORT_CBA_RULE_PATH to output the classification rules in your desired directory
 *
 * @author Lim Kha Shing
 */
public class ClassficationUsingFP {

    private static Rengine engine;
    private static final String LOAD_FRAUD_DATASET_PATH = "src/main/resources/fraud_set.xlsx";
    private static final String EXPORT_CBA_RULE_PATH = "src/main/resources";

    public static void main(String[] args) {
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
        engine.eval("library(readxl)");
        engine.eval("library(arulesCBA)");
        System.out.println("Reading dataset...");
        engine.eval(String.format("df <- read_excel(\"%s\")", LOAD_FRAUD_DATASET_PATH));

        System.out.println("Read done");

        // Change all features as Factors (String / categorical / discrete value)
        engine.eval("df[] <- lapply(df, factor)");

        // change the dataset into transaction type (Market Basket)
        engine.eval("trans <- as(discretizeDF.supervised(AUTHTXN_FRAUD_CHECK~., df), \"transactions\")\n");

        // print the summary information about transactions market basket
        String[] summaryDatasetInformation = engine.eval("capture.output(summary(trans))").asStringArray();
        for (String information : summaryDatasetInformation) {
            System.out.println(information);
        }

        startCBA();
        printCbaRulesInfo();
        generateCbaRules(EXPORT_CBA_RULE_PATH);

        System.out.println("\n==================== End ====================");
        engine.end();
    }

    /**
     * Print summarized information about the classification rules
     * that mined from fraud set using CBA algorithm
     */
    private static void printCbaRulesInfo() {
        System.out.println("Print summarized information about classification rules");
        System.out.println("=========================================================");
        String[] summaryRulesInformation = engine.eval("capture.output(summary(rules))").asStringArray();
        for (String information : summaryRulesInformation) {
            System.out.println(information);
        }
    }

    /**
     * Start CBA algorithm in mining classification rules
     */
    private static void startCBA() {
        System.out.println("\nClassification using Frequent Patterns with Associative Classification");
        System.out.println("By Classification Based on Associations algorithm (CBA algorithm)");
        System.out.println("=================================================");
        System.out.println("Mining classification rules...");

        long startTime = System.currentTimeMillis(); // get the start time
        engine.eval("library(arc)");
        engine.eval("appearance <- list(rhs =  c(\"AUTHTXN_FRAUD_CHECK=D\"), default=\"lhs\")\n");
        engine.eval("system.time(rules <- topRules(trans, appearance, minlen = 2, init_maxlen = 5, iteration_timeout = 10,\n" +
                "                               init_support = 0.5, init_conf = 0.5))");

        System.out.println("Mining Finished");
        System.out.println("Mining time takes " + getExecutionTime(startTime) + " milliseconds \n");
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
     * Generate rules into a text file from CBA algorithm
     * @param exportRulePath
     */
    private static void generateCbaRules(String exportRulePath) {
        System.out.println("\nGenerating Classification Based on Associations rules to text file (CBA_Rules.txt)...");
        engine.eval(String.format("sink(\"%s/CBA_Rules.txt\")", exportRulePath));
        engine.eval("inspect(rules)");
        engine.eval("sink()");
        System.out.println("Done generate rules to text file");
    }
}
