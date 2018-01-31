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
    private static Client client;
    private static String CONTEXT_OBJECT_ID;
    private static String FIELD_DATA;

    /**
     * Setup.  Create a dummy context object used by tests in this class.
     */
    @BeforeClass
    public static void setUp() {
        client = ClientBuilder.newClient();
        client.register(new LoggingFilter()); // so we can see HTTP traffic
        FIELD_DATA = UUID.randomUUID().toString();

        CONTEXT_OBJECT_ID = createContextObject(FIELD_DATA);
    }

    /**
     * Cleanup.
     */
    @AfterClass
    public static void tearDown() {
        deleteContextObject(CONTEXT_OBJECT_ID);
    }

    /**
     * This creates the request.  It re-uses the server-side RESTContextObject to make creating the request easier.
     */
    @Test
    public void testCreate() {
        RESTContextObject request = createRequest("pod", "cisco.base.pod");
        addDataElementsToRequest(request, "Context_Notes", "testing at 3:16", "string");

        String requestBody = getGson().toJson(request);

        // do the create
        Response response = client
                .target(BASE_URL).request().accept(MediaType.APPLICATION_JSON_TYPE)
                .post(Entity.json(requestBody));

        assertEquals("should have succeeded", 201, response.getStatus());
        String id = SDKUtils.getIdFromUri(response.getLocation());

        // now delete it
        response = client.target(BASE_URL + "/pod/" + id).request().delete();
        assertEquals("should have succeeded", 202, response.getStatus());
    }

    /**
     * Tests retrieve.
     */
    @Test
    public void testGet() {
        Response response;

        // failure case
        response = client.target(BASE_URL + "/pod/" + CONTEXT_OBJECT_ID + "blah").request().get();
        assertEquals("should succeed", 500, response.getStatus());

        // success case
        response = client.target(BASE_URL + "/pod/" + CONTEXT_OBJECT_ID).request().get();
        assertEquals("should succeed", 200, response.getStatus());
        String entity = response.readEntity(String.class);

        RESTContextObject RESTContextObject = getGson().fromJson(entity, RESTContextObject.class);
        assertEquals("unexpected contents", 1, RESTContextObject.getDataElements().size());
        assertEquals("unexpected contents", "Context_Notes", RESTContextObject.getDataElements().get(0).getKey());
        assertEquals("unexpected contents", FIELD_DATA, RESTContextObject.getDataElements().get(0).getValue());
    }

    /**
     * Tests searching.
     */
    @Test
    public void testSearch() {
        waitForSearchable(FIELD_DATA);
        Map<String, List<String>> query = new HashMap<>();
        query.put("Context_Notes", Arrays.asList(FIELD_DATA));
        SearchParams searchParams = new SearchParams("or", query);
        String requestBody = getGson().toJson(searchParams);
        Response response = client
                .target(BASE_URL + "/search/pod").request().accept(MediaType.APPLICATION_JSON_TYPE)
                .post(Entity.json(requestBody));

        assertEquals("should succeed", 200, response.getStatus());
        String entity = response.readEntity(String.class);

        JsonElement jsonResponse = getGson().fromJson(entity, JsonElement.class);
        JsonArray objects = jsonResponse.getAsJsonArray();
        assertEquals("should be only 1", 1, objects.size());
        JsonArray dataElements = objects.get(0).getAsJsonObject().get("dataElements").getAsJsonArray();
        assertEquals("wrong field data", FIELD_DATA, dataElements.get(0).getAsJsonObject().get("value").getAsString());
    }

    /**
     * Waits for the data to be searchable.
     */
    private void waitForSearchable(String fieldData) {
        doRetry("waiting for item to be searchable", 30, 1000, (Void v) -> {
            return getSearchResultCount(fieldData, "pod")==1;
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
    private int getSearchResultCount(String fieldData, String type) {
        System.out.println("Searching for Context_Notes with value " + fieldData);

        Map<String, List<String>> query = new HashMap<>();
        query.put("Context_Notes", Arrays.asList(fieldData));
        SearchParams searchParams = new SearchParams("or", query);
        String requestBody = getGson().toJson(searchParams);
        Response response = client
                .target(BASE_URL + "/search/" + type).request().accept(MediaType.APPLICATION_JSON_TYPE)
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
    private static String createContextObject(String fieldData) {
        RESTContextObject request = createRequest("pod", "cisco.base.pod");
        addDataElementsToRequest(request, "Context_Notes", fieldData, "string");

        String requestBody = getGson().toJson(request);
        Response response = client
                .target(BASE_URL).request().accept(MediaType.APPLICATION_JSON_TYPE).post(Entity.json(requestBody));

        assertEquals("should have succeeded", 201, response.getStatus());

        return SDKUtils.getIdFromUri(response.getLocation());
    }

    /**
     * Helper method to delete a context object.
     */
    private static void deleteContextObject(String id) {
        Response response = client.target(BASE_URL + "/pod/" + id).request().delete(Response.class);
        assertEquals("should have succeeded", 202, response.getStatus());
    }

    private static RESTContextObject createRequest(String type, String fieldset) {
        RESTContextObject request = new RESTContextObject();
        request.setType(type);
        request.setFieldsets(Arrays.asList(fieldset));
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
}
