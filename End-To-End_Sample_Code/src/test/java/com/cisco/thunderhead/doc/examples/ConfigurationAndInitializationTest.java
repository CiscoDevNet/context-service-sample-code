package com.cisco.thunderhead.doc.examples;

import com.cisco.thunderhead.client.ContextServiceClient;
import com.cisco.thunderhead.connector.ConnectorConfiguration;
import com.cisco.thunderhead.connector.ManagementConnector;
import com.cisco.thunderhead.connector.info.ConnectorInfoImpl;
import com.cisco.thunderhead.connector.pwreset.CredentialsChangedListener;
import com.cisco.thunderhead.connector.states.ConnectorStateListener;
import com.cisco.thunderhead.util.ReloadListenerWithWait;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.TimeoutException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class ConfigurationAndInitializationTest {
    private String connectionData = ConnectionData.getConnectionData();

    ConnectorInfoImpl connectorInfo = new ConnectorInfoImpl("doctest.example.com");
    ConnectorConfiguration configuration = new ConnectorConfiguration(){{
        addProperty("LAB_MODE", true); // exclude this line for prod mode
        addProperty("REQUEST_TIMEOUT", 10000);
    }};


    @AfterClass
    public static void flushAll() throws Exception {
        BaseExamplesTest.initializeContextAndManagementConnector();
        BaseExamplesTest.clean();
    }

    @Before
    public void initializeFactory() {
        ConfigurationAndInitialization.initializeConnectorFactory();
    }

    @Test
    public void createAndInitContextServiceClientWithCustomConfigurationTest() throws TimeoutException, InterruptedException {
        ContextServiceClient contextServiceClient = ConfigurationAndInitialization.createAndInitContextServiceClientWithCustomConfiguration(connectionData);
        exerciseConnector(contextServiceClient); // make sure connector was initialized and works
    }

    @Test
    public void createAndInitManagementConnectorWithCustomConfigurationTest() {
        ManagementConnector managementConnector = ConfigurationAndInitialization.createAndInitManagementConnectorWithCustomConfiguration(connectionData);
        assertEquals("https://admin.ciscospark.com", managementConnector.getManagementURL());
    }

    @Test
    public void addCredentialsListenerToManagementConnectorTest() {
        ManagementConnector managementConnector = ConfigurationAndInitialization.createAndInitManagementConnectorWithCustomConfiguration(connectionData);
        ContextServiceClient contextServiceClient = ConfigurationAndInitialization.createAndInitContextServiceClientWithCustomConfiguration(connectionData);
        CredentialsChangedListener credentialsChangedListener = ConfigurationAndInitialization.addCredentialsListenerToManagementConnector(managementConnector, contextServiceClient);
        assertNotNull(credentialsChangedListener);
    }

    @Test
    public void addStateListenerToContextConnectorTest() {
        ContextServiceClient contextServiceClient = ConfigurationAndInitialization.createAndInitContextServiceClientWithCustomConfiguration(connectionData);
        ConnectorStateListener connectorStateListener = ConfigurationAndInitialization.addStateListenerToContextConnector(contextServiceClient);
        assertNotNull(connectorStateListener);
    }

    @Test
    public void addStateListenerToManagementConnectorTest() {
        ManagementConnector managementConnector = ConfigurationAndInitialization.createAndInitManagementConnectorWithCustomConfiguration(connectionData);
        ConnectorStateListener connectorStateListener = ConfigurationAndInitialization.addStateListenerToManagementConnector(managementConnector, null, null);
        assertNotNull(connectorStateListener);
    }

    @Test
    public void updateAndReloadConnectorTest() {
        ContextServiceClient contextServiceClient = ConfigurationAndInitialization.createAndInitContextServiceClientWithCustomConfiguration(connectionData);
        ConnectorInfoImpl connectorInfo = new ConnectorInfoImpl("doctest.example.com");
        ReloadListenerWithWait reloadListener = ConfigurationAndInitialization.updateAndReloadConnector(contextServiceClient, connectionData, connectorInfo, configuration);
        assertTrue("Reload listener should have completed", reloadListener.isDone());
    }

    private void exerciseConnector(ContextServiceClient contextServiceClient) throws TimeoutException, InterruptedException {
        // check its that its functionality is present
        CreateEntities.createPodWithBaseFieldset(contextServiceClient);
        // clean up
        FlushEntities.flushPods(contextServiceClient);
    }
}
