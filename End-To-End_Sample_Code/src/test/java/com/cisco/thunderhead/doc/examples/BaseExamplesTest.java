package com.cisco.thunderhead.doc.examples;

import com.cisco.thunderhead.ContextObject;
import com.cisco.thunderhead.client.ClientResponse;
import com.cisco.thunderhead.client.ContextServiceClient;
import com.cisco.thunderhead.client.ContextServiceClientConstants;
import com.cisco.thunderhead.connector.ConnectorConfiguration;
import com.cisco.thunderhead.connector.ManagementConnector;
import com.cisco.thunderhead.connector.info.ConnectorInfoImpl;
import com.cisco.thunderhead.connector.states.ConnectorState;
import com.cisco.thunderhead.plugin.ConnectorFactory;
import com.cisco.thunderhead.rest.FlushStatusBean;
import org.apache.commons.lang3.StringUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URI;
import java.util.concurrent.TimeoutException;

import static com.cisco.thunderhead.doc.e2e.ContextServiceDemo.getNoManagementConnector;
import static org.junit.Assert.assertEquals;

public class BaseExamplesTest {
    protected static ContextServiceClient contextServiceClient;
    protected static ManagementConnector mgmtConnector;
    protected static ConfigurationAndInitialization.CustomCSConnectorStateListener connectorStateListener;
    protected static ConfigurationAndInitialization.CustomCSConnectorStateListener mgmtConnectorStateListener;

    private final static Logger LOGGER = LoggerFactory.getLogger(BaseExamplesTest.class);

    // Maximum time, in seconds, to wait for flush to complete before throwing an exception
    private final static int MAX_FLUSH_WAIT_IN_SECONDS = 60;

    @BeforeClass
    public static void initializeContextAndManagementConnector() throws Exception {
        ConnectorFactory.initializeFactory((new File(".")).getAbsolutePath() + "/connector.property");
        contextServiceClient = ConnectorFactory.getConnector(ContextServiceClient.class);
        LOGGER.info("ContextServiceClient version:  " + contextServiceClient.getVersion());

        ConnectorConfiguration config = new ConnectorConfiguration();
        config.addProperty("LAB_MODE", true);
        config.addProperty("REQUEST_TIMEOUT", 10000);
        config.addProperty(ContextServiceClientConstants.NO_MANAGEMENT_CONNECTOR, getNoManagementConnector());
        final String hostname = "doctest.example.com";
        ConnectorInfoImpl connInfo = new ConnectorInfoImpl(hostname);

        //Adding CS connector state listener. It needs to be done before calling init on a connector
        connectorStateListener = ConfigurationAndInitialization.addStateListenerToContextConnector(contextServiceClient);
        contextServiceClient.init(ConnectionData.getConnectionData(), connInfo, config);
        ConfigurationAndInitialization.waitForConnectorState(connectorStateListener, ConnectorState.REGISTERED, 3);

        mgmtConnector = ConnectorFactory.getConnector(ManagementConnector.class);
        mgmtConnectorStateListener = ConfigurationAndInitialization.addStateListenerToManagementConnector(mgmtConnector);
        mgmtConnector.init(ConnectionData.getConnectionData(), connInfo, config);
        ConfigurationAndInitialization.waitForConnectorState(mgmtConnectorStateListener, ConnectorState.REGISTERED, 3);

        // Flush Data, so we start with a clean slate...
        flushAllData();
    }

    @AfterClass
    public static void clean() throws Exception {
        flushAllData();
        destroyContextClient();
        destroyManagementConnector();
    }

    private static void flushAllData() throws Exception {
        contextServiceClient.flush(ContextObject.Types.POD);
        contextServiceClient.flush(ContextObject.Types.REQUEST);
        contextServiceClient.flush(ContextObject.Types.CUSTOMER);

        waitForFlushComplete(ContextObject.Types.POD);
        waitForFlushComplete(ContextObject.Types.REQUEST);
        waitForFlushComplete(ContextObject.Types.CUSTOMER);
    }

    /**
     * Use SDK to wait for flush to complete.  In this case, allow up to 30 seconds...
     * @param objectType
     * @throws TimeoutException
     */
    private static void waitForFlushComplete(String objectType) throws TimeoutException {
        FlushStatusBean status;
        status = contextServiceClient.waitForFlushComplete(objectType, MAX_FLUSH_WAIT_IN_SECONDS);
        if (!status.isCompleted()) {
            LOGGER.error("Flush did not complete within " + MAX_FLUSH_WAIT_IN_SECONDS + " seconds.");
            LOGGER.error("Flushed " + status.getNumberFlushed() + objectType + ".");
            throw new TimeoutException();
        }
    }

    private static String parseIdFromLocationUrl(URI location) {
        return StringUtils.substring(location.toString(), StringUtils.lastIndexOf(location.toString(), "/") + 1);
    }

    public static String validateClientResponseAndReturnId(ClientResponse response, int expectedStatus) {
        assertEquals(expectedStatus, response.getStatus());
        return parseIdFromLocationUrl(response.getLocation());
    }

    private static void destroyContextClient(){
        if(contextServiceClient==null)
            return;
        if (connectorStateListener != null ) {
            contextServiceClient.removeStateListener(connectorStateListener);
            connectorStateListener = null;
        }
        contextServiceClient.destroy();
    }

    private static void destroyManagementConnector(){
        if(mgmtConnector==null)
            return;
        if (mgmtConnectorStateListener != null ) {
            mgmtConnector.removeStateListener(mgmtConnectorStateListener);
            mgmtConnectorStateListener = null;
        }
        mgmtConnector.destroy();
    }


}
