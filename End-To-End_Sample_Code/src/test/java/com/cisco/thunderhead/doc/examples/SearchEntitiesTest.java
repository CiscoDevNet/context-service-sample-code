package com.cisco.thunderhead.doc.examples;

import com.cisco.thunderhead.ContextObject;
import com.cisco.thunderhead.client.ClientResponse;
import com.cisco.thunderhead.datatypes.PodMediaType;
import com.cisco.thunderhead.datatypes.PodState;
import com.cisco.thunderhead.dictionary.Field;
import com.cisco.thunderhead.dictionary.FieldSet;
import com.cisco.thunderhead.request.Request;
import com.cisco.thunderhead.tag.Tag;
import com.cisco.thunderhead.util.DataElementUtils;
import com.cisco.thunderhead.util.SDKUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

import static java.lang.Thread.sleep;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class SearchEntitiesTest extends BaseExamplesTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(SearchEntitiesTest.class);

    // Used to store and validate UUIDs.
    private static String custId1;
    private static String custId2;
    private static String custId3;
    private static String reqId1;
    private static String podId1;
    private static String podId2;
    private static String podId3;
    private static String podId4;
    private static String podId5;
    private static Set<Tag> tagSet1;
    private static Set<Tag> tagSet2;
    private static Set<Tag> tagSet3;
    private static Set<Tag> tagSet4;
    private static FieldSet customFieldSet;

    @BeforeClass
    public static void createDataToSearch() {
        // Create a custom fieldSet
        deleteExistingFieldAndFieldSet();
        customFieldSet = FieldSets.createFieldSet(contextServiceClient, SearchEntities.SEARCH_UNDERSCORE);

        // Create some tags to add to the POD
        tagSet1 = new HashSet<Tag>((Arrays.asList(new Tag("issue"), new Tag("major"), new Tag("preferred-customer"))));
        tagSet2 = new HashSet<Tag>((Arrays.asList(new Tag("issue"), new Tag("sales"))));
        tagSet3 = new HashSet<Tag>((Arrays.asList(new Tag("issue"), new Tag("major"), new Tag("marketing"))));
        tagSet4 = new HashSet<Tag>((Arrays.asList(new Tag("cancellation"))));

        ClientResponse response = createCustomer("John", "Doe", "111-111-1111", "gold");
        custId1 = validateClientResponseAndReturnId(response, 201);
        response = createCustomer("Jane", "Doe", "111-111-2222", "silver");
        custId2 = validateClientResponseAndReturnId(response, 201);
        response = createCustomer("Jane", "Smith", "111-111-3333", "bronze");
        custId3 = validateClientResponseAndReturnId(response, 201);

        response = createRequest("Title 1", "This is a request.", custId1);
        reqId1 = validateClientResponseAndReturnId(response, 201);

        response = createPod("This is first pod.", "Juan.Important@customer.com", "111-111-1111", "apple", tagSet1, custId1, reqId1);
        podId1 = validateClientResponseAndReturnId(response, 201);
        response = createPod("This is second pod.", "Ivanna.Buy@prospect.com", "222-222-2222", "banana", tagSet2, custId1, null);
        podId2 = validateClientResponseAndReturnId(response, 201);
        response = createPod("This is third pod.", "Selma.Adds@tvspot.com", "333-333-3333", "cherry", tagSet3, custId1, reqId1);
        podId3 = validateClientResponseAndReturnId(response, 201);
        response = createPod("This is fourth pod.", "Anita.Little.Help@support.com", "444-444-4444", "date", null, custId2, null);
        podId4 = validateClientResponseAndReturnId(response, 201);
        response = createPod("This is fifth pod.", "Will.Du@happy.com", "555-555-5555", "eggplant", null, custId2, null);
        podId5 = validateClientResponseAndReturnId(response, 201);

        try {
            sleep(2000);    // wait 2 seconds to allow data to propagate
        } catch (InterruptedException e) {
            fail("sleep interrupted");
        }
    }

    @AfterClass
    public static void deleteDataThatIsNotFlushed() {
        try {
            FlushEntities.flushAllEntities(contextServiceClient);
        }catch(InterruptedException e) {
            fail("flush failed. Exception caught: " + e.toString());
        }catch(TimeoutException e) {
            fail("flush failed. Exception caught: " + e.toString());
        }
        deleteExistingFieldAndFieldSet();
    }

    @Test
    public void testSearchForCustomerById () {

        // Search for each by ID
        List<ContextObject> customersFound = SearchEntities.searchForCustomerById(contextServiceClient, custId1);
        assertEquals("Error: Should find one and only one customer.", 1, customersFound.size());
        assertTrue("Error: Should have found customer custId1.", checkListForCustomer(customersFound, custId1));

        customersFound = SearchEntities.searchForCustomerById(contextServiceClient, custId2);
        assertEquals("Error: Should find one and only one customer.", 1, customersFound.size());
        assertTrue("Error: Should have found customer custId2.", checkListForCustomer(customersFound, custId2));
    }

    @Test
    public void testSearchForCustomerByFirstAndLastName () {
        // Search for customer Jane Doe.
        List<ContextObject> customersFound = SearchEntities.searchForCustomerByFirstAndLastName(contextServiceClient);
        assertEquals("Error: Should find one and only one customer.", 1, customersFound.size());
        assertTrue("Error: Should have found customer custId2.", checkListForCustomer(customersFound, custId2));
    }

    @Test
    public void testSearchForCustomerByFirstOrLastName () {
        // Search for any customer with first name "Jane" or last name "Doe"
        List<ContextObject> customersFound = SearchEntities.searchForCustomerByFirstOrLastName(contextServiceClient);
        assertEquals("Error: Should find exactly three customers.", 3, customersFound.size());
        assertTrue("Error: Should have found customer custId1.", checkListForCustomer(customersFound, custId1));
        assertTrue("Error: Should have found customer custId2.", checkListForCustomer(customersFound, custId2));
        assertTrue("Error: Should have found customer custId3.", checkListForCustomer(customersFound, custId3));
    }

    @Test
    public void testSearchForCustomerByGoldOrSilver () {
        // Search for any customer with sdkExample_fieldOne value of "gold" or "silver"
        List<ContextObject> customersFound = SearchEntities.searchForCustomerByGoldOrSilver(contextServiceClient);
        assertEquals("Error: Should find exactly two customers.", 2, customersFound.size());
        assertTrue("Error: Should have found customer custId1.", checkListForCustomer(customersFound, custId1));
        assertTrue("Error: Should have found customer custId2.", checkListForCustomer(customersFound, custId2));
    }

    @Test
    public void testSearchForPodsByQueryString () {
        List<ContextObject> podsFound = SearchEntities.searchForPodsByQueryString(contextServiceClient);
        assertEquals("Should find one POD", 1, podsFound.size());
        assertTrue("Should have found POD 2", checkListForPod(podsFound, podId2));
    }

    @Test
    public void testSearchForPodById () {

        // Search for each by ID
        List<ContextObject> podsFound = SearchEntities.searchForPodById(contextServiceClient, podId1);
        assertEquals("Error: Should find one and only one pod.", 1, podsFound.size());
        assertTrue("Error: Should have found pod podId1.", checkListForPod(podsFound, podId1));

        podsFound = SearchEntities.searchForPodById(contextServiceClient, podId2);
        assertEquals("Error: Should find one and only one pod.", 1, podsFound.size());
        assertTrue("Error: Should have found pod podId2.", checkListForPod(podsFound, podId2));
    }

    @Test
    public void testSearchForPodByCustomerId () {

        // Search for Pods by CustomerID
        List<ContextObject> podsFound = SearchEntities.searchForPodByCustomerId(contextServiceClient, custId1);
        assertEquals("Error: Should find exactly three pods.", 3, podsFound.size());
        assertTrue("Error: Should have found pod podId1.", checkListForPod(podsFound, podId1));
        assertTrue("Error: Should have found pod podId2.", checkListForPod(podsFound, podId2));
        assertTrue("Error: Should have found pod podId3.", checkListForPod(podsFound, podId3));

        podsFound = SearchEntities.searchForPodByCustomerId(contextServiceClient, custId2);
        assertEquals("Error: Should find exactly two pods.", 2, podsFound.size());
        assertTrue("Error: Should have found pod podId4.", checkListForPod(podsFound, podId4));
        assertTrue("Error: Should have found pod podId5.", checkListForPod(podsFound, podId5));
    }

    @Test
    public void testSearchForPodByRequestId () {

        // Search for Pods by RequestID
        List<ContextObject> podsFound = SearchEntities.searchForPodByRequestId(contextServiceClient, reqId1);
        assertEquals("Error: Should find exactly two pods.", 2, podsFound.size());
        assertTrue("Error: Should have found pod podId1.", checkListForPod(podsFound, podId1));
        assertTrue("Error: Should have found pod podId3.", checkListForPod(podsFound, podId3));
    }

    @Test
    public void testSearchForPodByRListOfIds () {
        List<String> idList = new ArrayList<String>(){{
            add(podId1);
            add(podId3);
            add(podId5);
        }};

        // Search for Pods by RequestID
        List<ContextObject> podsFound = SearchEntities.searchForPodByListOfIds(contextServiceClient, idList);
        assertEquals("Error: Should find exactly three pods.", 3, podsFound.size());
        assertTrue("Error: Should have found pod podId1.", checkListForPod(podsFound, podId1));
        assertTrue("Error: Should have found pod podId3.", checkListForPod(podsFound, podId3));
        assertTrue("Error: Should have found pod podId5.", checkListForPod(podsFound, podId5));
    }

    @Test
    public void testSearchForPodsTaggedAsMajorIssueForPreferredCustomer () {
        List<ContextObject> podsFound = SearchEntities.searchForPodsTaggedAsMajorIssueForPreferredCustomer(contextServiceClient);
        assertEquals("Error: Should find one and only one pod.", 1, podsFound.size());
        assertEquals(podId1, podsFound.get(0).getId().toString());
    }

    @Test
    public void testSearchForPodsTaggedAsSalesOrMarketing () {
        List<ContextObject> podsFound = SearchEntities.searchForPodsTaggedAsSalesOrMarketing(contextServiceClient);
        assertEquals("Error: Should find exactly two pods.", 2, podsFound.size());
        assertTrue("Error: Should have found pod podId2.", checkListForPod(podsFound, podId2));
        assertTrue("Error: Should have found pod podId3.", checkListForPod(podsFound, podId3));
    }

    @Test
    public void testSearchForPodsBySourceEmailOrSourcePhone () {
        List<ContextObject> podsFound = SearchEntities.searchForPodsBySourceEmailOrSourcePhone(contextServiceClient);
        assertEquals("Error: Should find one and only one pod.", 1, podsFound.size());
        assertTrue("Error: Should have found pod podId1.", checkListForPod(podsFound, podId1));
    }

    @Test
    public void testSearchForPodsByNullRequestIdValue () {
        List<ContextObject> podsFound = SearchEntities.searchForPodsByNullParentIdValue(contextServiceClient);
        //make sure the returned list contains the original created pod without parentId
        assertTrue("Error: Should have found pod podId2.", checkListForPod(podsFound, podId2));
        assertTrue("Error: Should have found pod podId2.", checkListForPod(podsFound, podId4));
        assertTrue("Error: Should have found pod podId2.", checkListForPod(podsFound, podId5));
    }

    @Test
    public void testSearchForPodByDateRanges () {
        // Create two pods (A and B)
        ClientResponse response = createPod("Pod A", "poda@example.com", "", "apple", null, null, null);
        String podA = validateClientResponseAndReturnId(response, 201);
        // Get the time.
        // Note that we get the time from the response object, which reflects the time on the server (and
        // thus, the time on the object).  If we were to use local time here, it is very possible to
        // get unexpected results and test failure (for example, if the server pod create date is
        // BEFORE the local requested start date, because the server is running behind the local machine).
        long time1 = SDKUtils.getLastUpdatedFromResponse(response).getDate().getTime();

        response = createPod("Pod B", "podb@example.com", "", "banana", tagSet4, null, null);
        String podB = validateClientResponseAndReturnId(response, 201);

        // Wait 15 seconds
        try {
            sleep(15000);
        } catch (InterruptedException e) {
            fail("sleep interrupted");
        }

        // Create another pod (C) and update pod A
        response = createPod("Pod C", "podc@example.com", "", "cherry", null, null, null);
        String podC = validateClientResponseAndReturnId(response, 201);
        response = updatePod(podA, PodMediaType.VOICE);
        validateClientResponseAndReturnId(response, 200);

        // Wait 5 seconds
        try {
            sleep(5000);
        } catch (InterruptedException e) {
            fail("sleep interrupted");
        }

        // Search for pods created in the first 10 second window.  Should return A and B.
        List<ContextObject> podsFound = SearchEntities.searchForPodsByCreateDateRange(contextServiceClient, time1, time1 + 10000);
        assertEquals("Error: Should find exactly two pods.", 2, podsFound.size());
        assertTrue("Error: Should have found pod podA.", checkListForPod(podsFound, podA));
        assertTrue("Error: Should have found pod podB.", checkListForPod(podsFound, podB));

        // Search for pods created in the second 10 second window.  Should return C only.
        podsFound = SearchEntities.searchForPodsByCreateDateRange(contextServiceClient, time1 + 10000, time1 + 20000);
        assertEquals("Error: Should find one and only one pod.", 1, podsFound.size());
        assertEquals(podC, podsFound.get(0).getId().toString());
        assertTrue("Error: Should have found pod podC.", checkListForPod(podsFound, podC));

        // Search for pods updated in the second 10 second window.  Should return A and C.
        podsFound = SearchEntities.searchForPodsByLastUpdatedDateRange(contextServiceClient, time1 + 10000, time1 + 20000);
        assertEquals("Error: Should find exactly two pods.", 2, podsFound.size());
        assertTrue("Error: Should have found pod podA.", checkListForPod(podsFound, podA));
        assertTrue("Error: Should have found pod podC.", checkListForPod(podsFound, podC));

        // Search for pods created in the first 10 second window that has custom field "banana".  Should return just B.
        podsFound = SearchEntities.searchForPodsByCustomFieldAndDateRangeAndTag(contextServiceClient, "banana", time1, time1 + 10000);
        assertEquals("Error: Should find one and only one pod.", 1, podsFound.size());
        assertTrue("Error: Should have found pod podB.", checkListForPod(podsFound, podB));
    }

    @Test
    public void testSearchForActivePods() {
        ClientResponse response = createPodWithState(PodState.CLOSED);
        String closedPodId = validateClientResponseAndReturnId(response, 201);

        List<ContextObject> results = SearchEntities.searchForActivePods(contextServiceClient);
        assertTrue(checkListForPod(results, podId1));
        assertTrue(checkListForPod(results, podId2));
        assertTrue(checkListForPod(results, podId3));
        assertFalse(checkListForPod(results, closedPodId));
    }

    // UTILITY METHODS
    private static ClientResponse createCustomer(final String firstName, final String lastName, final String phoneNumber, final String customField) {
        ContextObject customer = new ContextObject(ContextObject.Types.CUSTOMER);
        customer.setDataElements(
                DataElementUtils.convertDataMapToSet(
                        new HashMap<String, Object>() {{
                            put("Context_Work_Email", firstName + "." + lastName + "@example.com");
                            put("Context_Work_Phone", phoneNumber);
                            put("Context_First_Name", firstName);
                            put("Context_Last_Name", lastName);
                            put("Context_Street_Address_1", "123 Sesame Street");
                            put("Context_City", "Detroit");
                            put("Context_State", "MI");
                            put("Context_Country", "US");
                            put("Context_ZIP", "90210");
                            put(SearchEntities.FIELD_ONE, customField);
                        }}
                )
        );
        customer.setFieldsets(Arrays.asList("cisco.base.customer", customFieldSet.getId()));
        return contextServiceClient.create(customer);
    }

    private static ClientResponse createRequest(final String title, final String description, String customerId) {
        ContextObject request = new ContextObject(ContextObject.Types.REQUEST);
        request.setDataElements(
                DataElementUtils.convertDataMapToSet(
                        new HashMap<String, Object>() {{
                            put("Context_Title", title);
                            put("Context_Description", description);
                        }})
        );
        request.setFieldsets(Arrays.asList("cisco.base.request"));
        request.setCustomerId(UUID.fromString(customerId));
        return contextServiceClient.create(request);
    }

    private static ClientResponse createPod(final String notes, final String sourceEmail, final String sourcePhone,
                                            final String customField, Set<Tag> tags,
                                            final String customerId, final String requestId) {
        ContextObject pod = new ContextObject(ContextObject.Types.POD);
        pod.setDataElements(
                DataElementUtils.convertDataMapToSet(
                        new HashMap<String, Object>() {{
                            put("Context_Notes", notes);
                            put("Context_POD_Source_Email", sourceEmail);
                            put("Context_POD_Source_Phone", sourcePhone);
                            put("Context_POD_Activity_Link", "http://myservice.example.com/service/ID/xxxx");
                            put(SearchEntities.FIELD_ONE, customField);
                        }}
                )
        );
        pod.setFieldsets(Arrays.asList("cisco.base.pod", customFieldSet.getId()));
        if (tags != null) {
            pod.setTags(tags);
        }
        if (customerId != null) {
            try {
                UUID custId = UUID.fromString(customerId);
                pod.setCustomerId(custId);
            } catch (IllegalArgumentException e) {
                // Do nothing if bad or nonexistent customerId
            }
        }
        if (requestId != null) {
            try {
                UUID reqId = UUID.fromString(requestId);
                pod.setParentId(reqId);
            } catch (IllegalArgumentException e) {
                // Do nothing if bad or nonexistent requestId
            }
        }

        return contextServiceClient.create(pod);
    }

    private static ClientResponse createPodWithState(String state) {
        ContextObject pod = new ContextObject(ContextObject.Types.POD);
        pod.setDataElements(
                DataElementUtils.convertDataMapToSet(
                        new HashMap<String, Object>() {{
                            put("Context_Notes", "Notes about this context.");
                        }}
                )
        );
        pod.setFieldsets(Arrays.asList("cisco.base.pod"));
        pod.setState(state);
        return contextServiceClient.create(pod);
    }

    private static ClientResponse updatePod(String podId, String mediaType) {
        UUID uuid = UUID.fromString(podId);
        ContextObject pod = GetEntities.getPod(contextServiceClient, uuid);
        pod.setMediaType(mediaType);

        return contextServiceClient.update(pod);
    }


    private static void dumpPod(String podId) {
        UUID uuid = UUID.fromString(podId);
        ContextObject pod = GetEntities.getPod(contextServiceClient, uuid);

        LOGGER.info("Pod: " + pod.toString());
    }

    private static void deleteExistingFieldAndFieldSet() {

        List<FieldSet> list = FieldSets.searchFieldSet(contextServiceClient, SearchEntities.SEARCH_UNDERSCORE);
        for(FieldSet fieldSet : list){
            if(fieldSet.getId().equals(SearchEntities.FIELDSET)){
                contextServiceClient.delete(fieldSet);
            }
        }

        List<Field> fieldlist = FieldSets.searchField(contextServiceClient, SearchEntities.SEARCH_UNDERSCORE);
        for(Field field : fieldlist){
            if(field.getId().equals(SearchEntities.FIELD_ONE) || field.getId().equals(SearchEntities.FIELD_TWO) || field.getId().equals(SearchEntities.FIELD_THREE)){
                contextServiceClient.delete(field);
            }
        }
    }

    private static boolean checkListForCustomer(List<ContextObject> customers, String id) {
        for (ContextObject customer : customers) {
            if (customer.getId().toString().equals(id)) {
                return true;
            }
        }
        return false;
    }

    private static boolean checkListForPod(List<ContextObject> pods, String id) {
        for (ContextObject pod : pods) {
            if (pod.getId().toString().equals(id)) {
                return true;
            }
        }
        return false;
    }
}
