package com.cisco.thunderhead.doc.examples;

import com.cisco.thunderhead.ContextObject;
import com.cisco.thunderhead.client.ContextServiceClient;
import com.cisco.thunderhead.errors.ApiException;
import com.cisco.thunderhead.util.DataElementUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;


public class GetEntities {
    private static final Logger LOGGER = LoggerFactory.getLogger(GetEntities.class);
    /**
     * Retrieve a pod from Context Service
     * @param contextServiceClient an initialized ContextServiceClient
     * @param podId the ID of the pod to get
     * @return the pod
     */
    public static ContextObject getPod(ContextServiceClient contextServiceClient, UUID podId) {
        return contextServiceClient.getContextObject(ContextObject.Types.POD, podId.toString());
    }

    /**
     * Extract the Context_Notes value out of a pod
     * @param pod the pod to extract the Context_Notes from
     * @return the Context_Notes
     */
    public static String getContextNotes(ContextObject pod) {
        return (String) DataElementUtils.convertDataSetToMap(pod.getDataElements()).get("Context_Notes");
    }

    /**
     * Retrieve a customer from Context Service
     * @param contextServiceClient an initialized ContextServiceClient
     * @param customerId the ID of the customer to get
     * @return the customer
     */
    public static ContextObject getCustomer(ContextServiceClient contextServiceClient, UUID customerId) {
        return contextServiceClient.getContextObject(ContextObject.Types.CUSTOMER, customerId.toString());
    }

    /**
     * Extract the Context_First_Name value out of a customer
     * @param customer the customer to extract the Context_First_Name from
     * @return the Context_First_Name
     */
    public static String getCustomerFirstName(ContextObject customer) {
        return (String) DataElementUtils.convertDataSetToMap(customer.getDataElements()).get("Context_First_Name");
    }

    /**
     * Retrieve a request from Context Service
     * @param contextServiceClient an initialized ContextServiceClient
     * @param requestId the ID of the request to get
     * @return the request
     */
    public static ContextObject getRequest(ContextServiceClient contextServiceClient, UUID requestId) {
        return contextServiceClient.getContextObject(ContextObject.Types.REQUEST, requestId.toString());
    }

    /**
     * Extract the Context_Title value out of a request
     * @param request the request to extract the Context_First_Name from
     * @return the Context_Title
     */
    public static String getRequestTitle(ContextObject request) {
        return (String) DataElementUtils.convertDataSetToMap(request.getDataElements()).get("Context_Title");
    }

    /**
     * Extract a detail.comment from Context Service
     * @param contextServiceClient
     * @param detailCommentId the ID of the detail.comment to get
     * @return the detail.comment
     */
    public static ContextObject getDetailComment(ContextServiceClient contextServiceClient, UUID detailCommentId) {
        return contextServiceClient.getContextObject("detail.comment", detailCommentId.toString());
    }

    /**
     * Extract the Context_Comment value out of a detail.comment
     * @param detailComment the detail.comment to extract the Context_Comment from
     * @return the Context_Comment
     */
    public static String getDetailCommentContextComment(ContextObject detailComment) {
        return (String) DataElementUtils.convertDataSetToMap(detailComment.getDataElements()).get("Context_Comment");
    }

    /**
     * Demonstrate catching and throwing a Get error
     * @param contextServiceClient an initialized ContextServiceClient
     */
    public static void throwErrorOnGet(ContextServiceClient contextServiceClient) {
        try {
            String invalidId = "2472ae10-4f8c-11e6-87cb-851eced64b31";
            contextServiceClient.getContextObject(ContextObject.Types.POD, invalidId);
        } catch (ApiException e) {
            LOGGER.info("get failed as expected (pod id was invalid): " + e);
            throw e;
        }
    }
}
