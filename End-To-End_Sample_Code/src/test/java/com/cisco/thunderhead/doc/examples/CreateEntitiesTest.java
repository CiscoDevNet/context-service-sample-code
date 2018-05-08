package com.cisco.thunderhead.doc.examples;

import com.cisco.thunderhead.ContextObject;
import com.cisco.thunderhead.util.DataElementUtils;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class CreateEntitiesTest extends BaseExamplesTest {

    @Test
    public void testCreateDetailCommentWithBaseFieldset() {

        ContextObject customer = CreateEntities.createCustomerWithBaseFieldset(contextServiceClient);
        ContextObject pod = CreateEntities.createPodWithCustomer(contextServiceClient, customer);
        ContextObject detailComment = CreateEntities.createDetailCommentWithBaseFieldset(contextServiceClient, pod);
        assertNotNull(detailComment.getId());
        String contextComment = (String) DataElementUtils.convertDataSetToMap(detailComment.getDataElements()).get("Context_Comment");
        assertEquals("Detailed context comment.", contextComment);
    }

    @Test
    public void testCreateDetailFeedbackWithBaseFieldset() {

        ContextObject customer = CreateEntities.createCustomerWithBaseFieldset(contextServiceClient);
        ContextObject pod = CreateEntities.createPodWithCustomer(contextServiceClient, customer);
        ContextObject detailComment = CreateEntities.createDetailFeedbackWithBaseFieldset(contextServiceClient, pod);
        assertNotNull(detailComment.getId());
        String contextComment = (String) DataElementUtils.convertDataSetToMap(detailComment.getDataElements()).get("Context_Comment");
        assertEquals("Detailed context feedback.", contextComment);
    }


    @Test
    public void testCreatePodWithBaseFieldset() {
        ContextObject pod = CreateEntities.createPodWithBaseFieldset(contextServiceClient);
        assertNotNull(pod.getId());
        String contextNotes = (String) DataElementUtils.convertDataSetToMap(pod.getDataElements()).get("Context_Notes");
        assertEquals("Notes about this context.", contextNotes);
    }

    @Test
    public void testCreateCustomerWithBaseFieldset() {
        ContextObject customer = CreateEntities.createCustomerWithBaseFieldset(contextServiceClient);
        assertNotNull(customer.getId());
        String customerEmail = (String) DataElementUtils.convertDataSetToMap(customer.getDataElements()).get("Context_Work_Email");
        assertEquals("john.doe@example.com", customerEmail);
    }

    @Test
    public void testCreateRequestWithBaseFieldset() {
        ContextObject customer = CreateEntities.createCustomerWithBaseFieldset(contextServiceClient);
        ContextObject request = CreateEntities.createRequestWithBaseFieldset(contextServiceClient, customer);
        assertNotNull(request.getId());
        String contextTitle = (String) DataElementUtils.convertDataSetToMap(request.getDataElements()).get("Context_Title");
        assertEquals("Request1 Title", contextTitle);
    }

    @Test
    public void testCreatePodWithCustomer() {
        ContextObject customer = CreateEntities.createCustomerWithBaseFieldset(contextServiceClient);
        ContextObject pod = CreateEntities.createPodWithCustomer(contextServiceClient, customer);
        assertNotNull(pod.getId());
        assertEquals(customer.getId(), pod.getCustomerId());
    }

    @Test
    public void testCreatePodWithRequest() {
        ContextObject customer = CreateEntities.createCustomerWithBaseFieldset(contextServiceClient);
        ContextObject request = CreateEntities.createRequestWithBaseFieldset(contextServiceClient, customer);
        ContextObject pod = CreateEntities.createPodWithRequest(contextServiceClient, request);
        assertNotNull(pod.getId());
        assertEquals(request.getId(), pod.getParentId());
    }

    @Test
    public void testCreatePodWithCustomerAndRequest() {
        ContextObject customer = CreateEntities.createCustomerWithBaseFieldset(contextServiceClient);
        ContextObject request = CreateEntities.createRequestWithBaseFieldset(contextServiceClient, customer);
        ContextObject pod = CreateEntities.createPodWithCustomerAndRequest(contextServiceClient, customer, request);
        assertNotNull(pod.getId());
        assertEquals(customer.getId(), pod.getCustomerId());
        assertEquals(request.getId(), pod.getParentId());
    }

    @Test
    public void testCreateMultiplePodsWithSameCustomer() {
        ContextObject customer = CreateEntities.createCustomerWithBaseFieldset(contextServiceClient);
        List<ContextObject> pods = CreateEntities.createMultiplePodsWithSameCustomer(contextServiceClient, customer);
        for (ContextObject pod: pods) {
            assertNotNull(pod.getId());
            assertEquals(customer.getId(), pod.getCustomerId());
        }
    }

    @Test
    public void testCreateMultiplePodsWithSameRequest() {
        ContextObject customer = CreateEntities.createCustomerWithBaseFieldset(contextServiceClient);
        ContextObject request = CreateEntities.createRequestWithBaseFieldset(contextServiceClient, customer);
        List<ContextObject> pods = CreateEntities.createMultiplePodsWithSameRequest(contextServiceClient, request);
        for (ContextObject pod: pods) {
            assertNotNull(pod.getId());
            assertEquals(request.getId(), pod.getParentId());
        }
    }
}
