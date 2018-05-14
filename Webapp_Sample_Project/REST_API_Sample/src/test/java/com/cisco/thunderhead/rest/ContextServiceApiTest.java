package com.cisco.thunderhead.rest;

import com.cisco.thunderhead.util.RFC3339Date;
import com.cisco.thunderhead.util.SDKUtils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
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
import java.lang.reflect.Type;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

import static org.junit.Assert.assertEquals;

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
    private static UUID CUSTOMER_ID;
    private static UUID REQUEST_ID;
    private static String POD_TYPE ="pod";
    private static String CUSTOMER_TYPE ="customer";
    private static String REQUEST_TYPE ="request";

    /**
     * Setup.  Create a dummy context object used by tests in this class.
     */
    @BeforeClass
    public static void setUp() {
        client = ClientBuilder.newClient();
        client.register(new LoggingFilter()); // so we can see HTTP traffic
        FIELD_DATA = UUID.randomUUID().toString();
        CUSTOMER_ID = UUID.fromString(createContextObject(CUSTOMER_TYPE, "cisco.base.customer", "Context_Home_Phone","123-456-7890",null, null));
        REQUEST_ID = UUID.fromString(createContextObject(REQUEST_TYPE, "cisco.base.request", "Context_Description","testing CustomerId", CUSTOMER_ID, null));
        ACTIVITY_ID = createContextObject(POD_TYPE,"cisco.base.pod", "Context_Notes",FIELD_DATA, null, null);

    }

    /**
     * Cleanup.
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
        RESTContextObject request = createRequest(POD_TYPE, "cisco.base.pod", null, null);
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
     * This creates the activity with customerId
     * Get the activity and verify the customerId and parentId
     * Search for the activity that has bot customerId and requestId and verify it
     */
    @Test
    public void testCreateGetSearchDeletePodWithCustomerId() {
        RESTContextObject request = createRequest(POD_TYPE, "cisco.base.pod", CUSTOMER_ID, null);
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
        assertEquals(CUSTOMER_ID, contextObject.getCustomerId());

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
     * This creates the activity with requestId
     * Get the activity and verify the customerId and parentId
     * Search for the activity that has bot customerId and requestId and verify it
     */
    @Test
    public void testCreateGetSearchDeletePodWithParentId() {
        RESTContextObject request = createRequest(POD_TYPE, "cisco.base.pod", null, REQUEST_ID);
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
        assertEquals(REQUEST_ID, contextObject.getParentId());

        // search  activity for parentId
        waitForSearchable("parentId", REQUEST_ID.toString(), POD_TYPE);
        Map<String, List<String>> query = new HashMap<>();
        query.put("parentId", Arrays.asList(REQUEST_ID.toString()));
        SearchParams searchParams = new SearchParams("or", query);
        requestBody = getGson().toJson(searchParams);
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
    public void testCreateGetSearchDeletePodWithCustomerAndParentId() {
        RESTContextObject request = createRequest(POD_TYPE, "cisco.base.pod", CUSTOMER_ID, REQUEST_ID);
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
        assertEquals(REQUEST_ID, contextObject.getParentId());
        assertEquals(CUSTOMER_ID, contextObject.getCustomerId());

        // search  activity for customerId and parentId
        waitForSearchable("parentId", REQUEST_ID.toString(), POD_TYPE);
        Map<String, List<String>> query = new HashMap<>();
        query.put("parentId", Arrays.asList(REQUEST_ID.toString()));
        query.put("customerId", Arrays.asList(CUSTOMER_ID.toString()));
        SearchParams searchParams = new SearchParams("and", query);
        requestBody = getGson().toJson(searchParams);
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
    private static String createContextObject(String type, String fieldset, String dataElement, String fieldData, UUID customerId, UUID parentId) {
        RESTContextObject request = createRequest(type, fieldset, customerId, parentId);
        addDataElementsToRequest(request, dataElement, fieldData, "string");

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

    private static RESTContextObject createRequest(String type, String fieldset, UUID customerId, UUID parentId) {
        RESTContextObject request = new RESTContextObject();
        request.setType(type);
        request.setFieldsets(Arrays.asList(fieldset));
        if(parentId != null){
            request.setParentId( parentId);
        }
        if(customerId != null){
            request.setCustomerId( customerId);
        }
        return request;
    }

    private static Gson getGson() {
        Gson gson = new GsonBuilder().registerTypeAdapter(Date.class, new JsonDeserializer<Date>() {
            @Override
            public Date deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
                String datestr = json.getAsString();
                try {
                    RFC3339Date date = new RFC3339Date(datestr);
                    return date.getDate();
                } catch (ParseException e) {
                    throw new RuntimeException("couldn't parse date: " + datestr);
                }
            }
        }).create();
        return gson;
    }



    /**
     * Helper method to add a data element to a request.
     */
    private static void addDataElementsToRequest(RESTContextObject request, String key, String value, String type) {
        List<RESTContextObject.ContextDataElement> dataElements = request.getDataElements();
        dataElements.add(new RESTContextObject.ContextDataElement(key, value, type));
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
