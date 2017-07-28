package com.cisco.thunderhead.management;

import com.cisco.thunderhead.connector.ManagementConnector;
import com.cisco.thunderhead.connector.RegisteringApplication;
import com.cisco.thunderhead.connector.pwreset.CredentialsChangedListener;
import com.cisco.thunderhead.plugin.ConnectorFactory;
import com.cisco.thunderhead.rest.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.io.File;

/**
 * This handles setting up the Management Connector so it can listen for
 * credentials changed events and update the file that stores connection
 * data when that happens.
 */
public class ManagementContextListener implements ServletContextListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(RegistrationService.class);

    private static File connectorPropertyFile;
    private static ManagementConnector managementConnector;
    private static RegisteringApplication registerApp;

    /**
     * Invoked during web application startup.
     */
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        connectorPropertyFile = Utils.initializeConnectorPropertyFile(sce.getServletContext());
        ConnectorFactory.initializeFactory(connectorPropertyFile.getAbsolutePath());
        registerApp = ConnectorFactory.getConnector(RegisteringApplication.class);

        String connectionData = Utils.getConnectionData();
        if (connectionData!=null) {
            initManagementConnector(connectionData);
        } else {
            LOGGER.info("Connection data not yet configured");
        }
    }

    /**
     * This initializes the management connector given the connection data.
     */
    public static void initManagementConnector(String connectionData) {
        LOGGER.info("Initializing management connector");
        managementConnector = Utils.getInitializedManagementConnector(connectorPropertyFile, connectionData);
        managementConnector.addCredentialsChangedListener(new CSCredentialsChangedListener());
    }

    /**
     * Invoked during web application shutdown.
     */
    @Override
    public void contextDestroyed(ServletContextEvent sce) {

    }

    public static ManagementConnector getManagementConnector() {
        return managementConnector;
    }

    public static RegisteringApplication getRegisterApp() {
        return registerApp;
    }

    private static class CSCredentialsChangedListener implements CredentialsChangedListener {
        @Override
        public void credentialsChanged(String connectionData) {
            LOGGER.info("Credentials changed; saving latest connection data");

            // Write the latest connection data to a file.
            Utils.saveConnectionData(connectionData);
        }
    }
}
