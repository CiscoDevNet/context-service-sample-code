package com.cisco.thunderhead.doc.examples;


import com.cisco.thunderhead.client.ContextServiceClient;
import com.cisco.thunderhead.client.ContextServiceClientConstants;
import com.cisco.thunderhead.connector.Connector;
import com.cisco.thunderhead.connector.ConnectorConfiguration;
import com.cisco.thunderhead.connector.info.ConnectorInfo;
import com.cisco.thunderhead.connector.info.ConnectorInfoImpl;
import com.cisco.thunderhead.connector.updates.ReloadListener;
import com.cisco.thunderhead.plugin.ConnectorFactory;

import static com.cisco.thunderhead.doc.e2e.ContextServiceDemo.getNoManagementConnector;

public class Proxy {

    /**
     * Configure ContextServiceClient to use an http proxy
     * @param connectionData
     * @return an initialized ContextServiceClient that uses a configured
     */
    public static ContextServiceClient createAndInitContextServiceClientWithProxy(String connectionData) {
        // set the proxy
        System.setProperty("contextservice.proxyURL", "http://proxy.esl.cisco.com:80");
        // initialize the client
        ContextServiceClient contextServiceClient = ConnectorFactory.getConnector(ContextServiceClient.class);
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
        return contextServiceClient;
    }

    /**
     * Configure ContextService to use a new proxy, and reload
     * @param connector the connector to relaod
     * @param connectionData your connectionData string
     * @param connectorInfo a ConnectorInfo object
     * @param connectorConfiguration a ConnectorConfiguration object
     * @param reloadListener a reload listener that is notified when reload is complete.
     */
    public static void reloadConnectorWithProxy(Connector connector, String connectionData, ConnectorInfo connectorInfo, ConnectorConfiguration connectorConfiguration, ReloadListener reloadListener){
        // configure the proxy
        System.setProperty("contextservice.proxyURL", "http://proxy.esl.cisco.com:80");
        // reload the config. If you wish to wait for the reload to complete, pass a ReloadListener.
        connector.updateAndReloadConfigAsync(connectionData, connectorInfo, connectorConfiguration, reloadListener);
    }

    /**
     * Configure ContextService to use a new proxy, and reload
     * @param connector the connector to relaod
     * @param connectionData your connectionData string
     * @param connectorInfo a ConnectorInfo object
     * @param connectorConfiguration a ConnectorConfiguration object
     * @param reloadListener a reload listener that is notified when reload is complete.
     */
    public static void reloadConnectorWithNoProxy(Connector connector, String connectionData, ConnectorInfo connectorInfo, ConnectorConfiguration connectorConfiguration, ReloadListener reloadListener){
        // removing the system property
        System.clearProperty("contextservice.proxyURL");
        // reload the config. If you wish to wait for the reload to complete, pass a ReloadListener.
        connector.updateAndReloadConfigAsync(connectionData, connectorInfo, connectorConfiguration, reloadListener);
    }
}
