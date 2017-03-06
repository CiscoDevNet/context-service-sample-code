package com.cisco.thunderhead.doc.examples;

import com.cisco.thunderhead.customer.Customer;
import com.cisco.thunderhead.pod.Pod;
import com.cisco.thunderhead.request.Request;
import com.cisco.thunderhead.util.DataElementUtils;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class UpdateEntitiesTest extends BaseExamplesTest {

    @Test
    public void updatePodTest() {
        Pod originalPod = CreateEntities.createPodWithBaseFieldset(contextServiceClient);
        String originalContextNotes = (String) DataElementUtils.convertDataSetToMap(originalPod.getDataElements()).get("Context_Notes");
        Pod updatedPod = UpdateEntities.updatePod(contextServiceClient, originalPod.getId());
        String updatedContextNotes = (String) DataElementUtils.convertDataSetToMap(updatedPod.getDataElements()).get("Context_Notes");
        // check that the pods are the same
        assertEquals(originalPod.getId(), updatedPod.getId());
        // check that Context_Notes were updated
        assertEquals("pod was modified", updatedContextNotes);
        assertNotEquals(
                "Context_Notes should have been updated",
                originalContextNotes,
                updatedContextNotes
        );
    }

    @Test
    public void addContributorToPodTest() {
        Pod originalPod = CreateEntities.createPodWithBaseFieldset(contextServiceClient);
        UpdateEntities.addContributorToPod(contextServiceClient, originalPod.getId());
        Pod updatedPod = contextServiceClient.get(Pod.class, originalPod.getId().toString());
        assertEquals(
                "updated pod should have the new contributor",
                "AgentId",
                updatedPod.getContributors().get(1).getId()
        );
    }

    @Test
    public void updateCustomerTest() {
        Customer originalCustomer = CreateEntities.createCustomerWithBaseFieldset(contextServiceClient);
        String originalAddress = (String) DataElementUtils.convertDataSetToMap(originalCustomer.getDataElements()).get("Context_Street_Address_1");
        Customer updatedPod = UpdateEntities.updateCustomer(contextServiceClient, originalCustomer.getId());
        String updatedAddress = (String) DataElementUtils.convertDataSetToMap(updatedPod.getDataElements()).get("Context_Street_Address_1");
        // check that the customer are the same
        assertEquals(originalCustomer.getId(), updatedPod.getId());
        // check that Context_Street_Address_1 was updated
        assertEquals("333 Sesame Street", updatedAddress);
        assertNotEquals(
                "Context_Street_Address_1 should have been updated",
                originalAddress,
                updatedAddress
        );
    }

    @Test
    public void updateRequestTest() {
        Request originalRequest = CreateEntities.createRequestWithBaseFieldset(contextServiceClient);
        String originalTitle = (String) DataElementUtils.convertDataSetToMap(originalRequest.getDataElements()).get("Context_Title");
        Request updatedRequest = UpdateEntities.updateRequest(contextServiceClient, originalRequest.getId());
        String updatedTitle = (String) DataElementUtils.convertDataSetToMap(updatedRequest.getDataElements()).get("Context_Title");
        // check that the pods are the same
        assertEquals(originalRequest.getId(), updatedRequest.getId());
        // check that Context_Notes were updated
        assertEquals("Updated Context Title", updatedTitle);
        assertNotEquals(
                "Context_Notes should have been updated",
                originalTitle,
                updatedTitle
        );
    }
}
