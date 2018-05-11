package com.cisco.thunderhead.doc.examples;

import com.cisco.thunderhead.ContextObject;
import com.cisco.thunderhead.client.ContextServiceClient;
import com.cisco.thunderhead.client.Operation;
import com.cisco.thunderhead.client.SearchParameters;
import com.cisco.thunderhead.util.RFC3339Date;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class SearchEntities {

    // Logger
    private final static Logger LOGGER = LoggerFactory.getLogger(SearchEntities.class);

    public static String SEARCH_UNDERSCORE = "search_";
    public static final String FIELDSET = "sdkExample_" + SEARCH_UNDERSCORE + "fieldSet";
    public static final String FIELD_ONE = "sdkExample_" + SEARCH_UNDERSCORE + "fieldOne";
    public static final String FIELD_TWO = "sdkExample_" + SEARCH_UNDERSCORE + "fieldTwo";
    public static final String FIELD_THREE = "sdkExample_" + SEARCH_UNDERSCORE + "fieldThree";

    /**
     * Search for customer by ID.
     *
     * @param contextServiceClient an initialized Context Service Client
     * @param id the customer ID
     * @return List (of at most one, since ID is unique) of customer matching ID
     */
    public static List<ContextObject> searchForCustomerById (ContextServiceClient contextServiceClient, String id) {
        SearchParameters params = new SearchParameters();
        params.add("id", id);
        params.add("type", ContextObject.Types.CUSTOMER);
        // Note that for a single parameter (type is "special"), it doesn't matter whether we use the AND or OR Operation.
        return contextServiceClient.search(ContextObject.class, params, Operation.OR);
    }

    /**
     * Search for customer matching all specified fields.
     * 
     * @param contextServiceClient an initialized Context Service Client
     * @return List of customers matching the criteria
     */
    public static List<ContextObject> searchForCustomerByFirstAndLastName (ContextServiceClient contextServiceClient) {
        SearchParameters params = new SearchParameters();
        params.add("Context_First_Name", "Jane");
        params.add("Context_Last_Name", "Doe");
        params.add("type", ContextObject.Types.CUSTOMER);
        List<ContextObject> result = contextServiceClient.search(ContextObject.class, params, Operation.AND);

        for (ContextObject customer : result) {
            LOGGER.info("Found customer: " + customer.toString());
        }
        return result;
    }

    /**
     * Search for customer matching any of the specified fields.
     *
     * @param contextServiceClient an initialized Context Service Client
     * @return List of customers matching the criteria
     */
    public static List<ContextObject> searchForCustomerByFirstOrLastName (ContextServiceClient contextServiceClient) {
        SearchParameters params = new SearchParameters();
        params.add("Context_First_Name", "Jane");
        params.add("Context_Last_Name", "Doe");
        // NOTE: type is "special", so it won't just return all ContextObjects of this type when using OR
        params.add("type", ContextObject.Types.CUSTOMER);
        List<ContextObject> result = contextServiceClient.search(ContextObject.class, params, Operation.OR);

        for (ContextObject customer : result) {
            LOGGER.info("Found customer: " + customer.toString());
        }
        return result;
    }

    /**
     * Search for customer that matches any of multiple values of the custom field "sdkExample_fieldOne"
     * whose field value is either "gold" or "silver".
     *
     * @param contextServiceClient an initialized Context Service Client
     * @return List of customers matching the criteria
     */
    public static List<ContextObject> searchForCustomerByGoldOrSilver (ContextServiceClient contextServiceClient) {
        SearchParameters params = new SearchParameters();
        params.add(FIELD_ONE, "gold");
        params.add(FIELD_ONE, "silver");
        // NOTE: type is "special", so it won't just return all ContextObjects of this type when using OR
        params.add("type", ContextObject.Types.CUSTOMER);
        List<ContextObject> result = contextServiceClient.search(ContextObject.class, params, Operation.OR);

        for (ContextObject customer : result) {
            LOGGER.info("Found customer: " + customer.toString());
        }
        return result;
    }

    /**
     * Search for Pod by ID.
     *
     * @param contextServiceClient an initialized Context Service Client
     * @param id the Pod ID
     * @return List (of at most one, since ID is unique) of Pod matching ID
     */
    public static List<ContextObject> searchForPodById (ContextServiceClient contextServiceClient, String id) {
        SearchParameters params = new SearchParameters();
        params.add("id", id);
        params.add("type", ContextObject.Types.POD);
        // Note that for a single parameter (type is "special"), it doesn't matter whether we use the AND or OR Operation.
        List<ContextObject> result = contextServiceClient.search(ContextObject.class, params, Operation.OR);

        for (ContextObject pod : result) {
            LOGGER.info("Found pod: " + pod.toString());
        }
        return result;
    }

    /**
     * Search for Pod by CustomerID.
     * 
     * @param contextServiceClient an initialized Context Service Client
     * @param id the Customer ID
     * @return List of Pods associated with the Customer ID
     */
    public static List<ContextObject> searchForPodByCustomerId (ContextServiceClient contextServiceClient, String id) {
        SearchParameters params = new SearchParameters();
        params.add("customerId", id);
        params.add("type", ContextObject.Types.POD);
        // Note that for a single parameter (type is "special"), it doesn't matter whether we use the AND or OR Operation.
        List<ContextObject> result = contextServiceClient.search(ContextObject.class, params, Operation.OR);

        for (ContextObject pod : result) {
            LOGGER.info("Found pod: " + pod.toString());
        }
        return result;
    }

    /**
     * Search for Pod by Request ID.
     *
     * @param contextServiceClient an initialized Context Service Client
     * @param id the Request ID
     * @return List of Pods associated with the Request ID
     */
    public static List<ContextObject> searchForPodByRequestId (ContextServiceClient contextServiceClient, String id) {
        SearchParameters params = new SearchParameters();
        params.add("parentId", id);
        params.add("type", ContextObject.Types.POD);
        // Note that for a single parameter (type is "special"), it doesn't matter whether we use the AND or OR Operation.
        List<ContextObject> result = contextServiceClient.search(ContextObject.class, params, Operation.OR);

        for (ContextObject pod : result) {
            LOGGER.info("Found pod: " + pod.toString());
        }
        return result;
    }
    
    /**
     * Search for Pods that do not have a parent Id value.
     *
     * @param contextServiceClient an initialized Context Service Client
     * @return a list of Pods matching the query
     */
    public static List<ContextObject> searchForPodsByNullParentIdValue(ContextServiceClient contextServiceClient) {
        SearchParameters params = new SearchParameters();
        params.add("notExists", "parentId");
        params.add("type", ContextObject.Types.POD);
        List<ContextObject> result = contextServiceClient.search(ContextObject.class, params, Operation.AND);

        for (ContextObject pod : result) {
            LOGGER.info("Found pod: " + pod.toString());
        }
        return result;
    }  

    /**
     * Search for Pod by List of Pod IDs.
     * 
     * @param contextServiceClient an initialized Context Service Client
     * @param idList the list of Pod IDs
     * @return List of Pods matching any of the IDs in the list
     */
    public static List<ContextObject> searchForPodByListOfIds (ContextServiceClient contextServiceClient, List<String> idList) {
        SearchParameters params = new SearchParameters();
        params.addAll("id", idList);
        params.add("type", ContextObject.Types.POD);

        // For a list, make sure to use OR; since no Pod can match ALL of different IDs.
        // type is "special" and will be used to only match objects of that type, regardless of AND or OR
        List<ContextObject> result = contextServiceClient.search(ContextObject.class, params, Operation.OR);

        for (ContextObject pod : result) {
            LOGGER.info("Found pod: " + pod.toString());
        }
        return result;
    }

    /**
     * Returns list of PODs that any of the specified tags.  
     *
     * @param contextServiceClient an initialized Context Service Client
     * @return List of Pods that match at least one of the tags
     */
    public static List<ContextObject> searchForPodsTaggedAsSalesOrMarketing (ContextServiceClient contextServiceClient) {
        SearchParameters params = new SearchParameters();
        params.add("tags", "sales");
        params.add("tags", "marketing");
        params.add("type", ContextObject.Types.POD);
        // type is "special" and will be used to only match objects of that type, regardless of AND or OR
        List<ContextObject> result = contextServiceClient.search(ContextObject.class, params, Operation.OR);

        for (ContextObject pod : result) {
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
    public static List<ContextObject> searchForPodsTaggedAsMajorIssueForPreferredCustomer (ContextServiceClient contextServiceClient) {
        SearchParameters params = new SearchParameters();
        params.add("tags", "issue");
        params.add("tags", "major");
        params.add("tags", "preferred-customer");
        params.add("type", ContextObject.Types.POD);
        // type is "special" and will be used to only match objects of that type, regardless of AND or OR
        List<ContextObject> result = contextServiceClient.search(ContextObject.class, params, Operation.AND);

        for (ContextObject pod : result) {
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
    public static List<ContextObject> searchForPodsBySourceEmailOrSourcePhone (ContextServiceClient contextServiceClient) {
        SearchParameters params = new SearchParameters();
        params.add("Context_POD_Source_Phone", "111-111-1111");
        params.add("Context_POD_Source_Email", "John.Doe@example.com");
        params.add("type", ContextObject.Types.POD);
        // type is "special" and will be used to only match objects of that type, regardless of AND or OR
        List<ContextObject> result = contextServiceClient.search(ContextObject.class, params, Operation.OR);

        for (ContextObject pod : result) {
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
    public static List<ContextObject> searchForPodsByCreateDateRange (ContextServiceClient contextServiceClient, long startTime, long endTime) {
        // Convert times (msec) to Date/Time strings...
        String startDate = new RFC3339Date(startTime).toString();
        String endDate = new RFC3339Date(endTime).toString();

        SearchParameters params = new SearchParameters();
        params.add("startCreatedDate", startDate);
        params.add("endCreatedDate", endDate);
        params.add("type", ContextObject.Types.POD);
        // type is "special" and will be used to only match objects of that type, regardless of AND or OR
        List<ContextObject> result = contextServiceClient.search(ContextObject.class, params, Operation.AND);

        for (ContextObject pod : result) {
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
    public static List<ContextObject> searchForPodsByLastUpdatedDateRange (ContextServiceClient contextServiceClient, long startTime, long endTime) {
        // Convert times (msec) to Date/Time strings...
        String startDate = new RFC3339Date(startTime).toString();
        String endDate = new RFC3339Date(endTime).toString();

        SearchParameters params = new SearchParameters();
        params.add("startDate", startDate);
        params.add("endDate", endDate);
        params.add("type", ContextObject.Types.POD);
        // type is "special" and will be used to only match objects of that type, regardless of AND or OR
        List<ContextObject> result = contextServiceClient.search(ContextObject.class, params, Operation.AND);

        for (ContextObject pod : result) {
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
    public static List<ContextObject> searchForPodsByCustomFieldAndDateRangeAndTag(ContextServiceClient contextServiceClient, String customField, long startTime, long endTime) {
        // Convert times (msec) to Date/Time strings...
        String startDate = new RFC3339Date(startTime).toString();
        String endDate = new RFC3339Date(endTime).toString();

        SearchParameters params = new SearchParameters();
        params.add("startCreatedDate", startDate);
        params.add("endCreatedDate", endDate);
        params.add(FIELD_ONE, customField);
        params.add("tags", "cancellation");
        params.add("type", ContextObject.Types.POD);
        // type is "special" and will be used to only match objects of that type, regardless of AND or OR
        List<ContextObject> result = contextServiceClient.search(ContextObject.class, params, Operation.AND);

        for (ContextObject pod : result) {
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
    public static List<ContextObject> searchForActivePods(ContextServiceClient contextServiceClient) {
        SearchParameters params = new SearchParameters(){{
            add("state", ContextObject.States.ACTIVE);
            add("type", ContextObject.Types.POD);
        }};
        // type is "special" and will be used to only match objects of that type, regardless of AND or OR
        List<ContextObject> result = contextServiceClient.search(ContextObject.class, params, Operation.AND);

        for (ContextObject pod : result) {
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
    public static List<ContextObject> searchForPodsByQueryString(ContextServiceClient contextServiceClient){
        SearchParameters params = new SearchParameters();
        params.add("query_string", "Ivanna.Buy@prospect.com 222-222-2222 banana \"Ivanna Buy\"");
        params.add("type", ContextObject.Types.POD);
        // type is "special" and will be used to only match objects of that type, regardless of AND or OR
        List<ContextObject> result = contextServiceClient.search(ContextObject.class, params, Operation.AND);

        for (ContextObject pod: result){
            LOGGER.info("Found pod: " + pod.toString());
        }
        return result;
    }
}
