import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.mllib.fpm.AssociationRules;
import org.apache.spark.mllib.fpm.FPGrowth;
import org.apache.spark.mllib.fpm.FPGrowthModel;

import java.io.*;
import java.util.*;

/**
 * Cardzone Fraud Detection project using FP-growth algorithm to mine frequent patterns from fraud set
 * Fraud set are dataset that undergo pre-process in Python and extracted only AUTHTXN_FRAUD_CHECK as D
 *
 * @author Lim Kha Shing
 */
public class FPGrowthSpark {

    private static FPGrowthModel<String> model;
    private static final String inputExcelFileName = "fraud_set.xlsx";

    public static void main(String[] args) throws IOException {

        // Initialize Apache Spark session
        SparkConf sparkConf = new SparkConf().setAppName("FPGrowthSpark").setMaster("local");
        JavaSparkContext sc = new JavaSparkContext(sparkConf);
        sc.setLogLevel("ERROR");

        File inputFile = new File("src/main/resources/" + inputExcelFileName);

        System.out.println("Start to generate frequent patterns based on FP-growth algorithm");
        System.out.println("=================================================================");
        xlsxConvertToTXT(inputFile.getAbsolutePath(), "OldItemset.txt");
        checkUniqueItemset("OldItemset.txt");

        JavaRDD<String> data = sc.textFile("ProcessedItemset.txt");

        // Preprocess the input file data file. Separate the itemset in split() according to the file
        JavaRDD<List<String>> transactions = data.map(line -> Arrays.asList(line.split(" ")));

        long startTime = System.currentTimeMillis(); // get the start time
        printFrequentPatterns(transactions, 0.5);
        System.out.println("Time taken to mine frequent patterns " + getExecutionTime(startTime) + " milliseconds \n");
        System.out.println("The total number of frequentt patterns generated: " + getNumberOfPatterns());

//        printAssociationRule(0.5);

        sc.stop();
    }

    /**
     * Printing Frequent itemset from a transactions (dataset)
     *
     * @param transactions
     *
     */
    private static void printFrequentPatterns(JavaRDD<List<String>> transactions, double minSup) throws FileNotFoundException, UnsupportedEncodingException {

        // .setMinSupport(): the minimum support for an itemset to be identified as frequent.
        // For example, if an item appears 3 out of 5 transactions, it has a support of 3/5=0.6
        // Minimum support is used to prune the associations that are less frequent.

        // numPartitions: the number of partitions used to distribute the work.
        FPGrowth fpGrowth = new FPGrowth()
                .setMinSupport(minSup) // Range from 0 to 1
                .setNumPartitions(1024);

        model = fpGrowth.run(transactions);

        System.out.println("Writing the results of frequent itemsets into text file...");
        PrintWriter writer = new PrintWriter("Frequent Itemsets.txt", "UTF-8");
        writer.print("[Frequent Patterns] | Item Support");
        writer.println();

        for (FPGrowth.FreqItemset<String> itemset: model.freqItemsets().toJavaRDD().collect()) {
            writer.println(itemset.javaItems() + " | Item Support = " + itemset.freq());
        }
        writer.close();
        System.out.println("Frequent Itemsets Write Finish\n");
    }

    /**
     * Generating Association Rule from the frequent itemset
     * minConfidence is the threshold that indicate how often the rule must found to be true.
     *
     * @param minConfidence
     */
    private static void printAssociationRule(double minConfidence) throws FileNotFoundException, UnsupportedEncodingException {

        System.out.println("Writing the results of Association Rule into text file...");
        PrintWriter writer = new PrintWriter("Association Rules.txt", "UTF-8");
        writer.print("[Rule Antecedent] => Consequent | Min Confidence");
        writer.println();

        for (AssociationRules.Rule<String> rule : model.generateAssociationRules(minConfidence).toJavaRDD().collect()) {
            writer.println(rule.javaAntecedent() + " => " + rule.javaConsequent() + " | Min Confidence = " + rule.confidence());
        }
        writer.close();
        System.out.println("Association Rules Write Finish");
    }

    /**
     * This method is to check whether there is duplicate items in a itemset (in a row)
     * If there is duplicate items, the following duplicate item will add counter at behind
     * This method also will output a final ProcessedItemset.txt which FP-growth will use it as input file
     *
     * @param textFile
     */
    private static void checkUniqueItemset(String textFile) throws IOException {

        System.out.println("Start checks duplicated items and adding a subscript behind...");
        BufferedReader bufferedReader = new BufferedReader(new FileReader(textFile));
        PrintWriter writer = new PrintWriter("ProcessedItemset.txt", "UTF-8");
        String readLine;

        while ((readLine = bufferedReader.readLine()) != null) {

            String[] list = readLine.split(" ");
            Set<String> set = new LinkedHashSet<>();

            for (String str : list) {
                String value = str;
                // Iterate as long as you can't add the value indicating that we have
                // already the value in the set
                for (int i = 2; !set.add(value); i++) {
                    value = str + "__(" + i + ")";
                }
                writer.print(value + " ");
            }
            writer.println();
        }
        writer.close();
        System.out.println("Finished checks duplicated items and adding a subscript. The new processed file is ProcessedItemset.txt\n");
    }

    /**
     * This method is to convert XLSX (Excel) file into TXT file which FP-growth can handle
     *
     * @param excelFilePath
     * @param newTextFileName
     */
    public static void xlsxConvertToTXT(String excelFilePath, String newTextFileName) throws IOException {

        System.out.println("Converting into text file....");

        File excelFile = new File(excelFilePath);
        FileInputStream excelFIS = new FileInputStream(excelFile);

        // we create an XSSF Workbook object for our XLSX Excel File
        XSSFWorkbook workbook = new XSSFWorkbook(excelFIS);

        // we get first sheet. Change the indexz according to the excel sheet
        XSSFSheet sheet = workbook.getSheetAt(0);

        // we iterate on rows
        Iterator<Row> iterator = sheet.iterator();
        iterator.next(); // skip the header row

        PrintWriter writer = new PrintWriter(newTextFileName, "UTF-8");

        while(iterator.hasNext()) {
            Row row = iterator.next();

            // iterate on cells for the current row
            Iterator<Cell> cellIterator = row.cellIterator();

            while (cellIterator.hasNext()) {
                Cell cell = cellIterator.next();
//                System.out.print(cell.toString() + " ");

                // write to txt file
                writer.print(cell.toString() + " ");
            }
            writer.println();
//            System.out.println();
        }
        writer.close();
        workbook.close();
        excelFIS.close();
        System.out.println("Finish Convert. Outputted File as " + newTextFileName + "\n");
    }

    /**
     * Get the execution time for training and testing
     * @param startTime
     */
    private static long getExecutionTime(long startTime) {
        long stopTime = System.currentTimeMillis(); // get the end time
        return (stopTime - startTime);
    }

    private static int getNumberOfPatterns() throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader("Frequent Itemsets.txt"));
        int lines = 0;
        while (reader.readLine() != null) lines++;
        reader.close();
        return lines;
    }
}
