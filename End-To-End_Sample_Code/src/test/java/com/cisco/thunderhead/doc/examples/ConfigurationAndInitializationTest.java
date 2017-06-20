package com.cisco.thunderhead.doc.examples;

import com.cisco.thunderhead.client.ContextServiceClient;
import com.cisco.thunderhead.connector.ConnectorConfiguration;
import com.cisco.thunderhead.connector.ManagementConnector;
import com.cisco.thunderhead.connector.info.ConnectorInfoImpl;
import com.cisco.thunderhead.connector.pwreset.CredentialsChangedListener;
import com.cisco.thunderhead.connector.states.ConnectorStateListener;
import com.cisco.thunderhead.plugin.ConnectorFactory;
import com.cisco.thunderhead.util.ReloadListenerWithWait;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.TimeoutException;

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

        //cleanup
        ConfigurationAndInitialization.destroyCSConnector(contextServiceClient);
    }

    @Test
    public void createAndInitManagementConnectorWithCustomConfigurationTest() {
        ManagementConnector managementConnector = ConfigurationAndInitialization.createAndInitManagementConnectorWithCustomConfiguration(connectionData);
        //TODO: re-enable assert below. this commented out -- for testing locally only
        //assertEquals("https://admin.ciscospark.com", managementConnector.getManagementURL());
        assertTrue(managementConnector.getStatus().contains("REGISTERED"));

        //cleanup
        ConfigurationAndInitialization.destroyMgmtConnector(managementConnector);
    }

    @Test
    public void addCredentialsListenerToManagementConnectorTest() {
        ContextServiceClient contextServiceClient = ConfigurationAndInitialization.createAndInitContextServiceClientWithCustomConfiguration(connectionData);
        ManagementConnector managementConnector = ConfigurationAndInitialization.createAndInitManagementConnectorWithCustomConfiguration(connectionData);
        CredentialsChangedListener credentialsChangedListener = ConfigurationAndInitialization.addCredentialsListenerToManagementConnector(managementConnector, contextServiceClient);
        assertNotNull(credentialsChangedListener);

        //cleanup
        managementConnector.removeCredentialsChangedListener(credentialsChangedListener);
        ConfigurationAndInitialization.destroyCSConnector(contextServiceClient);
        ConfigurationAndInitialization.destroyMgmtConnector(managementConnector);
    }

    @Test
    public void addStateListenerToContextConnectorTest() {
        ContextServiceClient contextServiceClient = ConfigurationAndInitialization.createAndInitContextServiceClientWithCustomConfiguration(connectionData);
        ConnectorStateListener connectorStateListener = ConfigurationAndInitialization.addStateListenerToContextConnector(contextServiceClient);
        assertNotNull(connectorStateListener);

        //cleanup
        contextServiceClient.removeStateListener(connectorStateListener);
        ConfigurationAndInitialization.destroyCSConnector(contextServiceClient);
    }

    @Test
    public void addStateListenerToManagementConnectorTest() {
        ManagementConnector managementConnector = ConnectorFactory.getConnector(ManagementConnector.class);
        ConnectorStateListener connectorStateListener = ConfigurationAndInitialization.addStateListenerToManagementConnector(managementConnector, null);
        ConfigurationAndInitialization.createAndInitManagementConnectorWithCustomConfiguration(connectionData);
        assertNotNull(connectorStateListener);

        //cleanup
        managementConnector.removeStateListener(connectorStateListener);
        ConfigurationAndInitialization.destroyMgmtConnector(managementConnector);
    }

    @Test
    public void updateAndReloadConnectorTest() {
        ContextServiceClient contextServiceClient = ConfigurationAndInitialization.createAndInitContextServiceClientWithCustomConfiguration(connectionData);
        ConnectorInfoImpl connectorInfo = new ConnectorInfoImpl("doctest.example.com");
        ReloadListenerWithWait reloadListener = ConfigurationAndInitialization.updateAndReloadConnector(contextServiceClient, connectionData, connectorInfo, configuration);
        assertTrue("Reload listener should have completed", reloadListener.isDone());

        //cleanup
        ConfigurationAndInitialization.destroyCSConnector(contextServiceClient);
    }

    private void exerciseConnector(ContextServiceClient contextServiceClient) throws TimeoutException, InterruptedException {
        // check its that its functionality is present
        CreateEntities.createPodWithBaseFieldset(contextServiceClient);
        // clean up
        FlushEntities.flushPods(contextServiceClient);
    }
}
