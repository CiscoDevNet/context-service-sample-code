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

import static com.cisco.thunderhead.doc.e2e.ContextServiceDemo.getNoManagementConnector;

public class ConfigurationAndInitialization {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigurationAndInitialization.class);
    /**
     * Before you initialize the connectors, initialize ConnectorFactory.
     */
    public static void initializeConnectorFactory() {
        String pathToConnectorProperty = Paths.get("./connector.property").toAbsolutePath().toString();
        ConnectorFactory.disableBootstrapUpgradeCheck();
        ConnectorFactory.initializeFactory(pathToConnectorProperty);
    }

    /**
     * Create and initialize the ContextServiceClient with customer configuration.
     * ConnectorFactory should already be initialized.
     * @param connectionData
     * @return an initialized ContextServiceClient
     */
    public static ContextServiceClient createAndInitContextServiceClientWithCustomConfiguration(String connectionData) {
        ContextServiceClient contextServiceClient = ConnectorFactory.getConnector(ContextServiceClient.class);
        String hostname = "doctest.example.com";
        ConnectorInfoImpl connInfo = new ConnectorInfoImpl(hostname);
        ConnectorConfiguration configuration = new ConnectorConfiguration(){{
            addProperty("LAB_MODE", true); // exclude this line for prod mode
            addProperty("REQUEST_TIMEOUT", 10000);
            //----Test Only Begin - Not needed for production implementation ----
            addProperty(ContextServiceClientConstants.NO_MANAGEMENT_CONNECTOR, getNoManagementConnector());
            //----Test Only End - Not needed for production implementation ----
        }};
        contextServiceClient.init(connectionData, connInfo, configuration);

        // Optionally, parse the JSON returned by getStatus for additional status information
        String status = contextServiceClient.getStatus();

        return contextServiceClient;
    }

    /**
     * Create and initialize the ManagementConnector.
     * ConnectorFactory should already be initialized.
     * @param connectionData
     * @return an initialized ManagementConnector
     */
    public static ManagementConnector createAndInitManagementConnectorWithCustomConfiguration(String connectionData){
        ManagementConnector managementConnector = ConnectorFactory.getConnector(ManagementConnector.class);
        String hostname = "doctest.example.com";
        ConnectorInfoImpl connInfo = new ConnectorInfoImpl(hostname);
        ConnectorConfiguration configuration = new ConnectorConfiguration(){{
            addProperty("LAB_MODE", true); // exclude this line for prod mode
            addProperty("REQUEST_TIMEOUT", 10000);
            //----Test Only Begin - Not needed for production implementation ----
            addProperty(ContextServiceClientConstants.NO_MANAGEMENT_CONNECTOR, getNoManagementConnector());
            //----Test Only End - Not needed for production implementation ----
        }};
        managementConnector.init(connectionData, connInfo, configuration);

        // Optionally, parse the JSON returned by getStatus for additional status information
        String status = managementConnector.getStatus();

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
                    addProperty("LAB_MODE", true); // exclude this line for prod mode
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
     * @return the created ConnectorStateListener
     */
    public static ConnectorStateListener addStateListenerToManagementConnector(ManagementConnector managementConnector){
        ConnectorStateListener stateListener = new ConnectorStateListener() {
            public ConnectorState connectorState;

            @Override
            public void stateChanged(ConnectorState previousState, ConnectorState newState)
            {
                connectorState = newState;
                LOGGER.info("Management Connector state changed: " + newState);
                if (newState == ConnectorState.STOPPED) {
                    // Perform optional cleanup tasks
                    LOGGER.info("Management Connector stopped.");

                }
            }
        };
        managementConnector.addStateListener(stateListener);
        return stateListener;
    }

    /**
     * Before initializing the connector, create and add a ConnectorStateListener to the ContextServiceClient.
     * @param contextServiceClient
     * @return the created ConnectorStateListener
     */
    public static ConnectorStateListener addStateListenerToContextConnector(ContextServiceClient contextServiceClient){
        ConnectorStateListener stateListener = new ConnectorStateListener() {
            public ConnectorState connectorState;

            @Override
            public void stateChanged(ConnectorState previousState, ConnectorState newState)
            {
                connectorState = newState;
                LOGGER.info("Context Service Client state changed: " + newState);
                if (newState == ConnectorState.STOPPED) {
                    // Perform optional cleanup tasks
                    LOGGER.info("Context Service Client stopped.");

                }
            }
        };
        contextServiceClient.addStateListener(stateListener);
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
}
