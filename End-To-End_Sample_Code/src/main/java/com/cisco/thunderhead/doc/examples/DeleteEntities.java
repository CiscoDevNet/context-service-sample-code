package com.cisco.thunderhead.doc.examples;


import com.cisco.thunderhead.client.ContextServiceClient;
import com.cisco.thunderhead.customer.Customer;
import com.cisco.thunderhead.pod.Pod;
import com.cisco.thunderhead.request.Request;

public class DeleteEntities {

    /**
     * Delete a POD.
     * @param an initialized ContextServiceClient
     * @param pod a pod object to delete
     */
    public static void deletePod(ContextServiceClient contextServiceClient, Pod pod){
        contextServiceClient.delete(Pod.class, pod.getId().toString());
    }

    /**
     * Delete a Customer.
     * @param an initialized ContextServiceClient
     * @param customer a customer object to delete
     */
    public static void deleteCustomer(ContextServiceClient contextServiceClient, Customer customer){
        contextServiceClient.delete(Customer.class, customer.getId().toString());
    }

    /**
     * Delete a Request
     * @param an initialized ContextServiceClient
     * @param request a request object to delete
     */
    public static void deleteRequest(ContextServiceClient contextServiceClient, Request request){
        contextServiceClient.delete(Request.class, request.getId().toString());
    }

}
