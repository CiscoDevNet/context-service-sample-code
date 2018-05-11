package com.cisco.thunderhead.doc.examples;

import com.cisco.thunderhead.ContextObject;
import com.cisco.thunderhead.Contributor;
import com.cisco.thunderhead.client.ContextServiceClient;
import com.cisco.thunderhead.datatypes.ContributorType;
import com.cisco.thunderhead.datatypes.PodMediaType;
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
    public static ContextObject updatePod(ContextServiceClient contextServiceClient, UUID podId) {
        ContextObject pod = contextServiceClient.getContextObject(ContextObject.Types.POD, podId.toString());
        // Add a media type
        pod.setMediaType(PodMediaType.SOCIAL);
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
    public static ContextObject addContributorToPod(ContextServiceClient contextServiceClient, UUID podId) {
        ContextObject pod = contextServiceClient.getContextObject(ContextObject.Types.POD, podId.toString());
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
    public static ContextObject updateCustomer(ContextServiceClient contextServiceClient, UUID customerId) {
        ContextObject customer = contextServiceClient.getContextObject(ContextObject.Types.CUSTOMER, customerId.toString());
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
    public static ContextObject updateRequest(ContextServiceClient contextServiceClient, UUID requestId) {
        ContextObject request = contextServiceClient.getContextObject(ContextObject.Types.REQUEST, requestId.toString());
        Map<String, Object> updateData = DataElementUtils.convertDataSetToMap(request.getDataElements());
        updateData.put("Context_Title", "Updated Context Title");
        request.setDataElements(DataElementUtils.convertDataMapToSet(updateData));
        contextServiceClient.update(request);
        return request;
    }
}
