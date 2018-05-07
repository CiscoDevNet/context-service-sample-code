package com.cisco.thunderhead.doc.examples;

import com.cisco.thunderhead.ContextObject;
import com.cisco.thunderhead.errors.ApiErrorType;
import com.cisco.thunderhead.errors.ApiException;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class GetEntitiesTest extends BaseExamplesTest {

    @Test
    public void getPodTest() {
        ContextObject originalPod = CreateEntities.createPodWithBaseFieldset(contextServiceClient);
        ContextObject gottenPod = GetEntities.getPod(contextServiceClient, originalPod.getId());
        assertEquals(originalPod.getId(), gottenPod.getId());
    }

    @Test
    public void getContextNotesTest() {
        ContextObject pod = CreateEntities.createPodWithBaseFieldset(contextServiceClient);
        assertEquals("Notes about this context.", GetEntities.getContextNotes(pod));
    }

    @Test
    public void getCustomerTest() {
        ContextObject originalCustomer = CreateEntities.createCustomerWithBaseFieldset(contextServiceClient);
        ContextObject gottenCustomer = GetEntities.getCustomer(contextServiceClient, originalCustomer.getId());
        assertEquals(originalCustomer.getId(), gottenCustomer.getId());
    }

    @Test
    public void getCustomerFirstNameTest() {
        ContextObject customer = CreateEntities.createCustomerWithBaseFieldset(contextServiceClient);
        assertEquals("John", GetEntities.getCustomerFirstName(customer));
    }

    @Test
    public void getRequestTest() {
        ContextObject originalRequest = CreateEntities.createRequestWithBaseFieldset(contextServiceClient);
        ContextObject gottenRequest = GetEntities.getRequest(contextServiceClient, originalRequest.getId());
        assertEquals(originalRequest.getId(), gottenRequest.getId());
    }

    @Test
    public void getRequestTitleTest() {
        ContextObject request = CreateEntities.createRequestWithBaseFieldset(contextServiceClient);
        assertEquals("Request1 Title", GetEntities.getRequestTitle(request));
    }

    @Test
    public void getDetailCommentTest() {
        ContextObject pod = CreateEntities.createPodWithBaseFieldset(contextServiceClient);
        ContextObject originalDetailComment = CreateEntities.createDetailCommentWithBaseFieldset(contextServiceClient, pod);
        ContextObject gottenDetailComment = GetEntities.getDetailComment(contextServiceClient, originalDetailComment.getId());
        assertEquals(originalDetailComment.getId(), gottenDetailComment.getId());
    }

    @Test
    public void getDetailCommentCommentTest() {
        ContextObject pod = CreateEntities.createPodWithBaseFieldset(contextServiceClient);
        ContextObject detailComment = CreateEntities.createDetailCommentWithBaseFieldset(contextServiceClient, pod);
        assertEquals("Detailed context comment.", GetEntities.getDetailCommentContextComment(detailComment));
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
