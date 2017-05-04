package com.cisco.thunderhead.example.ui;

import com.cisco.thunderhead.BaseDbBean;
import com.cisco.thunderhead.client.ContextServiceClient;
import com.cisco.thunderhead.client.ContextServiceClientConstants;
import com.cisco.thunderhead.client.Operation;
import com.cisco.thunderhead.client.SearchParameters;
import com.cisco.thunderhead.connector.ConnectorConfiguration;
import com.cisco.thunderhead.connector.info.ConnectorInfoImpl;
import com.cisco.thunderhead.connector.states.ConnectorStateListener;
import com.cisco.thunderhead.plugin.ConnectorFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.Function;

/**
 * Various utilities.
 */
public class Utils {
    private static final Logger LOGGER = LoggerFactory.getLogger(Utils.class);
    private static final int MAX_RESULTS = 25;

    public static ContextServiceClient getInitializedContextServiceClient(String jsonConnectionData, ConnectorStateListener listener) {
        ContextServiceClient contextServiceClient = ConnectorFactory.getConnector(ContextServiceClient.class);

        if (listener!=null) {
            contextServiceClient.addStateListener(listener);
        }

        boolean labMode = true;
        int requestTimeOut = 40000;
        boolean noFms = true;

        String hostname = "doctest.example.com";
        ConnectorInfoImpl connInfo = new ConnectorInfoImpl(hostname);
        ConnectorConfiguration configuration = new ConnectorConfiguration(){{
            addProperty(ContextServiceClientConstants.LAB_MODE, labMode); // exclude this line for prod mode
            addProperty(ContextServiceClientConstants.REQUEST_TIMEOUT, requestTimeOut);
            addProperty(ContextServiceClientConstants.NO_MANAGEMENT_CONNECTOR, noFms);
        }};
        contextServiceClient.init(jsonConnectionData, connInfo, configuration);
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

    static <T extends BaseDbBean> void waitForSearchable(ContextServiceClient contextServiceClient, Collection<String> fields, Class<T> clazz) {
        java.util.List result;
        do {
            result = search(contextServiceClient, fields, clazz);
            LOGGER.info("waitForSearchable expected: " + fields.size() + ", actual: " + result.size());
        } while (result.size()!=fields.size());
    }

    /**
     * This returns all the beans represented by the ids.  Search has a max limit on the result set so this
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

    static void waitForNotSearchable(ContextServiceClient contextServiceClient, BaseDbBean contextBean, Class<? extends BaseDbBean> clazz) {
        java.util.List result;
        do {
            result = search(contextServiceClient, Collections.singletonList(contextBean.getId().toString()), clazz);
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
