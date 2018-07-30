package com.cisco.thunderhead.rest;

import com.cisco.thunderhead.util.SDKUtils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Tests the REST API
 */
public class ContextServiceApiTest {
    private static final String BASE_URL = "http://localhost:8080/rest";
    private static final String GET_BASE_URL = BASE_URL + "/%s/";
    private static final String SEARCH_BASE_URL = BASE_URL + "/search/%s/";
    private static Client client;
    private static String ACTIVITY_ID;
    private static String FIELD_DATA;
    private static String CUSTOMER_ID;
    private static String REQUEST_ID;
    private static String POD_TYPE ="pod";
    private static String CUSTOMER_TYPE ="customer";
    private static String REQUEST_TYPE ="request";

    /**
     * Setup.  Create a  customer, request and an activity used by tests in this class.
     */
    @BeforeClass
    public static void setUp() {
        client = ClientBuilder.newClient();
        client.register(new LoggingFilter()); // so we can see HTTP traffic
        FIELD_DATA = UUID.randomUUID().toString();
        CUSTOMER_ID = createContextObject(CUSTOMER_TYPE, "cisco.base.customer", "Context_Home_Phone","123-456-7890",null, null);
        REQUEST_ID = createContextObject(REQUEST_TYPE, "cisco.base.request", "Context_Description","testing CustomerId", CUSTOMER_ID, null);
        ACTIVITY_ID = createContextObject(POD_TYPE,"cisco.base.pod", "Context_Notes", FIELD_DATA, null, null);
    }

    /**
     * Cleanup. Delete the customer, request and activity created in the beginning of the test
     */
    @AfterClass
    public static void tearDown() {
        deleteContextObject(ACTIVITY_ID, POD_TYPE);
        deleteContextObject(CUSTOMER_ID.toString(), CUSTOMER_TYPE);
        deleteContextObject(REQUEST_ID.toString(), REQUEST_TYPE);
    }

    /**
     * This creates the request.  It re-uses the server-side RESTContextObject to make creating the request easier.
     */
    @Test
    public void testCreate() {
        RESTContextObject request = createRESTContextObject(POD_TYPE, "cisco.base.pod", null, null);
        addDataElementsToRequest(request, "Context_Notes", "testing at 3:16", "string");

        String requestBody = getGson().toJson(request);

        // do the create
        Response response = client
                .target(BASE_URL).request().accept(MediaType.APPLICATION_JSON_TYPE)
                .post(Entity.json(requestBody));

        assertEquals("should have succeeded", 201, response.getStatus());
        String id = SDKUtils.getIdFromUri(response.getLocation());
        // now delete it
        response = client.target(setBasePath(POD_TYPE) + id).request().delete();
        assertEquals("should have succeeded", 202, response.getStatus());
    }

    /**
     * Tests retrieve.
     */
    @Test
    public void testGet() {
        Response response;

        // failure case
        response = client.target(setBasePath(POD_TYPE)+ "/" + ACTIVITY_ID + "blah").request().get();
        assertEquals("should succeed", 500, response.getStatus());

        // success case
        RESTContextObject RESTContextObject = getContextObject(ACTIVITY_ID, POD_TYPE);
        assertEquals("unexpected contents", 1, RESTContextObject.getDataElements().size());
        assertEquals("unexpected contents", "Context_Notes", RESTContextObject.getDataElements().get(0).getKey());
        assertEquals("unexpected contents", FIELD_DATA, RESTContextObject.getDataElements().get(0).getValue());
    }

    /**
     * Tests searching.
     */
    @Test
    public void testSearch() {
        waitForSearchable("Context_Notes", FIELD_DATA, POD_TYPE);
        Map<String, List<String>> query = new HashMap<>();
        query.put("Context_Notes", Arrays.asList(FIELD_DATA));
        SearchParams searchParams = new SearchParams("or", query);
        String requestBody = getGson().toJson(searchParams);
        JsonArray objects = searchContextObject(requestBody, POD_TYPE);
        assertEquals("should be only 1", 1, objects.size());
        JsonArray dataElements = objects.get(0).getAsJsonObject().get("dataElements").getAsJsonArray();
        assertEquals("wrong field data", FIELD_DATA, dataElements.get(0).getAsJsonObject().get("value").getAsString());
    }

    /**
     * This creates the activity with customerId and not requestID
     * Get the activity and verify the customerId and parentId
     * Search for the activity that has bot customerId and requestId and verify it
     */
    @Test
    public void testCreateGetSearchDeletePodWithCustomerId() {
        RESTContextObject request = createRESTContextObject(POD_TYPE, "cisco.base.pod", CUSTOMER_ID, null);
        addDataElementsToRequest(request, "Context_Notes", "testing CustomerId", "string");

        String requestBody = getGson().toJson(request);

        // do the create
        Response response = client
                .target(BASE_URL).request().accept(MediaType.APPLICATION_JSON_TYPE)
                .post(Entity.json(requestBody));

        assertEquals("should have succeeded", 201, response.getStatus());

        String id = SDKUtils.getIdFromUri(response.getLocation());

        //get activity
        RESTContextObject contextObject = getContextObject(id, POD_TYPE);
        assertEquals(CUSTOMER_ID, contextObject.getCustomerId().toString());

        // search  activity for customerId
        waitForSearchable("customerId", CUSTOMER_ID.toString(), POD_TYPE);
        Map<String, List<String>> query = new HashMap<>();
        query.put("customerId", Arrays.asList(CUSTOMER_ID.toString()));
        SearchParams searchParams = new SearchParams("or", query);
        requestBody = getGson().toJson(searchParams);
        JsonArray objects = searchContextObject(requestBody, POD_TYPE);
        assertEquals("should be only 1", 1, objects.size());
        String customerId = objects.get(0).getAsJsonObject().get("customerId").getAsString();
        assertEquals("customerId should match", CUSTOMER_ID.toString(), customerId);

        // now delete it
        response = client.target(setBasePath(POD_TYPE) + id).request().delete();
        assertEquals("should have succeeded", 202, response.getStatus());
    }


    /**
     * This creates the activity with requestId and no customerId
     * Get the activity and verify the customerId and parentId
     * Search for the activity that has bot customerId and requestId and verify it
     */
    @Test
    public void testCreateGetSearchDeletePodWithParentId() throws InterruptedException {
        RESTContextObject request = createRESTContextObject(POD_TYPE, "cisco.base.pod", null, REQUEST_ID);
        addDataElementsToRequest(request, "Context_Notes", "testing ParentId", "string");

        String requestBody = getGson().toJson(request);

        // do the create
        Response response = client
                .target(BASE_URL).request().accept(MediaType.APPLICATION_JSON_TYPE)
                .post(Entity.json(requestBody));

        assertEquals("should have succeeded", 201, response.getStatus());

        String id = SDKUtils.getIdFromUri(response.getLocation());

        //get activity
        RESTContextObject contextObject = getContextObject(id, POD_TYPE);
        assertEquals(REQUEST_ID, contextObject.getParentId().toString());

        // search  activity for parentId
        waitForSearchable("parentId", REQUEST_ID.toString(), POD_TYPE);
        Map<String, List<String>> query = new HashMap<>();
        query.put("parentId", Arrays.asList(REQUEST_ID.toString()));
        SearchParams searchParams = new SearchParams("or", query);
        requestBody = getGson().toJson(searchParams);

        Thread.sleep(2000);
        JsonArray objects = searchContextObject(requestBody, POD_TYPE);
        assertEquals("should be only 1", 1, objects.size());
        String parentId = objects.get(0).getAsJsonObject().get("parentId").getAsString();
        assertEquals("parentId should match", REQUEST_ID.toString(), parentId);

        // now delete it
        response = client.target(setBasePath(POD_TYPE)  + id).request().delete();
        assertEquals("should have succeeded", 202, response.getStatus());
    }


    /**
     * This creates the activity with both customerId and requestId
     * Get the activity and verify the customerId and parentId
     * Search for the activity that has bot customerId and requestId and verify it
     */
    @Test
    public void testCreateGetSearchDeletePodWithCustomerAndParentId() throws InterruptedException {
        RESTContextObject request = createRESTContextObject(POD_TYPE, "cisco.base.pod", CUSTOMER_ID, REQUEST_ID);
        addDataElementsToRequest(request, "Context_Notes", "testing ParentId", "string");

        String requestBody = getGson().toJson(request);

        // do the create
        Response response = client
                .target(BASE_URL).request().accept(MediaType.APPLICATION_JSON_TYPE)
                .post(Entity.json(requestBody));

        assertEquals("should have succeeded", 201, response.getStatus());

        String id = SDKUtils.getIdFromUri(response.getLocation());

        //get activity
        RESTContextObject contextObject = getContextObject(id, POD_TYPE);
        assertEquals(REQUEST_ID, contextObject.getParentId().toString());
        assertEquals(CUSTOMER_ID, contextObject.getCustomerId().toString());

        // search  activity for customerId and parentId
        waitForSearchable("parentId", REQUEST_ID.toString(), POD_TYPE);
        Map<String, List<String>> query = new HashMap<>();
        query.put("parentId", Arrays.asList(REQUEST_ID.toString()));
        query.put("customerId", Arrays.asList(CUSTOMER_ID.toString()));
        SearchParams searchParams = new SearchParams("and", query);
        requestBody = getGson().toJson(searchParams);

        Thread.sleep(2000);

        JsonArray objects = searchContextObject(requestBody, POD_TYPE);
        assertEquals("should be only 1", 1, objects.size());
        String parentId = objects.get(0).getAsJsonObject().get("parentId").getAsString();
        assertEquals("parentId should match", REQUEST_ID.toString(), parentId);
        String customerId = objects.get(0).getAsJsonObject().get("customerId").getAsString();
        assertEquals("customerId should match", CUSTOMER_ID.toString(), customerId);
        // now delete it
        response = client.target(setBasePath(POD_TYPE)  + id).request().delete();
        assertEquals("should have succeeded", 202, response.getStatus());
    }

    /**
     * Create a pod with dataElements containing fields with all 4 types (string, integer, double, and boolean).  Get and
     * verify created successfully with the appropriate types.  Update all 4 to verify that all 4 types can be updated.
     * Get and verify.  Delete field to cleanup.
     */
    @Test
    public void testCreateGetUpdateDeletePodAllDataElementTypes() {

        RESTContextObject request = createRESTContextObject(POD_TYPE, "cisco.test.custom", null, null);
        addDataElementsToRequest(request, "Context_Test_String", "test string, no type provided", null);
        addDataElementsToRequest(request, "Context_Test_Double", 23.45, "double");
        addDataElementsToRequest(request, "Context_Test_Boolean", true, "boolean");
        addDataElementsToRequest(request, "Context_Test_Integer", 19, "integer");

        String requestBody = getGson().toJson(request);

        // do the create
        Response response = client
                .target(BASE_URL).request().accept(MediaType.APPLICATION_JSON_TYPE)
                .post(Entity.json(requestBody));

        assertEquals("should have succeeded", 201, response.getStatus());
        String id = SDKUtils.getIdFromUri(response.getLocation());

        // get activity
        RESTContextObject contextObject = getContextObject(id, POD_TYPE);

        // assert dataElements added correctly
        verifyDataElements(contextObject, "Context_Test_String", "test string, no type provided", "string");
        verifyDataElements(contextObject, "Context_Test_Double", 23.45, "double");
        verifyDataElements(contextObject, "Context_Test_Boolean", true, "boolean");
        verifyDataElements(contextObject, "Context_Test_Integer", 19, "integer");

        updateDataElementsOnRequest(contextObject,"Context_Test_Double",25.34);
        updateDataElementsOnRequest(contextObject,"Context_Test_Boolean",false);
        updateDataElementsOnRequest(contextObject,"Context_Test_Integer",21);
        updateDataElementsOnRequest(contextObject,"Context_Test_String","test string, no type provided - updated");

        requestBody = getGson().toJson(contextObject);

        // do the update
        response = client.target(setBasePath(POD_TYPE) + id).request().accept(MediaType.APPLICATION_JSON_TYPE)
                .put(Entity.json(requestBody));

        assertEquals("should have succeeded", 202, response.getStatus());

        // get activity
        RESTContextObject updatedContextObject = getContextObject(id, POD_TYPE);

        // assert dataElements updated correctly
        verifyDataElements(updatedContextObject, "Context_Test_String", "test string, no type provided - updated", "string");
        verifyDataElements(updatedContextObject, "Context_Test_Double", 25.34, "double");
        verifyDataElements(updatedContextObject, "Context_Test_Boolean", false, "boolean");
        verifyDataElements(updatedContextObject, "Context_Test_Integer", 21, "integer");

        // now delete it
        response = client.target(setBasePath(POD_TYPE) + id).request().delete();
        assertEquals("should have succeeded", 202, response.getStatus());

    }

    /**
     * Waits for the data to be searchable.
     */
    private void waitForSearchable(String dataElement, String fieldData, String type) {
        doRetry("waiting for item to be searchable", 30, 1000, (Void v) -> {
            return getSearchResultCount(dataElement, fieldData, type)==1;
        });
    }

    /**
     * Simplistic retry logic.
     * @param message the thing to retry
     * @param count number of times to retry
     * @param timeBetweenRetries how long to wait (ms)
     * @param closure the function to execute
     */
    public static Boolean doRetry(String message, int count, long timeBetweenRetries, Function<Void, Boolean> closure) {
        for (int i=0; i<count; i++) {
            try {
                System.out.println(message + "; attempt " + i);
                if (closure.apply(null)) {
                    return true;
                }
            } catch (Exception e) {
                message = e.getMessage();
            }
            try {
                Thread.sleep(timeBetweenRetries);
            } catch (InterruptedException ignore) {
            }
        }
        return false;
    }

    /**
     * Returns the number of records that match the query.
     */
    private int getSearchResultCount(String dataElement, String fieldData, String type) {
        System.out.println("Searching for Context_Notes with value " + fieldData);

        Map<String, List<String>> query = new HashMap<>();
        query.put(dataElement, Arrays.asList(fieldData));
        SearchParams searchParams = new SearchParams("or", query);
        String requestBody = getGson().toJson(searchParams);
        Response response = client
                .target(setSearchBasePath(type)).request().accept(MediaType.APPLICATION_JSON_TYPE)
                .post(Entity.json(requestBody));

        assertEquals("should succeed", 200, response.getStatus());
        String entity = response.readEntity(String.class);

        JsonElement jsonResponse = getGson().fromJson(entity, JsonElement.class);
        JsonArray objects = jsonResponse.getAsJsonArray();
        return objects.size();
    }

    /**
     * Helper method to create a context object
     */
    private static String createContextObject(String type, String fieldset, String dataElement, Object fieldData, String customerId, String parentId) {
        RESTContextObject request = createRESTContextObject(type, fieldset, customerId, parentId);
        addDataElementsToRequest(request, dataElement, fieldData, fieldData.getClass().getSimpleName().toLowerCase());

        String requestBody = getGson().toJson(request);

        Response response = client
                .target(BASE_URL).request().accept(MediaType.APPLICATION_JSON_TYPE).post(Entity.json(requestBody));

        assertEquals("should have succeeded", 201, response.getStatus());

        return SDKUtils.getIdFromUri(response.getLocation());
    }

    /**
     * Helper method to delete a context object.
     */
    private static void deleteContextObject(String id, String type) {
        Response response = client.target(setBasePath(type) + id).request().delete(Response.class);
        assertEquals("should have succeeded", 202, response.getStatus());
    }

    private static RESTContextObject getContextObject(String id, String type) {
        Response response  = client.target(setBasePath(type)  + id).request().get();
        assertEquals("should succeed", 200, response.getStatus());
        String entity = response.readEntity(String.class);
        return getGson().fromJson(entity, RESTContextObject.class);

    }

    private static JsonArray searchContextObject(String requestBody, String type) {

        Response response = client
                .target(setSearchBasePath(type)).request().accept(MediaType.APPLICATION_JSON_TYPE)
                .post(Entity.json(requestBody));

        assertEquals("should succeed", 200, response.getStatus());
        String entity = response.readEntity(String.class);

        JsonElement jsonResponse = getGson().fromJson(entity, JsonElement.class);
        return jsonResponse.getAsJsonArray();
    }

    private static RESTContextObject createRESTContextObject(String type, String fieldset, String customerId, String parentId) {
        RESTContextObject request = new RESTContextObject();
        request.setType(type);
        request.setFieldsets(Arrays.asList(fieldset));
        if(parentId != null){
            request.setParentId(UUID.fromString(parentId));
        }
        if(customerId != null){
            request.setCustomerId(UUID.fromString(customerId));
        }
        return request;
    }

    private static Gson getGson() {
        return new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").create();
    }

    /**
     * Helper method to add a data element to a request.
     */
    private static void addDataElementsToRequest(RESTContextObject request, String key, Object value, String type) {
        List<RESTContextObject.ContextDataElement> dataElements = request.getDataElements();
        dataElements.add(new RESTContextObject.ContextDataElement(key, value, type));
    }

    /**
     * Helper method to update a data element on a request.
     */
    private static void updateDataElementsOnRequest(RESTContextObject request, String key, Object value) {
        List<RESTContextObject.ContextDataElement> dataElements = request.getDataElements();
        for(RESTContextObject.ContextDataElement contextDataElement : dataElements) {
            if(contextDataElement.getKey().equalsIgnoreCase(key)) {
                contextDataElement.setValue(value);
            }
        }
    }

    /**
     * Helper method to verify a data elements on a request.
     */
    private static void verifyDataElements(RESTContextObject request, String key, Object value, String type) {
        Boolean foundDataElementKey = false;
        List<RESTContextObject.ContextDataElement> dataElements = request.getDataElements();
        for(RESTContextObject.ContextDataElement contextDataElement : dataElements) {
            if(contextDataElement.getKey().equalsIgnoreCase(key)) {
                foundDataElementKey = true;
                assertEquals("type should match",type, contextDataElement.getType());
                if(type.equalsIgnoreCase("integer")) {
                    //gson conversion has changed the integer back to a double. Fix it and compare the value.
                    Double dataElementValue = (Double)contextDataElement.getValue();
                    assertEquals("value should match",value, Integer.valueOf(dataElementValue.intValue()));
                }else {
                    assertEquals("value should match",value, contextDataElement.getValue());
                }
            }
        }
        assertTrue("dataElement key: " + key + " should have been in dataElements", foundDataElementKey);
    }

    private static class LoggingFilter implements ClientRequestFilter {

        @Override
        public void filter(ClientRequestContext requestContext) throws IOException {
            if (requestContext.getEntity()!=null) {
                System.out.println(requestContext.getEntity().toString());
            }
        }
    }

    private static final String setBasePath(String type) {
        return String.format(GET_BASE_URL, type);
    }

    private static final String setSearchBasePath(String type) {
        return String.format(SEARCH_BASE_URL, type);
    }
}
