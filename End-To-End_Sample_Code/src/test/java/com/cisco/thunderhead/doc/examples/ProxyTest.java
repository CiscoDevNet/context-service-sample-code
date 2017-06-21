package com.cisco.thunderhead.doc.examples;

import com.cisco.thunderhead.client.ContextServiceClient;
import com.cisco.thunderhead.connector.ConnectorConfiguration;
import com.cisco.thunderhead.connector.info.ConnectorInfoImpl;
import com.cisco.thunderhead.util.ReloadListenerWithWait;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Test;

import java.util.concurrent.TimeoutException;

public class ProxyTest {
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

    @After
    public void clearProxy() {
        System.clearProperty("contextservice.proxyURL");
    }

    @Test
    public void createAndInitContextServiceClientWithProxyTest() throws TimeoutException, InterruptedException {
        ContextServiceClient contextServiceClient = Proxy.createAndInitContextServiceClientWithProxy(connectionData);
        exerciseConnector(contextServiceClient);

        //cleanup
        ConfigurationAndInitialization.destroyCSConnector(contextServiceClient);
    }

    @Test
    public void reloadConnectorWithProxyTest() throws TimeoutException, InterruptedException {
        // initialize connector
        ContextServiceClient contextServiceClient = ConfigurationAndInitialization.createAndInitContextServiceClientWithCustomConfiguration(connectionData);
        // reload connector with proxy
        ReloadListenerWithWait reloadListener = new ReloadListenerWithWait();
        Proxy.reloadConnectorWithProxy(contextServiceClient, connectionData, connectorInfo, configuration, reloadListener);
        reloadListener.waitForCompletion(60000);
        // make sure client still works
        exerciseConnector(contextServiceClient);

        //cleanup
        ConfigurationAndInitialization.destroyCSConnector(contextServiceClient);
    }

    @Test
    public void reloadConnectorWithNoProxyTest() throws TimeoutException, InterruptedException {
        // initialize connector
        ContextServiceClient contextServiceClient = Proxy.createAndInitContextServiceClientWithProxy(connectionData);
        // reload connector with proxy
        ReloadListenerWithWait reloadListener = new ReloadListenerWithWait();
        Proxy.reloadConnectorWithNoProxy(contextServiceClient, connectionData, connectorInfo, configuration, reloadListener);
        reloadListener.waitForCompletion(60000);
        // make sure client still works
        exerciseConnector(contextServiceClient);

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
