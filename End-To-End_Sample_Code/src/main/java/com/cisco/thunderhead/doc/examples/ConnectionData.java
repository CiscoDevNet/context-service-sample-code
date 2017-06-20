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
    //TODO: delete this commented out -- for testing locally only
    //private static String connectionData = "eyJjcmVkZW50aWFscyI6eyJuYW1lIjoiQ1MtVGVzdC1NQS1Ob25BZG1pbi1FaW5zdGVpbiIsInBhc3N3b3JkIjoiNn5YdnhkWEdQc3szRnw5NDBvRVx1MDAzZWs2TiUqNV43RihwayIsIm9yZ0lkIjoiNGVhZTYzMTAtZTEyMi00YzA0LWEyMDYtOTNjYTU4MDhjOThjIiwiY2xpZW50SWQiOiJDNmNmNDU4NTBjYTY3YWVlOGI4ODQ0MWE3NTFiMmVlYzM4NTYwNTZjMDI3YjAyMmZhNzMwMGUzYjBiM2VhOTM2ZSIsImNsaWVudFNlY3JldCI6ImUzMzVmMjBiMGQ1MWU2ODE3ZTY2MzY1YjE5NzNjMWNmNDE2MjJmZWMxYTE1NzhkOWUwNzUwNWZiNDZhYmJhYTMiLCJtYWNoaW5lQWNjb3VudExvY2F0aW9uIjoiNmExYmE1YzYtZmY3YS00MGNkLTlkODYtYmNkY2RkYjRjYTAxIiwiY2lzVXVpZCI6IjZhMWJhNWM2LWZmN2EtNDBjZC05ZDg2LWJjZGNkZGI0Y2EwMSJ9LCJjcmVkZW50aWFsc0xhYk1vZGUiOnsibmFtZSI6IkNTLVRlc3QtTUEtTm9uQWRtaW4tRWluc3RlaW4iLCJwYXNzd29yZCI6IjZ%2BWHZ4ZFhHUHN7M0Z8OTQwb0VcdTAwM2VrNk4lKjVeN0YocGsiLCJvcmdJZCI6IjRlYWU2MzEwLWUxMjItNGMwNC1hMjA2LTkzY2E1ODA4Yzk4YyIsImNsaWVudElkIjoiQzZjZjQ1ODUwY2E2N2FlZThiODg0NDFhNzUxYjJlZWMzODU2MDU2YzAyN2IwMjJmYTczMDBlM2IwYjNlYTkzNmUiLCJjbGllbnRTZWNyZXQiOiJlMzM1ZjIwYjBkNTFlNjgxN2U2NjM2NWIxOTczYzFjZjQxNjIyZmVjMWExNTc4ZDllMDc1MDVmYjQ2YWJiYWEzIiwibWFjaGluZUFjY291bnRMb2NhdGlvbiI6IjZhMWJhNWM2LWZmN2EtNDBjZC05ZDg2LWJjZGNkZGI0Y2EwMSIsImNpc1V1aWQiOiI2YTFiYTVjNi1mZjdhLTQwY2QtOWQ4Ni1iY2RjZGRiNGNhMDEifSwiZGlzY292ZXJ5VXJsIjoiZGlzY292ZXJ5LnByb2R1czEuY2lzY29jY3NlcnZpY2UuY29tIiwiaWRlbnRpdHlCcm9rZXJVcmwiOiJpZGJyb2tlci53ZWJleC5jb20iLCJmbXNVcmwiOiJoZXJjdWxlcy1hLndieDIuY29tIiwiY2x1c3RlciI6eyJjbHVzdGVySWQiOiJTREtUZXN0QmFzZS5jb250ZXh0LmNsdXN0ZXIuYTM1MTM4NDAtNzRlYi00OTUzLWExN2QtY2Q1ODBmOTM3MzEyIiwiY2x1c3Rlck5hbWUiOiJTREtUZXN0QmFzZSBjb250ZXh0IGNsdXN0ZXIifX0%3D";
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
