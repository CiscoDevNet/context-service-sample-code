package com.cisco.thunderhead.example.ui;

import com.cisco.thunderhead.BaseDbBean;
import com.cisco.thunderhead.ContextObject;
import com.cisco.thunderhead.client.ContextServiceClient;
import com.cisco.thunderhead.client.ContextServiceClientConstants;
import com.cisco.thunderhead.client.Operation;
import com.cisco.thunderhead.client.SearchParameters;
import com.cisco.thunderhead.connector.ConnectorConfiguration;
import com.cisco.thunderhead.connector.ManagementConnector;
import com.cisco.thunderhead.connector.info.ConnectorInfoImpl;
import com.cisco.thunderhead.connector.states.ConnectorStateListener;
import com.cisco.thunderhead.plugin.ConnectorFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Paths;
import java.util.*;
import java.util.function.Function;

/**
 * Various utilities.
 */
public class Utils {
    private static final Logger LOGGER = LoggerFactory.getLogger(Utils.class);
    private static final int MAX_RESULTS = 25;

    public static ContextServiceClient getInitializedContextServiceClient(String jsonConnectionData, ConnectorStateListener listener) {
        // initialize connector factory
        String pathToConnectorProperty = Paths.get("./connector.property").toAbsolutePath().toString();
        ConnectorFactory.initializeFactory(pathToConnectorProperty);
        LOGGER.info("Initialized Connector Factory");

        boolean labMode = true;
        int requestTimeOut = 10000;
        boolean noFms = true;

        // initialize management connector
        ManagementConnector managementConnector = ConnectorFactory.getConnector(ManagementConnector.class);
        if (listener!=null) {
            managementConnector.addStateListener(listener);
        }
        String hostname = "doctest.example.com";
        ConnectorInfoImpl connInfo = new ConnectorInfoImpl(hostname);
        ConnectorConfiguration configuration = new ConnectorConfiguration(){{
            addProperty(ContextServiceClientConstants.LAB_MODE, labMode); // exclude this line for prod mode
            addProperty(ContextServiceClientConstants.REQUEST_TIMEOUT, requestTimeOut);
//            addProperty(ContextServiceClientConstants.NO_MANAGEMENT_CONNECTOR, noFms);
        }};
        managementConnector.init(jsonConnectionData, connInfo, configuration);
        LOGGER.info("Initialized management connector");

        // initialize context service client
        ContextServiceClient contextServiceClient = ConnectorFactory.getConnector(ContextServiceClient.class);
        if (listener!=null) {
            contextServiceClient.addStateListener(listener);
        }
        // reuse configuration we used for management connector
        contextServiceClient.init(jsonConnectionData, connInfo, configuration);
        LOGGER.info("Initialized Context Service client");
        return contextServiceClient;
    }

    public static void runIt(String connectionData, RunIt runIt) {
        String configFilePath = "connector.property";
        try {
            ConnectorFactory.initializeFactory(configFilePath);
        }
        catch(Exception e) {
            System.out.println("Error Initializing Factory! The Error is: " + e);
        }

        // Initialize Client Connection
        ContextServiceClient contextServiceClient = Utils.getInitializedContextServiceClient(connectionData, null);

        // Do some things with Context Service...
        runIt.runIt(contextServiceClient);

        // Close Connections
        contextServiceClient.destroy();
        System.out.println("\n\n*** Finished ***\n\n");
    }

    /**
     * Wait for an object to become searchable (after created). Used for Field or Fieldset (or any other
     * non-<code>ContextObject</code> object types.
     */
    static <T extends BaseDbBean> void waitForSearchable(ContextServiceClient contextServiceClient, Collection<String> fields, Class<T> clazz) {
        java.util.List result;
        do {
            result = search(contextServiceClient, fields, clazz);
            LOGGER.info("waitForSearchable expected: " + fields.size() + ", actual: " + result.size());
        } while (result.size()!=fields.size());
    }

    /**
     * Wait for an object to become searchable (after created). Used for <code>ContextObject</code> object types.
     */
    static void waitForSearchable(ContextServiceClient contextServiceClient, Collection<String> fields, String type) {
        java.util.List result;
        do {
            result = search(contextServiceClient, fields, type);
            LOGGER.info("waitForSearchable expected: " + fields.size() + ", actual: " + result.size());
        } while (result.size()!=fields.size());
    }

    /**
     * This returns all the beans represented by the ids.  This is used when searching for Context beans that <em>are not</em>
     * <code>ContextObject</code>s, such as Field and Fieldset objects.
     *
     * Search has a max limit on the result set so this
     * does multiple search requests to return all the beans requested.
     */
    static <T extends BaseDbBean> List<T> search(ContextServiceClient contextServiceClient, Collection<String> ids, Class<T> clazz) {
        List<T> beans = new ArrayList<>();
        Set<String> idsToSearch = new HashSet<>(ids);
        while (idsToSearch.size()>0) {
            SearchParameters searchParameters = new SearchParameters();
            Iterator<String> it = idsToSearch.iterator();
            for (int i=0; i<MAX_RESULTS; i++) {
                if (!it.hasNext()) {
                    break;
                }
                searchParameters.add("id", it.next());
                it.remove();
            }
            List<T> beanSubset = contextServiceClient.search(clazz, searchParameters, Operation.OR);
            beans.addAll(beanSubset);
        }
        return beans;
    }

    /**
     * This returns all the beans represented by the ids.  This is used when searching for <code>ContextObject</code>
     * objects. For those searches a "type" parameter is required in the "searchParameters" argument of the actual
     * search API call, and this method automatically provides that.
     *
     * Search has a max limit on the result set so this
     * does multiple search requests to return all the beans requested.
     */
    static List<ContextObject> search(ContextServiceClient contextServiceClient, Collection<String> ids, String type) {
        List<ContextObject> beans = new ArrayList<>();
        Set<String> idsToSearch = new HashSet<>(ids);
        while (idsToSearch.size()>0) {
            SearchParameters searchParameters = new SearchParameters();
            Iterator<String> it = idsToSearch.iterator();
            for (int i=0; i<MAX_RESULTS; i++) {
                if (!it.hasNext()) {
                    break;
                }
                searchParameters.add("id", it.next());
                it.remove();
            }
            searchParameters.add("type", type);
            List<ContextObject> beanSubset = contextServiceClient.search(ContextObject.class, searchParameters, Operation.OR);
            beans.addAll(beanSubset);
        }
        return beans;
    }

    /**
     * Wait for an object to not be searchable (deleted). Used for Field or Fieldset (or any other non-<code>ContextObject</code>
     * object types.
     */
    static void waitForNotSearchable(ContextServiceClient contextServiceClient, BaseDbBean contextBean, Class<? extends BaseDbBean> clazz) {
        java.util.List result;
        do {
            result = search(contextServiceClient, Collections.singletonList(contextBean.getId().toString()), clazz);
            LOGGER.info("waitForSearchable expected: 0, actual: " + result.size());
        } while (result.size()!=0);
    }

    /**
     * Wait for an object to not be searchable (deleted). Used for <code>ContextObject</code> object types.
     */
    static void waitForNotSearchable(ContextServiceClient contextServiceClient, ContextObject contextBean) {
        java.util.List result;
        do {
            result = search(contextServiceClient, Collections.singletonList(contextBean.getId().toString()), contextBean.getType());
            LOGGER.info("waitForSearchable expected: 0, actual: " + result.size());
        } while (result.size()!=0);
    }

    public static Boolean doRetry(String message, int count, long timeBetweenRetries, Function<Void, Boolean> closure) {
        for (int i=0; i<count; i++) {
            try {
                LOGGER.info("XXX --> Trying to " + message);
                return closure.apply(null);
            } catch (Exception e) {
                LOGGER.info("XXX --> Exception: " + e.getMessage());
                message = e.getMessage();
            }
            try {
                Thread.sleep(timeBetweenRetries);
            } catch (InterruptedException ignore) {
            }
        }
        return false;
    }
}
