package com.cisco.thunderhead.doc.examples;

import com.cisco.thunderhead.customer.Customer;
import com.cisco.thunderhead.pod.Pod;
import com.cisco.thunderhead.request.Request;
import com.cisco.thunderhead.util.DataElementUtils;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class CreateEntitiesTest extends BaseExamplesTest {

    @Test
    public void testCreatePodWithBaseFieldset() {
        Pod pod = CreateEntities.createPodWithBaseFieldset(contextServiceClient);
        assertNotNull(pod.getId());
        String contextNotes = (String) DataElementUtils.convertDataSetToMap(pod.getDataElements()).get("Context_Notes");
        assertEquals("Notes about this context.", contextNotes);
    }

    @Test
    public void testCreateCustomerWithBaseFieldset() {
        Customer customer = CreateEntities.createCustomerWithBaseFieldset(contextServiceClient);
        assertNotNull(customer.getId());
        String customerEmail = (String) DataElementUtils.convertDataSetToMap(customer.getDataElements()).get("Context_Work_Email");
        assertEquals("john.doe@example.com", customerEmail);
    }

    @Test
    public void testCreateRequestWithBaseFieldset() {
        Request request = CreateEntities.createRequestWithBaseFieldset(contextServiceClient);
        assertNotNull(request.getId());
        String contextTitle = (String) DataElementUtils.convertDataSetToMap(request.getDataElements()).get("Context_Title");
        assertEquals("Request1 Title", contextTitle);
    }

    @Test
    public void testCreatePodWithCustomer() {
        Customer customer = CreateEntities.createCustomerWithBaseFieldset(contextServiceClient);
        Pod pod = CreateEntities.createPodWithCustomer(contextServiceClient, customer);
        assertNotNull(pod.getId());
        assertEquals(customer.getId(), pod.getCustomerId());
    }

    @Test
    public void testCreatePodWithRequest() {
        Request request = CreateEntities.createRequestWithBaseFieldset(contextServiceClient);
        Pod pod = CreateEntities.createPodWithRequest(contextServiceClient, request);
        assertNotNull(pod.getId());
        assertEquals(request.getId(), pod.getRequestId());
    }

    @Test
    public void testCreatePodWithCustomerAndRequest() {
        Customer customer = CreateEntities.createCustomerWithBaseFieldset(contextServiceClient);
        Request request = CreateEntities.createRequestWithBaseFieldset(contextServiceClient);
        Pod pod = CreateEntities.createPodWithCustomerAndRequest(contextServiceClient, customer, request);
        assertNotNull(pod.getId());
        assertEquals(customer.getId(), pod.getCustomerId());
        assertEquals(request.getId(), pod.getRequestId());
    }

    @Test
    public void testCreateMultiplePodsWithSameCustomer() {
        Customer customer = CreateEntities.createCustomerWithBaseFieldset(contextServiceClient);
        List<Pod> pods = CreateEntities.createMultiplePodsWithSameCustomer(contextServiceClient, customer);
        for (Pod pod: pods) {
            assertNotNull(pod.getId());
            assertEquals(customer.getId(), pod.getCustomerId());
        }
    }

    @Test
    public void testCreateMultiplePodsWithSameRequest() {
        Request request = CreateEntities.createRequestWithBaseFieldset(contextServiceClient);
        List<Pod> pods = CreateEntities.createMultiplePodsWithSameRequest(contextServiceClient, request);
        for (Pod pod: pods) {
            assertNotNull(pod.getId());
            assertEquals(request.getId(), pod.getRequestId());
        }
    }
}
