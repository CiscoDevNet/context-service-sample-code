package com.cisco.thunderhead.doc.examples;

import com.cisco.thunderhead.client.ContextServiceClient;
import com.cisco.thunderhead.client.ContextServiceClientConstants;
import com.cisco.thunderhead.connector.Connector;
import com.cisco.thunderhead.connector.ConnectorConfiguration;
import com.cisco.thunderhead.connector.ManagementConnector;
import com.cisco.thunderhead.connector.info.ConnectorInfo;
import com.cisco.thunderhead.connector.info.ConnectorInfoImpl;
import com.cisco.thunderhead.connector.pwreset.CredentialsChangedListener;
import com.cisco.thunderhead.connector.states.ConnectorState;
import com.cisco.thunderhead.connector.states.ConnectorStateListener;
import com.cisco.thunderhead.errors.ApiErrorType;
import com.cisco.thunderhead.errors.ApiException;
import com.cisco.thunderhead.plugin.ConnectorFactory;
import com.cisco.thunderhead.util.ReloadListenerWithWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Paths;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.cisco.thunderhead.doc.e2e.ContextServiceDemo.getNoManagementConnector;

public class ConfigurationAndInitialization {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigurationAndInitialization.class);

    // state listeners for Context Service Client and Management connectors
    private static CustomConnectorStateListener connectorStateListener;
    private static ConnectorStateListener mgmtConnectorStateListener;

    public static class CustomConnectorStateListener implements ConnectorStateListener {
        protected ConnectorState connectorState;

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
     * Before you initialize the connectors, initialize ConnectorFactory.
     */
    public static void initializeConnectorFactory() {
        String pathToConnectorProperty = Paths.get("./connector.property").toAbsolutePath().toString();
        ConnectorFactory.disableBootstrapUpgradeCheck();
        ConnectorFactory.initializeFactory(pathToConnectorProperty);
    }

    /**
     * Create and initialize the ContextServiceClient with custom configuration.
     * ConnectorFactory should already be initialized.
     * @param connectionData
     * @return an initialized ContextServiceClient
     */
    public static ContextServiceClient createAndInitContextServiceClientWithCustomConfiguration(String connectionData) {
        ContextServiceClient contextServiceClient = ConnectorFactory.getConnector(ContextServiceClient.class);

        // Add Context Service Client connector state listener. It needs to be done before calling init on the connector
        connectorStateListener = addStateListenerToContextConnector(contextServiceClient);

        String hostname = "doctest.example.com";
        ConnectorInfoImpl connInfo = new ConnectorInfoImpl(hostname);
        ConnectorConfiguration configuration = new ConnectorConfiguration(){{
            addProperty("LAB_MODE", true); // exclude this line for prod mode
            addProperty("REQUEST_TIMEOUT", 10000);
            //TEST ONLY BEGIN - Do not use in production
            addProperty(ContextServiceClientConstants.NO_MANAGEMENT_CONNECTOR, getNoManagementConnector());
            //TEST ONLY END - Do not use in production
        }};
        contextServiceClient.init(connectionData, connInfo, configuration);

        // Wait 3 sec for connector to be initialized.
        try {
            waitForConnectorState(connectorStateListener, ConnectorState.REGISTERED, 3);
            LOGGER.info(">>>> CS Connector initialized successfully");
        }catch(Exception e){
            LOGGER.error(">>>> CS Connector FAILED to initialize successfully", e);
        }

        // Optionally, parse the JSON returned by getStatus for additional status information
        String status = contextServiceClient.getStatus();

        return contextServiceClient;
    }

    /**
     * Create and initialize the ManagementConnector with custom configuration.
     * ConnectorFactory should already be initialized.
     * @param connectionData
     * @return an initialized ManagementConnector
     */
    public static ManagementConnector createAndInitManagementConnectorWithCustomConfiguration(String connectionData){
        AtomicBoolean isRegistered = new AtomicBoolean(false);

        ManagementConnector managementConnector = ConnectorFactory.getConnector(ManagementConnector.class);
        // Add Management connector state listener. It needs to be done before calling init on the connector
        mgmtConnectorStateListener = addStateListenerToManagementConnector(managementConnector, isRegistered);

        String hostname = "doctest.example.com";
        ConnectorInfoImpl connInfo = new ConnectorInfoImpl(hostname);
        ConnectorConfiguration configuration = new ConnectorConfiguration(){{
            addProperty("LAB_MODE", true); // exclude this line for production mode
            addProperty("REQUEST_TIMEOUT", 10000);
            //TEST ONLY BEGIN - Do not use in production
            addProperty(ContextServiceClientConstants.NO_MANAGEMENT_CONNECTOR, getNoManagementConnector());
            //TEST ONLY END - Do not use in production
        }};
        managementConnector.init(connectionData, connInfo, configuration);

        // Optionally, parse the JSON returned by getStatus for additional status information
        String status = managementConnector.getStatus();
        // Connector could be already registered in Before Class, so check if it is already registered
        if(! status.contains("REGISTERED")) {
            try {
                waitForConnectorToRegister(isRegistered, 3);
                LOGGER.info(">>>> Management Connector initialized successfully");
            } catch (Exception e) {
                LOGGER.error(">>>> Management Connector FAILED to initialize successfully", e);
            }
        }

        return managementConnector;
    }

    /**
     * Create and add a CredentialsChangedListener to the ManagementConnector.
     * It's recommended that you do this before initializing the connector.
     * @param managementConnector
     * @return the created CredentialsChangedListener
     */
    public static CredentialsChangedListener addCredentialsListenerToManagementConnector(ManagementConnector managementConnector, final ContextServiceClient contextServiceClient){
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
                }};
                // Notify contextServiceClient that the connection data changed.
                contextServiceClient.updateAndReloadConfigAsync(connectionData, connInfo, configuration, null);
            }
        };
        managementConnector.addCredentialsChangedListener(credentialsChangedListener);
        return credentialsChangedListener;
    }

    /**
     * Before initializing the connector, create and add a ConnectorStateListener to the ManagementConnector.
     * @param managementConnector
     * @param isRegistered
     * @return the created ConnectorStateListener
     */
    public static ConnectorStateListener addStateListenerToManagementConnector(ManagementConnector managementConnector, final AtomicBoolean isRegistered){
        ConnectorStateListener stateListener = new ConnectorStateListener() {
            public ConnectorState connectorState;

            @Override
            public void stateChanged(ConnectorState previousState, ConnectorState newState)
            {
                connectorState = newState;
                LOGGER.info("Management Connector state changed: " + newState);
                if (newState == ConnectorState.STOPPED) {
                    // Perform optional cleanup tasks; update state related application flags
                    if(null!= isRegistered)
                        isRegistered.set(false);

                    LOGGER.info("Management Connector stopped.");
                }else if (newState == ConnectorState.REGISTERED) {
                    // Perform any actions needed once connector is registered; update state related application flags
                    if(null!= isRegistered)
                        isRegistered.set(true);

                    LOGGER.info("Management Connector registered.");
                }
            }
        };
        managementConnector.addStateListener(stateListener);
        return stateListener;
    }

    /**
     * Before initializing the connector, create and add a ConnectorStateListener for the ContextServiceClient.
     * @param contextServiceClient
     * @return the created ConnectorStateListener
     */
    public static CustomConnectorStateListener addStateListenerToContextConnector(ContextServiceClient contextServiceClient){
        CustomConnectorStateListener stateListener = new CustomConnectorStateListener();
        contextServiceClient.addStateListener(stateListener);
        return stateListener;
    }

    /**
     * Before initializing the connector, create and add a ConnectorStateListener for the Management Connector.
     * @param mgmtConnector
     * @return the created ConnectorStateListener
     */
    public static CustomConnectorStateListener addStateListenerToManagementConnector(ManagementConnector mgmtConnector){
        CustomConnectorStateListener stateListener = new CustomConnectorStateListener();
        mgmtConnector.addStateListener(stateListener);
        return stateListener;
    }

    /**
     * Create and add a ReloadListener to a Connector to update and reload.
     * @param connector the connector to relaod
     * @param connectionData your connectionData string
     * @param connectorInfo a ConnectorInfo object
     * @param connectorConfiguration a ConnectorConfiguration object
     * @return the new reload listener, after a reload has completed
     */
    public static ReloadListenerWithWait updateAndReloadConnector(Connector connector, String connectionData, ConnectorInfo connectorInfo, ConnectorConfiguration connectorConfiguration){
        ReloadListenerWithWait reloadListener = new ReloadListenerWithWait();
        connector.updateAndReloadConfigAsync(connectionData, connectorInfo, connectorConfiguration, reloadListener);

        try {
            reloadListener.waitForCompletion(60000);
        } catch (ApiException apiException) {
            if (apiException.getError().getErrorType().equals(ApiErrorType.TIMEOUT_REQUEST)) {
                LOGGER.error("Reload timed out!");
            } else {
                LOGGER.error("Error reloading connector: " + apiException.toString());
            }
            throw apiException;
        }

        return reloadListener;
    }

    /**
     * Wait for the connector state to be REGISTERED or throw an exception if timeoutSec value is reached.
     *
     * This example shows how to wait for the REGISTERED state by checking a connector state application flag
     * which is updated in the stateListener.
     * @param isRegistered
     * @param timeoutSec
     * @throws Exception
     */
    public static void waitForConnectorToRegister(AtomicBoolean isRegistered, int timeoutSec) throws Exception{
        long startTime = System.currentTimeMillis();
        while((System.currentTimeMillis() - startTime) <= timeoutSec*1000 &&
                !isRegistered.get()){
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        if(!isRegistered.get()){
            throw new Exception("Timed out waiting for connector to register.");
        }
    }

    /**
     * Wait timeoutSec for connector to reach a specified state.
     *
     * This example shows a different technique using CustomConnectorStateListener.getConnectorState() method to determine
     * connector state changes.
     * @param stateListener
     * @param expectedState
     * @param timeoutSec
     * @throws Exception
     */
    public static void waitForConnectorState(CustomConnectorStateListener stateListener, ConnectorState expectedState, int timeoutSec) throws Exception{
        long startTime = System.currentTimeMillis();
        while((System.currentTimeMillis() - startTime) <= timeoutSec*1000 &&
                expectedState.equals(stateListener.getConnectorState())){
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        if(!expectedState.equals(stateListener.getConnectorState())){
            throw new Exception("Timed out waiting for connector to reach "+ expectedState.name()+"; Current state is :" + stateListener.getConnectorState());
        }
    }

    /**
     * Remove state listener, if any were set, from ContextService connector
     * @param contextServiceClient
     */
    public static void beforeDestroyCSConnector(ContextServiceClient contextServiceClient){
        if(contextServiceClient != null && connectorStateListener !=null)
            contextServiceClient.removeStateListener(connectorStateListener);
    }

    /**
     * Remove state listener, if any were set, from Management connector
     * @param managementConnector
     */
    public static void beforeDestroyMgmtConnector(ManagementConnector managementConnector){
        if(managementConnector != null && mgmtConnectorStateListener != null)
            managementConnector.removeStateListener(mgmtConnectorStateListener);
    }

    /**
     * Destroy ContextService connector
     * @param contextServiceClient
     */
    public static void destroyCSConnector(ContextServiceClient contextServiceClient){
        beforeDestroyCSConnector(contextServiceClient);
        if(contextServiceClient!=null)
            contextServiceClient.destroy();
    }

    /**
     * Destroy Management connector
     * @param managementConnector
     */
    public static void destroyMgmtConnector(ManagementConnector managementConnector){
        beforeDestroyMgmtConnector(managementConnector);
        if(managementConnector!=null)
            managementConnector.destroy();
    }


}
