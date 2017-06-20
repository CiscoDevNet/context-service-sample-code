package com.cisco.thunderhead.doc.examples;

import com.cisco.thunderhead.client.ContextServiceClient;
import com.cisco.thunderhead.client.Operation;
import com.cisco.thunderhead.client.SearchParameters;
import com.cisco.thunderhead.customer.Customer;
import com.cisco.thunderhead.datatypes.PodState;
import com.cisco.thunderhead.pod.Pod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class SearchEntities {

    // Logger
    private final static Logger LOGGER = LoggerFactory.getLogger(SearchEntities.class);
    private final static String DATE_STRING_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";

    /**
     * Search for customer by ID.
     *
     * @param contextServiceClient an initialized Context Service Client
     * @param id the customer ID
     * @return List (of at most one, since ID is unique) of customer matching ID
     */
    public static List<Customer> searchForCustomerById (ContextServiceClient contextServiceClient, String id) {
        SearchParameters params = new SearchParameters();
        params.add("id", id);
        // Note that for a single parameter, it doesn't matter whether we use the AND or OR Operation.
        return contextServiceClient.search(Customer.class, params, Operation.OR);
    }

    /**
     * Search for customer matching all specified fields.
     * 
     * @param contextServiceClient an initialized Context Service Client
     * @return List of customers matching the criteria
     */
    public static List<Customer> searchForCustomerByFirstAndLastName (ContextServiceClient contextServiceClient) {
        SearchParameters params = new SearchParameters();
        params.add("Context_First_Name", "Jane");
        params.add("Context_Last_Name", "Doe");
        List<Customer> result = contextServiceClient.search(Customer.class, params, Operation.AND);

        for (Customer customer : result) {
            LOGGER.info("Found customer: " + customer.toString());
        }
        return result;
    }

    /**
     * Search for customer matching any of the specified fields.
     *
     * @param contextServiceClient  an initialized Context Service Client
     * @return List of customers matching the criteria
     */
    public static List<Customer> searchForCustomerByFirstOrLastName (ContextServiceClient contextServiceClient) {
        SearchParameters params = new SearchParameters();
        params.add("Context_First_Name", "Jane");
        params.add("Context_Last_Name", "Doe");
        List<Customer> result = contextServiceClient.search(Customer.class, params, Operation.OR);

        for (Customer customer : result) {
            LOGGER.info("Found customer: " + customer.toString());
        }
        return result;
    }

    /**
     * Search for customer that matches any of multiple values of the custom field "sdkExample_fieldOne"
     * whose field value is either "gold" or "silver".
     *
     * @param contextServiceClient  an initialized Context Service Client
     * @return List of customers matching the criteria
     */
    public static List<Customer> searchForCustomerByGoldOrSilver (ContextServiceClient contextServiceClient) {
        SearchParameters params = new SearchParameters();
        params.add("sdkExample_fieldOne", "gold");
        params.add("sdkExample_fieldOne", "silver");
        List<Customer> result = contextServiceClient.search(Customer.class, params, Operation.OR);

        for (Customer customer : result) {
            LOGGER.info("Found customer: " + customer.toString());
        }
        return result;
    }

    /**
     * Search for Pod by podID.
     *
     * @param contextServiceClient  an initialized Context Service Client
     * @param id the Pod ID
     * @return List (of at most one, since ID is unique) of Pod matching ID
     */
    public static List<Pod> searchForPodById (ContextServiceClient contextServiceClient, String id) {
        SearchParameters params = new SearchParameters();
        params.add("id", id);
        // Note that for a single parameter, it doesn't matter whether we use the AND or OR Operation.
        List<Pod> result = contextServiceClient.search(Pod.class, params, Operation.OR);

        for (Pod pod : result) {
            LOGGER.info("Found pod: " + pod.toString());
        }
        return result;
    }

    /**
     * Search for Pod by CustomerID.
     * 
     * @param contextServiceClient  an initialized Context Service Client
     * @param id the Customer ID
     * @return List of Pods associated with the Customer ID
     */
    public static List<Pod> searchForPodByCustomerId (ContextServiceClient contextServiceClient, String id) {
        SearchParameters params = new SearchParameters();
        params.add("customerId", id);
        // Note that for a single parameter, it doesn't matter whether we use the AND or OR Operation.
        List<Pod> result = contextServiceClient.search(Pod.class, params, Operation.OR);

        for (Pod pod : result) {
            LOGGER.info("Found pod: " + pod.toString());
        }
        return result;
    }

    /**
     * Search for Pod by RequestID.
     *
     * @param contextServiceClient  an initialized Context Service Client
     * @param id the Request ID
     * @return List of Pods associated with the Request ID
     */
    public static List<Pod> searchForPodByRequestId (ContextServiceClient contextServiceClient, String id) {
        SearchParameters params = new SearchParameters();
        params.add("requestId", id);
        // Note that for a single parameter, it doesn't matter whether we use the AND or OR Operation.
        List<Pod> result = contextServiceClient.search(Pod.class, params, Operation.OR);

        for (Pod pod : result) {
            LOGGER.info("Found pod: " + pod.toString());
        }
        return result;
    }

    /**
     * Search for Pod by List of Pod IDs.
     * 
     * @param contextServiceClient  an initialized Context Service Client
     * @param idList the list of Pod IDs
     * @return List of Pods matching any of the IDs in the list
     */
    public static List<Pod> searchForPodByListOfIds (ContextServiceClient contextServiceClient, List<String> idList) {
        SearchParameters params = new SearchParameters();
        params.addAll("id", idList);

        // For a list, make sure to use OR; since no Pod can match ALL of different IDs.
        List<Pod> result = contextServiceClient.search(Pod.class, params, Operation.OR);

        for (Pod pod : result) {
            LOGGER.info("Found pod: " + pod.toString());
        }
        return result;
    }

    /**
     * Returns list of PODs that any of the specified tags.  
     *
     * @param contextServiceClient  an initialized Context Service Client
     * @return List of Pods that match at least one of the tags
     */
    public static List<Pod> searchForPodsTaggedAsSalesOrMarketing (ContextServiceClient contextServiceClient) {
        SearchParameters params = new SearchParameters();
        params.add("tags", "sales");
        params.add("tags", "marketing");
        List<Pod> result = contextServiceClient.search(Pod.class, params, Operation.OR);

        for (Pod pod : result) {
            LOGGER.info("Found pod: " + pod.toString());
        }
        return result;
    }

    /**
     * Returns list of PODs that match ALL specified tags.
     *
     * @param contextServiceClient  an initialized Context Service Client
     * @return List of Pods that match all of the tags
     */
    public static List<Pod> searchForPodsTaggedAsMajorIssueForPreferredCustomer (ContextServiceClient contextServiceClient) {
        SearchParameters params = new SearchParameters();
        params.add("tags", "issue");
        params.add("tags", "major");
        params.add("tags", "preferred-customer");
        List<Pod> result = contextServiceClient.search(Pod.class, params, Operation.AND);

        for (Pod pod : result) {
            LOGGER.info("Found pod: " + pod.toString());
        }
        return result;
    }

    /**
     * Returns list of PODs that match either of two fields.  In this case,
     * we show a search for any pod that has source phone number "111-111-1111"
     * or source email "John.Doe@example.com".
     * @param contextServiceClient  an initialized Context Service Client
     * @return List of Pods that match any of the field criteria
     */
    public static List<Pod> searchForPodsBySourceEmailOrSourcePhone (ContextServiceClient contextServiceClient) {
        SearchParameters params = new SearchParameters();
        params.add("Context_POD_Source_Phone", "111-111-1111");
        params.add("Context_POD_Source_Email", "John.Doe@example.com");
        List<Pod> result = contextServiceClient.search(Pod.class, params, Operation.OR);

        for (Pod pod : result) {
            LOGGER.info("Found pod: " + pod.toString());
        }
        return result;
    }

    /**
     * Search for PODs created within a specified date and time range.
     * 
     * @param contextServiceClient  an initialized Context Service Client
     * @param startTime return PODs that were created no earlier than this time
     * @param endTime return PODs that were created no later than this time
     * @return List of PODs that were created within the date/time range.
     */
    public static List<Pod> searchForPodsByCreateDateRange (ContextServiceClient contextServiceClient, long startTime, long endTime) {
        // Convert times (msec) to Date/Time strings...
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_STRING_FORMAT);
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
        String startDate = sdf.format(new Date(startTime));
        String endDate = sdf.format(new Date(endTime));

        SearchParameters params = new SearchParameters();
        params.add("startCreatedDate", startDate);
        params.add("endCreatedDate", endDate);
        List<Pod> result = contextServiceClient.search(Pod.class, params, Operation.AND);

        for (Pod pod : result) {
            LOGGER.info("Found pod: " + pod.toString());
        }
        return result;
    }

    /**
     * Search for PODs last updated within a specified date and time range.
     * 
     * @param contextServiceClient  an initialized Context Service Client
     * @param startTime return PODs that were updated no earlier than this time
     * @param endTime return PODs that were dupated no later than this time
     * @return List of Pods that were updated within the date range
     */
    public static List<Pod> searchForPodsByLastUpdatedDateRange (ContextServiceClient contextServiceClient, long startTime, long endTime) {
        // Convert times (msec) to Date/Time strings...
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_STRING_FORMAT);
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
        String startDate = sdf.format(new Date(startTime));
        String endDate = sdf.format(new Date(endTime));

        SearchParameters params = new SearchParameters();
        params.add("startDate", startDate);
        params.add("endDate", endDate);
        List<Pod> result = contextServiceClient.search(Pod.class, params, Operation.AND);

        for (Pod pod : result) {
            LOGGER.info("Found pod: " + pod.toString());
        }
        return result;
    }

    /**
     * Build a complex query checking for a custom field, a time range, and a tag.
     * 
     * @param contextServiceClient  an initialized Context Service Client
     * @param startTime return PODs that were created no earlier than this time
     * @param endTime return PODs that were created no later than this time
     * @return a list of Pods matching the query
     */
    public static List<Pod> searchForPodsByCustomFieldAndDateRangeAndTag(ContextServiceClient contextServiceClient, String customField, long startTime, long endTime) {
        // Convert times (msec) to Date/Time strings...
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_STRING_FORMAT);
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
        String startDate = sdf.format(new Date(startTime));
        String endDate = sdf.format(new Date(endTime));

        SearchParameters params = new SearchParameters();
        params.add("startCreatedDate", startDate);
        params.add("endCreatedDate", endDate);
        params.add("sdkExample_fieldOne", customField);
        params.add("tags", "cancellation");
        List<Pod> result = contextServiceClient.search(Pod.class, params, Operation.AND);

        for (Pod pod : result) {
            LOGGER.info("Found pod: " + pod.toString());
        }
        return result;
    }

    /**
     * Search for all active PODs.
     * 
     * @param contextServiceClient  an initialized ContextServiceClient
     * @return a list of open PODs
     */
    public static List<Pod> searchForActivePods(ContextServiceClient contextServiceClient) {
        SearchParameters params = new SearchParameters(){{
            add("state", PodState.ACTIVE);
        }};
        List<Pod> result = contextServiceClient.search(Pod.class, params, Operation.AND);

        for (Pod pod : result) {
            LOGGER.info("Found pod: " + pod.toString());
        }
        return result;
    }

    /**
     * Search for PODs based on the last contributor, or the last person  to create or modify the POD.
     * 
     * @param contextServiceClient  an initialized ContextServiceClient
     * @param contributorUsername the username of a contributor
     * @return a list of PODs last modified by the given contributor
     */
    public static List<Pod> searchForPodsByLastContributor(ContextServiceClient contextServiceClient, final String contributorUsername) {
        SearchParameters params = new SearchParameters(){{
            add("newContributor.username", contributorUsername);
        }};
        List<Pod> result = contextServiceClient.search(Pod.class, params, Operation.AND);

        for (Pod pod: result) {
            LOGGER.info("Found pod: " + pod.toString());
        }
        return result;
    }

    /**
     * Search for all PODs modified by the specified contributor.
     * 
     * @param contextServiceClient  an initialized ContextServiceClient
     * @param contributorUsername the username of a contributor
     * @return a list of PODs modified by the given contributor
     */
    public static List<Pod> searchForPodsByContributor(ContextServiceClient contextServiceClient, final String contributorUsername) {
        SearchParameters params = new SearchParameters(){{
            add("contributors.username", contributorUsername);
        }};
        List<Pod> result = contextServiceClient.search(Pod.class, params, Operation.AND);

        for (Pod pod: result) {
            LOGGER.info("Found pod: " + pod.toString());
        }
        return result;
    }

    /**
     *  Search for PODs with a specified query_string parameter.
     * 
     *  @param contextServiceClient  an initialized ContextServiceClient
     *  @return a list of PODs found by the query_string sub queries
     */
    public static List<Pod> searchForPodsByQueryString(ContextServiceClient contextServiceClient){
        SearchParameters params = new SearchParameters();
        params.add("query_string", "Ivanna.Buy@prospect.com 222-222-2222 banana \"Ivanna Buy\"");

        List<Pod> result = contextServiceClient.search(Pod.class, params, Operation.AND);

        for (Pod pod: result){
            LOGGER.info("Found pod: " + pod.toString());
        }
        return result;
    }
}
