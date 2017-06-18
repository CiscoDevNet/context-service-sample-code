package com.cisco.thunderhead.example.ui;

import com.cisco.thunderhead.client.ContextServiceClient;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.commons.lang3.StringUtils;
import org.bouncycastle.util.encoders.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Map;

/**]
 * This deals with retrieving connection data from the filesystem, and provide access to a client.
 */
public class ConnectionData {

    public static final String connectionDataFileName = "connectiondata.txt";

    private static final Logger LOGGER = LoggerFactory.getLogger(ConnectionData.class);
    private static String connectionData = "";
    private static ContextServiceClient contextServiceClient;

    private ConnectionData() {
        // private hides public constructor
    }

    public static synchronized String getConnectionData() {
        if (!StringUtils.isEmpty(connectionData)) {
            return connectionData;
        }

        String connectDataFilePath = (new File(".")).getAbsolutePath() + "/" + connectionDataFileName;
        LOGGER.info("Connection Data File Path: " + connectDataFilePath);
        try {
            BufferedReader connectDataFile = new BufferedReader(new FileReader(connectDataFilePath));
            connectionData = connectDataFile.readLine();
            LOGGER.info("Connection Data: " + connectionData);
        } catch (IOException e) {
            LOGGER.error("Error reading " + connectionDataFileName + ": " + e);
            connectionData = "";
        }

        return connectionData;
    }

    public static synchronized void setConnectionData(String connData) {
        connectionData = connData;
    }

    public static String getDecodedConnectionData(String encodedConnectionData) {
        try {
            String json = new String(Base64.decode(URLDecoder.decode(encodedConnectionData, "UTF-8")));
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            Map jsonMap = gson.fromJson(json, Map.class);
            return gson.toJson(jsonMap);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("failed to decode connection data", e);
        }
    }

    public static String getEncodedConnectionData(String decodedConnectionData) {
        try {
            String encodedConnectionData = URLEncoder.encode(new String(Base64.encode(decodedConnectionData.getBytes())), "UTF-8");
            return encodedConnectionData;
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("failed to decode connection data", e);
        }
    }

    public static Map getDecodedConnectionDataAsMap(String encodedConnectionData) {
        return new Gson().fromJson(getDecodedConnectionData(encodedConnectionData), Map.class);
    }

    public static void setContextServiceClient(ContextServiceClient csc) {
        contextServiceClient = csc;
    }

    public static ContextServiceClient getContextServiceClient() {
        return contextServiceClient;
    }

    public static boolean saveConnectionData(String text) {
        String connectDataFilePath = (new File(".")).getAbsolutePath() + "/" + connectionDataFileName;
        try (FileWriter fw = new FileWriter(connectDataFilePath)) {
            fw.write(text);
            LOGGER.info("Saved connection data to " + connectDataFilePath);
            return true;
        } catch (IOException e) {
            LOGGER.error("Error writing to file " + connectionDataFileName);
            return false;
        }
    }
}
