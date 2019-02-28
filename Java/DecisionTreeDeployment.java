import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

public class DecisionTreeDeployment {

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
            // Scoring request, calling Decision Tree model
            URL scoringUrl = new URL("https://us-south.ml.cloud.ibm.com/v3/wml_instances/ce6494a0-7302-4176-873c-5effaa86d19c/deployments/260e8d25-5aa9-436d-b8d7-17d3aedd4e82/online");
            String wml_token = "Bearer " +
                    jsonString.toString()
                            .replace("\"","")
                            .replace("}", "")
                            .split(":")[1];

            scoringConnection = (HttpURLConnection) scoringUrl.openConnection();
            scoringConnection.setDoInput(true);
            scoringConnection.setDoOutput(true);
            scoringConnection.setRequestMethod("GET");
            scoringConnection.setRequestProperty("Accept", "application/json");
            scoringConnection.setRequestProperty("Authorization", wml_token);
            scoringConnection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            OutputStreamWriter writer = new OutputStreamWriter(scoringConnection.getOutputStream(), StandardCharsets.UTF_8);

            // NOTE: manually define and pass the array(s) of values to be scored in the next line
            // Can do it dynamically after receive inputs from payment gateway

            // Example : Fraud transaction
            String payload = "{\"fields\":[\"AUTHTXN_TXNTYPE_ID\",\"AUTHTXN_CARD_NO\",\"AUTHTXN_REQUEST_AMT\",\"AUTHTXN_MERCHANT_ID\",\"AUTHTXN_TERMINAL_ID\",\"AUTHTXN_CURRENCY_CODE\",\"AUTHTXN_CUST_ID\",\"Hour\"],\"values\":[[\"SALES\",\"C8286C6B5597E76C3B96BDF7E5DE913EE20F1BF6C4A49776\",5000,\"922000000000000\",\"98158467\",\"586\",\"123936\",1]]}";

            // Example : Legitimate transaction
            // String payload = "{\"fields\":[\"AUTHTXN_TXNTYPE_ID\",\"AUTHTXN_CARD_NO\",\"AUTHTXN_REQUEST_AMT\",\"AUTHTXN_MERCHANT_ID\",\"AUTHTXN_TERMINAL_ID\",\"AUTHTXN_CURRENCY_CODE\",\"AUTHTXN_CUST_ID\",\"Hour\"],\"values\":[[\"SALES\",\"C8286C6B5597E76C3B96BDF7E5DE913EE20F1BF6C4A49776\",5000,\"922000000000000\",\"98158467\",\"586\",\"123936\",8]]}";

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