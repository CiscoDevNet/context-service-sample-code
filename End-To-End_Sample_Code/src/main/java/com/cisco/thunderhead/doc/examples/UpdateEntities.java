package com.cisco.thunderhead.doc.examples;

import com.cisco.thunderhead.Contributor;
import com.cisco.thunderhead.client.ContextServiceClient;
import com.cisco.thunderhead.customer.Customer;
import com.cisco.thunderhead.datatypes.ContributorType;
import com.cisco.thunderhead.datatypes.PodMediaType;
import com.cisco.thunderhead.datatypes.PodState;
import com.cisco.thunderhead.pod.Pod;
import com.cisco.thunderhead.request.Request;
import com.cisco.thunderhead.util.DataElementUtils;

import java.util.Map;
import java.util.UUID;

public class UpdateEntities {

    /**
     * Update a POD.
     * @param contextServiceClient an initialized ContextServiceClient
     * @param podId
     * @return the updated POD
     */
    public static Pod updatePod(ContextServiceClient contextServiceClient, UUID podId) {
        Pod pod = contextServiceClient.get(Pod.class, podId.toString());
        // Add a media type and change the state
        pod.setMediaType(PodMediaType.SOCIAL);
        pod.setState(PodState.CLOSED);
        // update DataElements
        Map<String, Object> updateData =  DataElementUtils.convertDataSetToMap(pod.getDataElements());
        updateData.put("Context_Notes", "pod was modified");
        pod.setDataElements(DataElementUtils.convertDataMapToSet(updateData));
        // Send the update
        contextServiceClient.update(pod);
        return pod;
    }

    /**
     * Add a new contributor to a POD.
     * @param contextServiceClient an initialized ContextServiceClient
     * @param podId
     * @return the updated POD with the new contributor
     */
    public static Pod addContributorToPod(ContextServiceClient contextServiceClient, UUID podId) {
        Pod pod = contextServiceClient.get(Pod.class, podId.toString());
        Contributor contributor = new Contributor(ContributorType.USER, "AgentId");
        pod.setNewContributor(contributor);
        contextServiceClient.update(pod);
        return pod;
    }

    /**
     * Update a Customer.
     * @param contextServiceClient an initialized ContextServiceClient
     * @param customerId
     * @return the updated Customer
     */
    public static Customer updateCustomer(ContextServiceClient contextServiceClient, UUID customerId) {
        Customer customer = contextServiceClient.get(Customer.class, customerId.toString());
        Map<String, Object> updateData = DataElementUtils.convertDataSetToMap(customer.getDataElements());
        updateData.put("Context_Street_Address_1", "333 Sesame Street");
        customer.setDataElements(DataElementUtils.convertDataMapToSet(updateData));
        contextServiceClient.update(customer);
        return customer;
    }

    /**
     * Update a Request.
     * @param contextServiceClient an initialized ContextServiceClient
     * @param requestId
     * @return the updated Request
     */
    public static Request updateRequest(ContextServiceClient contextServiceClient, UUID requestId) {
        Request request = contextServiceClient.get(Request.class, requestId.toString());
        Map<String, Object> updateData = DataElementUtils.convertDataSetToMap(request.getDataElements());
        updateData.put("Context_Title", "Updated Context Title");
        request.setDataElements(DataElementUtils.convertDataMapToSet(updateData));
        contextServiceClient.update(request);
        return request;
    }
}
