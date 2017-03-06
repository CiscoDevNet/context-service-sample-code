package com.cisco.thunderhead.doc.examples;

import com.cisco.thunderhead.customer.Customer;
import com.cisco.thunderhead.errors.ApiErrorType;
import com.cisco.thunderhead.errors.ApiException;
import com.cisco.thunderhead.pod.Pod;
import com.cisco.thunderhead.request.Request;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class GetEntitiesTest extends BaseExamplesTest {

    @Test
    public void getPodTest() {
        Pod originalPod = CreateEntities.createPodWithBaseFieldset(contextServiceClient);
        Pod gottenPod = GetEntities.getPod(contextServiceClient, originalPod.getId());
        assertEquals(originalPod.getId(), gottenPod.getId());
    }

    @Test
    public void getContextNotesTest() {
        Pod pod = CreateEntities.createPodWithBaseFieldset(contextServiceClient);
        assertEquals("Notes about this context.", GetEntities.getContextNotes(pod));
    }

    @Test
    public void getCustomerTest() {
        Customer originalCustomer = CreateEntities.createCustomerWithBaseFieldset(contextServiceClient);
        Customer gottenCustomer = GetEntities.getCustomer(contextServiceClient, originalCustomer.getId());
        assertEquals(originalCustomer.getId(), gottenCustomer.getId());
    }

    @Test
    public void getCustomerFirstNameTest() {
        Customer customer = CreateEntities.createCustomerWithBaseFieldset(contextServiceClient);
        assertEquals("John", GetEntities.getCustomerFirstName(customer));
    }

    @Test
    public void getRequestTest() {
        Request originalRequest = CreateEntities.createRequestWithBaseFieldset(contextServiceClient);
        Request gottenRequest = GetEntities.getRequest(contextServiceClient, originalRequest.getId());
        assertEquals(originalRequest.getId(), gottenRequest.getId());
    }

    @Test
    public void getRequestTitleTest() {
        Request request = CreateEntities.createRequestWithBaseFieldset(contextServiceClient);
        assertEquals("Request1 Title", GetEntities.getRequestTitle(request));
    }

    @Test
    public void throwErrorOnGetTest() {
        try {
            GetEntities.throwErrorOnGet(contextServiceClient);
            fail("should have thrown an exception");
        } catch (ApiException e) {
            assertEquals(ApiErrorType.NOT_FOUND, e.getError().getErrorType());
        }
    }
}
