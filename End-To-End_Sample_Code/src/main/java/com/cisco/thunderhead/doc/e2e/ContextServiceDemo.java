package com.cisco.thunderhead.doc.e2e;

import com.cisco.thunderhead.client.ContextServiceClient;
import com.cisco.thunderhead.client.ContextServiceClientConstants;
import com.cisco.thunderhead.connector.ConnectorConfiguration;
import com.cisco.thunderhead.connector.ManagementConnector;
import com.cisco.thunderhead.connector.info.ConnectorInfoImpl;
import com.cisco.thunderhead.connector.pwreset.CredentialsChangedListener;
import com.cisco.thunderhead.connector.states.ConnectorState;
import com.cisco.thunderhead.connector.states.ConnectorStateListener;
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
        // Load our pre-created connection data
        String connectionData = ConnectionData.getConnectionData();

        // Initialize connector factory
        String pathToConnectorProperty = Paths.get("./connector.property").toAbsolutePath().toString();
        ConnectorFactory.initializeFactory(pathToConnectorProperty);
        LOGGER.info("Initialized Connector Factory");

        // Initialize Management connector
        ManagementConnector managementConnector = ConnectorFactory.getConnector(ManagementConnector.class);
        // Initialize Context Service Client
        ContextServiceClient contextServiceClient = ConnectorFactory.getConnector(ContextServiceClient.class);

        String hostname = "doctest.example.com";
        ConnectorInfoImpl connInfo = new ConnectorInfoImpl(hostname);
        ConnectorConfiguration configuration = new ConnectorConfiguration(){{
            addProperty("LAB_MODE", true); // exclude this line for production mode
            addProperty("REQUEST_TIMEOUT", 10000);
            //TEST ONLY BEGIN - Do not use in production
            addProperty(ContextServiceClientConstants.NO_MANAGEMENT_CONNECTOR, getNoManagementConnector());
            //TEST ONLY END - Do not use in production
        }};

        // Add Management connector state listener. It needs to be done before calling init on the connector
        CustomConnectorStateListener mgmtConnectorStateListener = addStateListenerToMgmtConnector(managementConnector);
        // Add CredentialsChangedListener to Management connector. It needs to be added before calling init on the connector
        CredentialsChangedListener credentialsChangedListener = addCustomCredentialsListenerToManagementConnector(managementConnector,contextServiceClient);
        managementConnector.init(connectionData, connInfo, configuration);
        // Now we can use the state listener to determine all the connector state changes
        try {
            waitForConnectorRegistered(mgmtConnectorStateListener, 3);
            LOGGER.info("Initialized Management connector");
        }catch(Exception e){
            LOGGER.error("Failed or timed out to initialize Management connector", e);
        }

        // Add Context Service Client connector state listener. It needs to be done before calling init on a connector
        CustomConnectorStateListener csConnectorStateListener = addStateListenerToContextConnector(contextServiceClient);

        // Init Context Service Client, reuse configuration we used for Management connector
        contextServiceClient.init(connectionData, connInfo, configuration);
        // Now we can use the state listener to determine all connector state changes
        try{
            waitForConnectorRegistered(csConnectorStateListener, 3);
            LOGGER.info("Initialized Context Service client");
        }catch(Exception e){
            LOGGER.error("Failed or timed out to initialize CS connector", e);
        }

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
        //TEST ONLY BEGIN - Do not use in production
        // This would only be done if you need to stop the connector - remove state listeners and destroy connectors
        contextServiceClient.removeStateListener(csConnectorStateListener);
        contextServiceClient.destroy();
        managementConnector.removeStateListener(mgmtConnectorStateListener);
        managementConnector.destroy();
        //TEST ONLY END - Do not use in production
    }

    public static boolean getNoManagementConnector() {
        String noManagementConnector = System.getenv(ContextServiceClientConstants.NO_MANAGEMENT_CONNECTOR);
        LOGGER.info("NO_MANAGEMENT_CONNECTOR: " + noManagementConnector);
        if (noManagementConnector==null) {
            noManagementConnector = "false";
        }
        return StringUtils.equalsIgnoreCase(noManagementConnector,"true");
    }

    /**
     * Create a Custom Connector State Listener to override the stateChanged behavior
     */
    public static class CustomConnectorStateListener implements ConnectorStateListener {
        public ConnectorState connectorState;

        public ConnectorState getConnectorState(){
            return connectorState;
        }

        @Override
        public void stateChanged(ConnectorState previousState, ConnectorState newState)
        {
            connectorState = newState;
            LOGGER.info("Connector state changed: " + newState);
            if (newState == ConnectorState.STOPPED) {
                // Perform optional cleanup tasks, etc ...
                LOGGER.info("Connector stopped.");
            }else if (newState == ConnectorState.REGISTERED) {
                // Perform any actions needed once connector is registered, etc ...
                LOGGER.info("Connector started.");
            } else if (newState == ConnectorState.UNREGISTERED) {
                // Perform any actions needed once connector is unregistered, etc ...
                LOGGER.info("Connector unregistered.");
            }
        }
    };

    /**
     * Before initializing the connector, create and add a ConnectorStateListener to the ContextServiceClient.
     * @param contextServiceClient
     * @return the created ConnectorStateListener
     */
    public static CustomConnectorStateListener addStateListenerToContextConnector(ContextServiceClient contextServiceClient){
        CustomConnectorStateListener stateListener = new CustomConnectorStateListener();
        contextServiceClient.addStateListener(stateListener);
        return stateListener;
    }

    /**
     * Before initializing the connector, create and add a ConnectorStateListener to the Management Connector.
     * @param mgmtConnector
     * @return the created ConnectorStateListener
     */
    public static CustomConnectorStateListener addStateListenerToMgmtConnector(ManagementConnector mgmtConnector){
        CustomConnectorStateListener stateListener = new CustomConnectorStateListener();
        mgmtConnector.addStateListener(stateListener);
        return stateListener;
    }

    /**
     * Wait timeoutSeconds for connector to be initialized, based on state listener callback
     * @param stateListener
     * @param timeoutSeconds
     */
    public static void waitForConnectorRegistered(CustomConnectorStateListener stateListener, int timeoutSeconds) throws Exception{
        long startTime = System.currentTimeMillis();
        while((System.currentTimeMillis() - startTime) <= timeoutSeconds*1000 &&
                ConnectorState.REGISTERED.equals(stateListener.getConnectorState())){
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        if(!ConnectorState.REGISTERED.equals(stateListener.getConnectorState())){
            throw new Exception("Timeout waiting for connector to register.");
        }
    }

    /**
     * Create and add a CredentialsChangedListener to the ManagementConnector.
     * It's recommended that you do this before initializing the connector.
     * @param managementConnector
     * @param contextServiceClient
     * @return the created CredentialsChangedListener
     */
    public static CredentialsChangedListener addCustomCredentialsListenerToManagementConnector(ManagementConnector managementConnector, final ContextServiceClient contextServiceClient){
        CredentialsChangedListener credentialsChangedListener = new CredentialsChangedListener() {
            String connectionData;

            @Override
            public void credentialsChanged(String newConnectionData) {
                LOGGER.info("ConnectionData changed: " + newConnectionData);
                connectionData = newConnectionData;
                // Connection data is not usually logged due to security considerations.

                String hostname = "doctest.example.com";
                ConnectorInfoImpl connInfo = new ConnectorInfoImpl(hostname);
                ConnectorConfiguration configuration = new ConnectorConfiguration() {{
                    addProperty("LAB_MODE", true); // exclude this line for production mode
                    addProperty("REQUEST_TIMEOUT", 10000);
                    //TEST ONLY BEGIN - Do not use in production
                    addProperty(ContextServiceClientConstants.NO_MANAGEMENT_CONNECTOR, getNoManagementConnector());
                    //TEST ONLY END - Do not use in production
                }};
                // Notify contextServiceClient that the connection data changed.
                contextServiceClient.updateAndReloadConfigAsync(connectionData, connInfo, configuration, null);
            }
        };
        managementConnector.addCredentialsChangedListener(credentialsChangedListener);
        return credentialsChangedListener;
    }
}
