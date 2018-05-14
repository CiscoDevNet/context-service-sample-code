package com.cisco.thunderhead.rest;

import com.cisco.thunderhead.client.ContextServiceClient;
import com.cisco.thunderhead.client.ContextServiceClientConstants;
import com.cisco.thunderhead.connector.ConnectorConfiguration;
import com.cisco.thunderhead.connector.ManagementConnector;
import com.cisco.thunderhead.connector.info.ConnectorInfoImpl;
import com.cisco.thunderhead.plugin.ConnectorFactory;
import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContext;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Various utility methods.
 */
public class Utils {
    private static final Logger LOGGER = LoggerFactory.getLogger(Utils.class);
    private static final String CONNECTION_DATA_FILE_PROP = "connection.info.file";
    private static final String CONNECTION_DATA_PROP = "connection.data";

    public static ContextServiceClient getInitializedContextServiceClient(File connectorPropertyFile, String jsonConnectionData) {
        // initialize connector factory
        ConnectorFactory.initializeFactory(connectorPropertyFile.getAbsolutePath());
        LOGGER.info("Initialized Connector Factory");

        boolean labMode = true;
        int requestTimeOut = 10000;
        boolean noFms = true;

        String hostname = "doctest.example.com";
        ConnectorInfoImpl connInfo = new ConnectorInfoImpl(hostname);
        ConnectorConfiguration configuration = new ConnectorConfiguration(){{
            addProperty(ContextServiceClientConstants.LAB_MODE, labMode); // exclude this line for prod mode
            addProperty(ContextServiceClientConstants.REQUEST_TIMEOUT, requestTimeOut);
//            addProperty(ContextServiceClientConstants.NO_MANAGEMENT_CONNECTOR, noFms);
        }};

        // initialize context service client
        ContextServiceClient contextServiceClient = ConnectorFactory.getConnector(ContextServiceClient.class);
        // reuse configuration we used for management connector
        contextServiceClient.init(jsonConnectionData, connInfo, configuration);
        LOGGER.info("Initialized Context Service client");
        return contextServiceClient;
    }

    public static ManagementConnector getInitializedManagementConnector(File connectorPropertyFile, String jsonConnectionData) {
        // initialize connector factory
        ConnectorFactory.initializeFactory(connectorPropertyFile.getAbsolutePath());
        LOGGER.info("Initialized Connector Factory");

        boolean labMode = true;
        int requestTimeOut = 10000;
        boolean noFms = true;

        // initialize management connector
        ManagementConnector managementConnector = ConnectorFactory.getConnector(ManagementConnector.class);
        String hostname = "doctest.example.com";
        ConnectorInfoImpl connInfo = new ConnectorInfoImpl(hostname);
        ConnectorConfiguration configuration = new ConnectorConfiguration(){{
            addProperty(ContextServiceClientConstants.LAB_MODE, labMode); // exclude this line for prod mode
            addProperty(ContextServiceClientConstants.REQUEST_TIMEOUT, requestTimeOut);
//            addProperty(ContextServiceClientConstants.NO_MANAGEMENT_CONNECTOR, noFms);
        }};
        managementConnector.init(jsonConnectionData, connInfo, configuration);
        LOGGER.info("Initialized management connector");
        return managementConnector;
    }

    /**
     * This initializes the Context Service extension JAR properties file.
     */
    public static File initializeConnectorPropertyFile(ServletContext servletContext) {
        // assumes running in Tomcat
        String contextServiceSdkExtensionName = "context-service-sdk-extension-2.0.4.jar";
        URL extensionUrl = null;
        try {
            // Find the location of the extension JAR bundled into the WAR.
            extensionUrl = servletContext.getResource("/context-service-extension/" + contextServiceSdkExtensionName);
        } catch (MalformedURLException e) {
            throw new RuntimeException("failed while retrieving " + contextServiceSdkExtensionName + " from WAR file", e);
        }

        // Create a connector properties file to use to initialize the SDK.
        String homeDir = System.getProperty("catalina.base");
        String path = homeDir + "/context-service";
        File connectorPropertyFile = new File(path, "connector.property");

        if (!connectorPropertyFile.exists()) {
            if (!connectorPropertyFile.getParentFile().exists()) {
                connectorPropertyFile.getParentFile().mkdir();
            }
            Properties props = new Properties();
            try {
                props.setProperty("path", new File(extensionUrl.toURI()).getParent());
                props.setProperty("jar-name", contextServiceSdkExtensionName);
            } catch (URISyntaxException e) {
                throw new RuntimeException("failed to initialize properties file", e);
            }

            // Save the connector property file in the filesystem.
            try (OutputStream os = new FileOutputStream(connectorPropertyFile)) {
                props.store(os, new Date().toString());
            } catch (IOException e) {
                throw new RuntimeException("failed to initialize properties file", e);
            }
            LOGGER.info("Connector property file located at " + connectorPropertyFile.getAbsolutePath());
        }
        return connectorPropertyFile;
    }

    /**
     * Formats an error as JSON.
     */
    public static String getError(String message) {
        Map<String,String> error = new HashMap<>();
        error.put("error", message);
        return new Gson().toJson(error);
    }

    /**
     * Reads the connection data from a properties file.
     */
    public static String getConnectionData() {
        Properties properties = readProperties();
        if (properties==null) {
            return null;
        }

        String connectionData = properties.getProperty(CONNECTION_DATA_PROP);
        LOGGER.warn("property not defined: " + CONNECTION_DATA_PROP);
        if (connectionData==null) {
            return null;
        }
        LOGGER.info("successfully read connection data"); // don't log the connection data itself
        return connectionData;
    }

    /**
     * Reads the properties into memory.
     */
    private static Properties readProperties() {
        String connectionDataFilename = System.getProperty(CONNECTION_DATA_FILE_PROP);
        if (connectionDataFilename==null) {
            LOGGER.warn("property must be set: " + CONNECTION_DATA_FILE_PROP);
            return null;
        }
        File connectionDataFile = new File(connectionDataFilename);

        if (!connectionDataFile.exists()) {
            LOGGER.warn("property file does not exist: " + connectionDataFilename);
            return null;
        }

        Properties properties = new Properties();
        try {
            properties.load(new FileInputStream(connectionDataFile));
            return properties;
        } catch (IOException e) {
            LOGGER.warn("problem reading file: " + connectionDataFilename, e);
            return null;
        }
    }

    /**
     * Writes the latest connection data to persistent storage.
     * @param connectionData the connection data, or null to clear it (deregistration)
     */
    public static boolean saveConnectionData(String connectionData) {
        Properties properties = readProperties();

        if (connectionData!=null) {
            properties.setProperty(CONNECTION_DATA_PROP, connectionData);
        } else {
            properties.remove(CONNECTION_DATA_PROP);
        }

        String connectionDataFilename = System.getProperty(CONNECTION_DATA_FILE_PROP);
        try (FileOutputStream os = new FileOutputStream(connectionDataFilename)){
            properties.store(os, "");
            LOGGER.info("successfully updated the connection data");
            return true;
        } catch (IOException e) {
            LOGGER.error("problem writing to file: " + connectionDataFilename, e);
            return false;
        }
    }
}
