package com.cisco.thunderhead.sample.importexport;

import com.cisco.thunderhead.BaseDbBean;
import com.cisco.thunderhead.SDKTestBase;
import com.cisco.thunderhead.client.ContextServiceClient;
import com.cisco.thunderhead.client.ContextServiceClientConstants;
import com.cisco.thunderhead.client.Operation;
import com.cisco.thunderhead.client.SearchParameters;
import com.cisco.thunderhead.connector.ConnectorConfiguration;
import com.cisco.thunderhead.connector.info.ConnectorInfoImpl;
import com.cisco.thunderhead.plugin.ConnectorFactory;
import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

/**
 * Shared utilities
 */
public class Utils {
    public static final String connectionDataFileName = "connectiondata.txt";
    private static String connectionData = "";

    /**
     * Initialize the Context Service SDK
     * @param connectionData Connector data to initialize Context Service
     * @return the initialized ContextServiceClient
     */
    static ContextServiceClient initContextServiceClient(String connectionData) {
        // host name of the connector
        String connectorHostName = "lab_connector";

        // The Connector factory has to be iniatialized before we can extract the connector
        ConnectorFactory.initializeFactory(SDKTestBase.DEFAULT_CONNECTOR_PROPERTIES_PATH);

        // Status information object for a given connector.
        ConnectorInfoImpl connectorInfo = new ConnectorInfoImpl(connectorHostName);

        // Configure client to disable upgrades, and use lab mode
        ConnectorConfiguration config = new ConnectorConfiguration();
        config.addProperty(ContextServiceClientConstants.LAB_MODE, true); // enable lab mode
        config.addProperty(ContextServiceClientConstants.NO_MANAGEMENT_CONNECTOR, getNoManagementConnector());

        // get connector for ContextServiceClient
        ContextServiceClient client = ConnectorFactory.getConnector(ContextServiceClient.class);

        // Initialize with our configuration objects
        client.init(connectionData.trim(), connectorInfo, config);
        return client;
    }

    /**
     * False to enable updates, true to disable.
     */
    private static boolean getNoManagementConnector() {
        String noManagementConnector = System.getenv(ContextServiceClientConstants.NO_MANAGEMENT_CONNECTOR);
        System.out.println("NO_MANAGEMENT_CONNECTOR: " + noManagementConnector);
        if (noManagementConnector==null) {
            noManagementConnector = "false"; // default to always update
        }
        return StringUtils.equalsIgnoreCase(noManagementConnector,"true");
    }

    public static synchronized String getConnectionData() {
        if (!StringUtils.isEmpty(connectionData)) {
            return connectionData;
        }

        String connectDataFilePath = (new File(".")).getAbsolutePath() + "/" + connectionDataFileName;
        try {
            BufferedReader connectDataFile = new BufferedReader(new FileReader(connectDataFilePath));
            connectionData = connectDataFile.readLine();
        } catch (IOException e) {
            connectionData = "";
        }

        return connectionData;
    }

    static <T extends BaseDbBean> void waitForSearchable(ContextServiceClient contextServiceClient, Collection<String> fields, Class<T> clazz, String searchType) {
        java.util.List result;
        do {
            result = search(contextServiceClient, fields, clazz, searchType);
        } while (result.size()!=fields.size());
    }

    static <T extends BaseDbBean> List<T> search(ContextServiceClient contextServiceClient, Collection<String> ids, Class<T> clazz, String searchType) {
        List<T> beans = new ArrayList<>();
        Set<String> idsToSearch = new HashSet<>(ids);
        while (idsToSearch.size()>0) {
            SearchParameters searchParameters = new SearchParameters();
            Iterator<String> it = idsToSearch.iterator();
            for (int i=0; i<10; i++) {
                if (!it.hasNext()) {
                    break;
                }
                searchParameters.add("id", it.next());
                searchParameters.add("type", searchType);
                it.remove();
            }
            List<T> beanSubset = contextServiceClient.search(clazz, searchParameters, Operation.OR);
            beans.addAll(beanSubset);
        }
        return beans;
    }
}
