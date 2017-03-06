package com.cisco.thunderhead.doc.examples;

import com.cisco.thunderhead.client.ContextServiceClient;
import com.cisco.thunderhead.customer.Customer;
import com.cisco.thunderhead.errors.ApiException;
import com.cisco.thunderhead.pod.Pod;
import com.cisco.thunderhead.request.Request;
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
    public static Pod getPod(ContextServiceClient contextServiceClient, UUID podId) {
        return contextServiceClient.get(Pod.class, podId.toString());
    }

    /**
     * Extract the Context_Notes value out of a pod
     * @param pod the pod to extract the Context_Notes from
     * @return the Context_Notes
     */
    public static String getContextNotes(Pod pod) {
        return (String) DataElementUtils.convertDataSetToMap(pod.getDataElements()).get("Context_Notes");
    }

    /**
     * Retrieve a customer from Context Service
     * @param contextServiceClient an initialized ContextServiceClient
     * @param customerId the ID of the customer to get
     * @return the customer
     */
    public static Customer getCustomer(ContextServiceClient contextServiceClient, UUID customerId) {
        return contextServiceClient.get(Customer.class, customerId.toString());
    }

    /**
     * Extract the Context_First_Name value out of a customer
     * @param customer the customer to extract the Context_First_Name from
     * @return the Context_First_Name
     */
    public static String getCustomerFirstName(Customer customer) {
        return (String) DataElementUtils.convertDataSetToMap(customer.getDataElements()).get("Context_First_Name");
    }

    /**
     * Retrieve a request from Context Service
     * @param contextServiceClient an initialized ContextServiceClient
     * @param requestId the ID of the request to get
     * @return the request
     */
    public static Request getRequest(ContextServiceClient contextServiceClient, UUID requestId) {
        return contextServiceClient.get(Request.class, requestId.toString());
    }

    /**
     * Extract the Context_Title value out of a request
     * @param request the request to extract the Context_First_Name from
     * @return the Context_Title
     */
    public static String getRequestTitle(Request request) {
        return (String) DataElementUtils.convertDataSetToMap(request.getDataElements()).get("Context_Title");
    }

    /**
     * Demonstrate catching and throwing a Get error
     * @param contextServiceClient an initialized ContextServiceClient
     */
    public static void throwErrorOnGet(ContextServiceClient contextServiceClient) {
        try {
            String invalidId = "2472ae10-4f8c-11e6-87cb-851eced64b31";
            contextServiceClient.get(Pod.class, invalidId);
        } catch (ApiException e) {
            LOGGER.info("get failed as expected (pod id was invalid): " + e);
            throw e;
        }
    }
}
