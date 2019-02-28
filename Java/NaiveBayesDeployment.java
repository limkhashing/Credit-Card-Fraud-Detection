import java.io.*;
import java.net.MalformedURLException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class NaiveBayesDeployment {

    private final static String wml_service_credentials_url = "https://us-south.ml.cloud.ibm.com";
    private final static String wml_service_credentials_username = "replace_your_own_username";
    private final static String wml_service_credentials_password = "replace_your_own_password";

    public static void main(String[] args) throws IOException {

        // NOTE: you must manually construct wml_credentials hash map below
        // using information retrieved from your IBM Cloud Watson Machine Learning Service instance.

        Map<String, String> wml_credentials = new HashMap<String, String>()
        {{
            put("url", wml_service_credentials_url);
            put("username", wml_service_credentials_username);
            put("password", wml_service_credentials_password);
        }};

        String wml_auth_header = "Basic " +
                Base64.getEncoder().encodeToString((wml_credentials.get("username") + ":" +
                        wml_credentials.get("password")).getBytes(StandardCharsets.UTF_8));
        String wml_url = wml_credentials.get("url") + "/v3/identity/token";
        HttpURLConnection tokenConnection = null;
        HttpURLConnection scoringConnection = null;
        BufferedReader tokenBuffer = null;
        BufferedReader scoringBuffer = null;
        try {
            // Getting WML token
            URL tokenUrl = new URL(wml_url);
            tokenConnection = (HttpURLConnection) tokenUrl.openConnection();
            tokenConnection.setDoInput(true);
            tokenConnection.setDoOutput(true);
            tokenConnection.setRequestMethod("GET");
            tokenConnection.setRequestProperty("Authorization", wml_auth_header);
            tokenBuffer = new BufferedReader(new InputStreamReader(tokenConnection.getInputStream()));
            StringBuffer jsonString = new StringBuffer();
            String line;
            while ((line = tokenBuffer.readLine()) != null) {
                jsonString.append(line);
            }

            // Scoring request, calling Naive Bayes model
            URL scoringUrl = new URL("\thttps://us-south.ml.cloud.ibm.com/v3/wml_instances/ce6494a0-7302-4176-873c-5effaa86d19c/deployments/45ae2c0c-c581-4798-842d-6c55ae32e1e5/online");
            String wml_token = "Bearer " +
                    jsonString.toString()
                            .replace("\"","")
                            .replace("}", "")
                            .split(":")[1];
            scoringConnection = (HttpURLConnection) scoringUrl.openConnection();
            scoringConnection.setDoInput(true);
            scoringConnection.setDoOutput(true);
            scoringConnection.setRequestMethod("POST");
            scoringConnection.setRequestProperty("Accept", "application/json");
            scoringConnection.setRequestProperty("Authorization", wml_token);
            scoringConnection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            OutputStreamWriter writer = new OutputStreamWriter(scoringConnection.getOutputStream(), StandardCharsets.UTF_8);

            // NOTE: manually define and pass the array(s) of values to be scored in the next line

            // Example : Fraud transaction
            String payload = "{\"fields\":[\"AUTHTXN_TXNTYPE_ID\",\"AUTHTXN_CARD_NO\",\"AUTHTXN_REQUEST_AMT\",\"AUTHTXN_POS_ENTRY_MODE\",\"AUTHTXN_MERCHANT_ID\",\"AUTHTXN_TERMINAL_ID\",\"AUTHTXN_COUNTRY_CODE\",\"AUTHTXN_CURRENCY_CODE\",\"AUTHTXN_MCC_ID\",\"AUTHTXN_CUST_ID\",\"AUTHTXN_NET_AMT\",\"AUTHTXN_SOURCE_BIZMODE\",\"AUTHTXN_DEST\",\"AUTHTXN_DEST_BIZMODE\",\"AUTHTXN_CHARGE_AT_IND\",\"AUTHTXN_BILLING_TXN_AMT\",\"AUTHTXN_CRDACCT_NO\",\"AUTHTXN_CRDPLAN_ID\",\"MOD_USER\",\"AUTHTXN_TXN_ENTRY_MODE\",\"AUTHTXN_CNTRYGRP_ID\",\"AUTHTXN_ACQ_COMPONENT_ID\",\"AUTHTXN_VMCCGRP_ID\",\"AUTHTXN_VCNTRYGRP_ID\",\"Year\",\"Month\",\"Day\",\"Day_of_week\",\"Hour\",\"Period_of_time\"],\"values\":[[\"SALES\",\"C8286C6B5597E76C3B96BDF7E5DE913EE20F1BF6C4A49776\",5000,\"51\",\"922000000000000\",\"98158467\",\"586\",\"586\",\"0\",\"123936\",5000,\"A\",\"CZCMS\",\"H\",\"S\",5000,\"7480000000000000000\",\"4V_GOLD\",\"SYSTEM\",\"0\",\"0\",\"ONUS_DOM\",\"0\",\"0\",2017,12,6,2,1,\"Midnight\"]]}";

            // Example : Legitimate transaction
            // String payload = "{\"fields\":[\"AUTHTXN_TXNTYPE_ID\",\"AUTHTXN_CARD_NO\",\"AUTHTXN_REQUEST_AMT\",\"AUTHTXN_POS_ENTRY_MODE\",\"AUTHTXN_MERCHANT_ID\",\"AUTHTXN_TERMINAL_ID\",\"AUTHTXN_COUNTRY_CODE\",\"AUTHTXN_CURRENCY_CODE\",\"AUTHTXN_MCC_ID\",\"AUTHTXN_CUST_ID\",\"AUTHTXN_NET_AMT\",\"AUTHTXN_SOURCE_BIZMODE\",\"AUTHTXN_DEST\",\"AUTHTXN_DEST_BIZMODE\",\"AUTHTXN_CHARGE_AT_IND\",\"AUTHTXN_BILLING_TXN_AMT\",\"AUTHTXN_CRDACCT_NO\",\"AUTHTXN_CRDPLAN_ID\",\"MOD_USER\",\"AUTHTXN_TXN_ENTRY_MODE\",\"AUTHTXN_CNTRYGRP_ID\",\"AUTHTXN_ACQ_COMPONENT_ID\",\"AUTHTXN_VMCCGRP_ID\",\"AUTHTXN_VCNTRYGRP_ID\",\"Year\",\"Month\",\"Day\",\"Day_of_week\",\"Hour\",\"Period_of_time\"],\"values\":[[\"SALES\",\"C8286C6B5597E76C3B96BDF7E5DE913EE20F1BF6C4A49776\",5000,\"51\",\"922000000000000\",\"98158467\",\"586\",\"586\",\"0\",\"123936\",5000,\"A\",\"CZCMS\",\"H\",\"S\",5000,\"7480000000000000000\",\"4V_GOLD\",\"SYSTEM\",\"0\",\"0\",\"ONUS_DOM\",\"0\",\"0\",2017,12,6,2,8,\"Midnight\"]]}";

            writer.write(payload);
            writer.close();

            scoringBuffer = new BufferedReader(new InputStreamReader(scoringConnection.getInputStream()));
            StringBuffer jsonStringScoring = new StringBuffer();
            String lineScoring;
            while ((lineScoring = scoringBuffer.readLine()) != null) {
                jsonStringScoring.append(lineScoring);
            }
            System.out.println(jsonStringScoring);
        } catch (IOException e) {
            System.out.println("The URL is not valid.");
            System.out.println(e.getMessage());
        }
        finally {
            if (tokenConnection != null) {
                tokenConnection.disconnect();
            }
            if (tokenBuffer != null) {
                tokenBuffer.close();
            }
            if (scoringConnection != null) {
                scoringConnection.disconnect();
            }
            if (scoringBuffer != null) {
                scoringBuffer.close();
            }
        }
    }
}