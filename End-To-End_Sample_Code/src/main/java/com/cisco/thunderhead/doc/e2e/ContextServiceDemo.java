package com.cisco.thunderhead.doc.e2e;

import com.cisco.thunderhead.client.ContextServiceClient;
import com.cisco.thunderhead.client.ContextServiceClientConstants;
import com.cisco.thunderhead.connector.ConnectorConfiguration;
import com.cisco.thunderhead.connector.ManagementConnector;
import com.cisco.thunderhead.connector.info.ConnectorInfoImpl;
import com.cisco.thunderhead.doc.examples.ConnectionData;
import com.cisco.thunderhead.plugin.ConnectorFactory;
import com.cisco.thunderhead.pod.Pod;
import com.cisco.thunderhead.util.DataElementUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;

public class ContextServiceDemo {
    private final static Logger LOGGER = LoggerFactory.getLogger(ContextServiceDemo.class);

    /**
     * Show full initialization flow for Context Service,
     * demonstrate a basic operation.
     */
    public static void main(String ... args) {
        // load our pre-created connection data
        String connectionData = ConnectionData.getConnectionData();

        // initialize connector factory
        String pathToConnectorProperty = Paths.get("./connector.property").toAbsolutePath().toString();
        ConnectorFactory.initializeFactory(pathToConnectorProperty);
        LOGGER.info("Initialized Connector Factory");

        // initialize management connector
        ManagementConnector managementConnector = ConnectorFactory.getConnector(ManagementConnector.class);
        String hostname = "doctest.example.com";
        ConnectorInfoImpl connInfo = new ConnectorInfoImpl(hostname);
        ConnectorConfiguration configuration = new ConnectorConfiguration(){{
            addProperty("LAB_MODE", true); // exclude this line for prod mode
            addProperty("REQUEST_TIMEOUT", 10000);
            //TEST ONLY BEGIN - Do not use in production
            addProperty(ContextServiceClientConstants.NO_MANAGEMENT_CONNECTOR, getNoManagementConnector());
            //TEST ONLY END - Do not use in production
        }};
        managementConnector.init(connectionData, connInfo, configuration);
        LOGGER.info("Initialized management connector");

        // initialize context service client
        ContextServiceClient contextServiceClient = ConnectorFactory.getConnector(ContextServiceClient.class);
        // reuse configuration we used for management connector
        contextServiceClient.init(connectionData, connInfo, configuration);
        LOGGER.info("Initialized Context Service client");

        // Now we can use Context Service!
        // e.g. create a Pod:
        Pod pod = new Pod(
                DataElementUtils.convertDataMapToSet(
                        new HashMap<String, Object>() {{
                            put("Context_Notes", "Context Service Demo POD");
                            put("Context_POD_Activity_Link", "http://myservice.example.com/service/ID/xxxx");
                        }}
                )
        );
        pod.setFieldsets(Arrays.asList("cisco.base.pod"));
        contextServiceClient.create(pod);
        LOGGER.info("Created Pod: " + pod.getId());

        // Do anything else you want to try here!
        // e.g. create data, update data, search for data
    }

    public static boolean getNoManagementConnector() {
        String noManagementConnector = System.getenv(ContextServiceClientConstants.NO_MANAGEMENT_CONNECTOR);
        LOGGER.info("NO_MANAGEMENT_CONNECTOR: " + noManagementConnector);
        if (noManagementConnector==null) {
            noManagementConnector = "false";
        }
        return StringUtils.equalsIgnoreCase(noManagementConnector,"true");
    }
}
