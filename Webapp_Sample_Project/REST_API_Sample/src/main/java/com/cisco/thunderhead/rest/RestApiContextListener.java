package com.cisco.thunderhead.rest;

import com.cisco.thunderhead.client.ContextServiceClient;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.io.File;

/**
 * Initializes the application.  Invoked during startup/shutdown.
 */
public class RestApiContextListener implements ServletContextListener {
    private static ContextServiceClient contextServiceClient;

    /**
     * Invoked during web application startup.
     */
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        File connectorPropertyFile = Utils.initializeConnectorPropertyFile(sce.getServletContext());

        String connectionData = Utils.getConnectionData();
        contextServiceClient = Utils.getInitializedContextServiceClient(connectorPropertyFile, connectionData);
    }

    /**
     * Invoked during web application shutdown.
     */
    @Override
    public void contextDestroyed(ServletContextEvent sce) {

    }

    public static ContextServiceClient getContextServiceClient() {
        return contextServiceClient;
    }
}
