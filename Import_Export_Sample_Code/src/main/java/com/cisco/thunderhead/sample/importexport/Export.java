package com.cisco.thunderhead.sample.importexport;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import com.cisco.thunderhead.ContextBean;
import com.cisco.thunderhead.SDKTestBase;
import com.cisco.thunderhead.client.ContextServiceClient;
import com.cisco.thunderhead.client.ContextServiceClientConstants;
import com.cisco.thunderhead.client.Operation;
import com.cisco.thunderhead.client.SearchParameters;
import com.cisco.thunderhead.connector.ConnectorConfiguration;
import com.cisco.thunderhead.connector.info.ConnectorInfoImpl;
import com.cisco.thunderhead.customer.Customer;
import com.cisco.thunderhead.plugin.ConnectorFactory;
import com.cisco.thunderhead.pod.Pod;
import com.cisco.thunderhead.request.Request;
import com.cisco.thunderhead.util.RFC3339Date;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

/**+
 * Export : This utility class is to export data for a specific org within a
 * given range of time.
 *
 * Program arguments :
 * -c, --connection             Specify the connection data as Base64 string
 * -d, --deleteExistingExports  If set, deletes existing export files in the same directory (disabled by default)
 * -e, --endDate                Specify the end date for the date range should be backup in the RFC3339 format (yyyy-MM-dd'T'HH:mm:ss.SSS'Z') (Default: NOW)
 * -m, --maxSummaryIds          The number of entity IDs to fetch per call (Max: 100000, Default: 1000)
 * -o, --output                 Specify the output directory
 * -p, --pretty                 If set, enables pretty printing the output json (disabled by default)
 * -s, --startDate              Specify the start date from which data to be backup in the RFC3339 format (yyyy-MM-dd'T'HH:mm:ss.SSS'Z')
 * -w, --windowSize             The number of entities to fetch per call (Max: 200, Default: 50)
 */
public class Export {

    private static Logger LOGGER = LoggerFactory.getLogger(Export.class);
    private static ContextServiceClient contextServiceClient;

    public static void main(String[] args) throws Exception {

        Arguments arguments = new Arguments(args);

        // initialize the Context Service SDK using the connection data passed from arguments
        contextServiceClient = initContextServiceClient(arguments.connection);

        // start timer to log performance
        long lStartTime = System.currentTimeMillis();

        // Open output files
        // We want to write entities to file as we fetch them, instead of all at once
        // because for large exports, the amount of memory required to hold every entity might be very large
        LOGGER.info("Creating output files...");
        Path outputDir = Paths.get(arguments.outputDir);
        JsonArrayWriter podWriter = new JsonArrayWriter("pod", outputDir, arguments.pretty, arguments.deleteExistingExports);
        JsonArrayWriter custWriter = new JsonArrayWriter("customer", outputDir, arguments.pretty, arguments.deleteExistingExports);
        JsonArrayWriter reqWriter = new JsonArrayWriter("request", outputDir, arguments.pretty, arguments.deleteExistingExports);

        try {
            // First, fetch pod IDs only from search API (with summary=true)
            // summary=true tells Context Service to only return the entity IDs matching the query, not the whole object
            // In summary mode, search returns up to 100000 entities (vs. 200 wth normal search API)
            // So we fetch all of the IDs in summary mode, and based on those IDs list, fetch each entity
            // Limit the search to the entities changed within the given time window
            Set<String> podIds = fetchEntityIds(Pod.class, arguments.startDate, arguments.endDate, arguments.maxSummaryIds);
            Set<String> customerIds = fetchEntityIds(Customer.class, arguments.startDate, arguments.endDate, arguments.maxSummaryIds);
            Set<String> requestIds = fetchEntityIds(Request.class, arguments.startDate, arguments.endDate, arguments.maxSummaryIds);

            // Java 8 lambda expression to run on each pod that is retrieved
            // The customer and request associated with each pod might not have been updated within the given window
            // so for each pod, explicitly extract the customer and request ids,
            // and them to their respective sets if they're not already included.
            Consumer<Pod> extractCustomerIdAndRequestId = pod -> {
                UUID customerId = pod.getCustomerId();
                UUID requestId = pod.getRequestId();

                if (customerId != null) {
                    customerIds.add(customerId.toString());
                }

                if (requestId != null) {
                    requestIds.add(requestId.toString());
                }
            };

            // Now that we have a list of ids for each entity type,
            // fetch the full entities from Context Service in chunks.
            // Write them to the file as we go so that we don't have to store them in memory.
            fetchVisitAndWriteEntities(Pod.class, podIds, podWriter, arguments.windowSize, extractCustomerIdAndRequestId);
            fetchAndWriteEntities(Customer.class, customerIds, custWriter, arguments.windowSize);
            fetchAndWriteEntities(Request.class, requestIds, reqWriter, arguments.windowSize);
        } finally {
            // Finish writing the files
            LOGGER.info("Closing writers...");
            podWriter.close();
            custWriter.close();
            reqWriter.close();

            // Metrics
            long lEndTime = System.currentTimeMillis();
            LOGGER.info("Total time elapsed in millis: "+(lEndTime - lStartTime));
            LOGGER.info("Number of pod written: " + podWriter.getNumberOfEntities());
            LOGGER.info("Number of customer written: " + custWriter.getNumberOfEntities());
            LOGGER.info("Number of request written: " + reqWriter.getNumberOfEntities());

            // Destroys and cleanups connector threads and tokens.
            contextServiceClient.destroy();
        }
    }

    /**
     * Initialize the Context Service SDK
     * @param connectionData Connector data to initialize Context Service
     * @return the initialized ContextServiceClient
     */
    private static ContextServiceClient initContextServiceClient(String connectionData) {
        // host name of the connector
        String connectorHostName = "lab_connector";

        // The Connector factory has to be iniatialized before we can extract the connector
        ConnectorFactory.initializeFactory(SDKTestBase.DEFAULT_CONNECTOR_PROPERTIES_PATH);

        // Status information object for a given connector.
        ConnectorInfoImpl connectorInfo = new ConnectorInfoImpl(connectorHostName);

        // Configure client to disable upgrades, and use lab mode
        ConnectorConfiguration config = new ConnectorConfiguration();
        config.addProperty(ContextServiceClientConstants.LAB_MODE, true); // enable lab mode
        config.addProperty(ContextServiceClientConstants.NO_MANAGEMENT_CONNECTOR, true); // Disable updates

        // get connector for ContextServiceClient
        ContextServiceClient client = ConnectorFactory.getConnector(ContextServiceClient.class);

        // Initialize with our configuration objects
        client.init(connectionData.trim(), connectorInfo, config);
        return client;
    }

    /**
     * Fetch all of the entity IDs of the given type within the given window.
     * Uses the ContextService search API with (summary=true) to get a lot of IDs at once.
     * If there are more IDs in the database than allowed by one call (maximum allowed by ContextService is 100,000),
     * then make recursive calls with smaller windows, and combine the results.
     * @param type type of Context Service entity to get IDs for
     * @param startDate start of window
     * @param endDate end of the window
     * @return a set of Entity Ids that were last updated within the given time window
     */
    private static <T extends ContextBean> Set<String> fetchEntityIds(Class<T> type, RFC3339Date startDate, RFC3339Date endDate, int maxSummaryIds) {
        LOGGER.info("Fetching " + type.getSimpleName() + " ids...");

        // perform a summary search or a given time window using Context Service SDK
        SearchParameters searchParameters = new SearchParameters();
        searchParameters.add("summary", "true");
        searchParameters.add("startDate", startDate.toString());
        searchParameters.add("endDate", endDate.toString());
        searchParameters.add("maxEntries", Integer.toString(maxSummaryIds)); // explicitly set bound in case the default changes
        List<T> entityIdBeans  = contextServiceClient.search(type, searchParameters, Operation.OR);

        // extract the id from every entity
        Set<String> entityIds = new HashSet<String>();

        // Recursive case:
        // If the summary API returns the maxed we ask for,
        // that means there may be more entities in the database that match the criteria.
        // Context service returns the ids in order of lastUpdated (newest to oldest),
        // so the approach here is to get the last updated date of the oldest one in the set,
        // and use that as our new end date, so we only get entities at least as old as that one.
        if (entityIdBeans.size() >= maxSummaryIds) {
            // fetch the oldest entity from the list
            String id = entityIdBeans.get(entityIdBeans.size() -1).getId().toString();
            String url = "context/context/v1/id/" + id;
            T entity = contextServiceClient.get(type, url);

            // make a recursive call, with that end date of the oldest pod so far as the new end date
            RFC3339Date newEndDate = entity.getLastUpdated();
            if (newEndDate != endDate) { // make sure our conditions changed
                Set<String> olderIds = fetchEntityIds(type, startDate, newEndDate, maxSummaryIds);
                entityIds.addAll(olderIds); // add these IDs to our results set
            } else { // our conditions are the same, can't recurse, so print warning and continue
                LOGGER.warn("May be missing entity IDs, but cannot shrink window size. " + // should never happen
                        "Matched " + entityIdBeans.size() + " in range: " + startDate + " - " + endDate);
            }
        }

        // extract the ids out and add them to our results set
        for (ContextBean bean: entityIdBeans) {
            entityIds.add(bean.getId().toString());
        }

        return entityIds;
    }

    /**
     * Fetch entities in chunks, and write each chunk to a file
     * Context Service will only return up to 200 full entities at a time, so we need to loop through the ids
     * @param type type of the Context Service entity
     * @param ids a collection of entity ids
     * @param writer JsonArrayWriter, to write entities to file as we go
     * @param visitor a callback to run on each individual entity before we write it.
     * @throws Exception
     */
    private static <T extends ContextBean> void fetchVisitAndWriteEntities(
            Class<T> type, Collection<String> ids, JsonArrayWriter writer, int windowSize, Consumer<T> visitor) throws Exception{

        LOGGER.info("Fetching " + type.getSimpleName() + " entities...");

        // convert to list make chunking easier
        List<String> idList = new ArrayList<String>(ids);

        // fetch entities in chunks of WINDOW_SIZE (maximum allowed by Context Service is 200)
        List<List<String>> idListChunks =  chunk(idList, windowSize);

        for (List<String> idListChunk: idListChunks) {
            // Fetch the chunk using Context Service search API
            SearchParameters searchParameters = new SearchParameters();
            searchParameters.addAll("id", idListChunk);
            List<T> someEntities = contextServiceClient.search(type, searchParameters, Operation.OR);

            // Perform callback on every entity (Java 8 stream API)
            if (visitor != null) {
                someEntities.forEach(visitor);
            }

            // write the entities to a file
            if(someEntities.size() > 0) {
                writer.writeEntities(someEntities);
            }
        }
    }

    // no callback
    private static <T extends ContextBean> void fetchAndWriteEntities(Class<T> type, Collection<String> ids, JsonArrayWriter writer, int windowSize) throws Exception{
        fetchVisitAndWriteEntities(type, ids, writer, windowSize, null);
    }

    /**
     * Helper to take a long list and break it a list of shorter lists
     * @param list A list of type T
     * @param chunkSize Maximum size of the smaller lists
     * @param <T> any type
     * @return a list of lists
     */
    private static <T> List<List<T>>  chunk (List<T> list, int chunkSize) {
        List<List<T>> listOfLists = new ArrayList<List<T>>();

        for (int i = 0; i < list.size(); i += chunkSize) {
            List<T> chunk = i + chunkSize > list.size()
                    ? list.subList(i, list.size())
                    : list.subList(i, i + chunkSize);
            listOfLists.add(chunk);
        }

        return listOfLists;
    }

    /**
     * Commandline argument specifications
     */
    public static class Arguments {

        Arguments(String []args){
            parseArguments(args);
        }

        final Logger LOGGER = LoggerFactory.getLogger(Arguments.class);
        @Parameter(names = {"-c", "--connection"}, description = "Specify the connection data as Base64 string", required = true)
        String connection;

        @Parameter(names = {"-s", "--startDate"}, converter = RFC3339Converter.class, description = "Specify the start date from which data to be export in the RFC3339 format (yyyy-MM-dd'T'HH:mm:ss.SSS'Z') ", required = true)
        RFC3339Date startDate;

        @Parameter(names = {"-e", "--endDate"}, converter = RFC3339Converter.class, description = "Specify the end date untill data should be export in the RFC3339 format (yyyy-MM-dd'T'HH:mm:ss.SSS'Z') ", required = false)
        RFC3339Date endDate = new RFC3339Date(new Date().getTime()); // default: NOW

        @Parameter(names = {"-o", "--output"}, description = "Specify the output directory ", required = true)
        String outputDir;

        @Parameter(names = {"-p", "--pretty"}, description = "Specify pretty print output (Default: false)", required = false)
        boolean pretty = false; // default: false

        @Parameter(names = {"-d", "--deleteExistingExports"}, description = "Overwrite existing export files in the specified directory (Default: false)", required = false)
        boolean deleteExistingExports = false; // default: false

        @Parameter(names = {"-m", "--maxSummaryIds"}, arity = 1, description = "The number of entity IDs to fetch per call (Max: 100000, Default: 1000)", required = false)
        int maxSummaryIds = 1000;
        private int MAX_SUMMARY_ENTRIES = 100000;

        @Parameter(names = {"-w", "--windowSize"}, arity = 1, description = "The number of entities to fetch per call (Max: 200, Default: 50)", required = false)
        int windowSize = 50;
        private int MAX_WINDOW_SIZE = 200;

        /**+
         * parseArguments : parse the arguments using JCommander
         * @param args
         */
        public void parseArguments(String args[]){
            try {
                JCommander jc = new JCommander(this, args);

                if (maxSummaryIds > MAX_SUMMARY_ENTRIES || maxSummaryIds < 2) { // DEFAULT_SUMMARY_ENTRIES
                    throw new ParameterException("maxSummaryIds must be between " + 2 + " and " + MAX_SUMMARY_ENTRIES);
                }

                if (windowSize > MAX_WINDOW_SIZE || windowSize < 1) { // MAX_POD_ENTRIES_LIMIT
                    throw new ParameterException("windowSize must be between " + 1 + " and " + MAX_WINDOW_SIZE);
                }

                LOGGER.info("Arguments- " + "StartDate: " + startDate + ", EndDate: " + endDate + ", Connection String: " + connection);
            }catch (ParameterException e) {
                // Display usage - this will catch the case where no args are specified as well as the case where required args are missing
                LOGGER.error("Invalid parameter: ", e);
                JCommander jCommander = new JCommander(this);
                jCommander.usage();
                throw (e);
            }
        }
    }
}
