package com.cisco.thunderhead.doc.examples;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class ConnectionData {

    public static final String connectionDataFileName = "connectiondata.txt";

    private static final Logger LOGGER = LoggerFactory.getLogger(ConnectionData.class);
    private static String connectionData;

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
}
