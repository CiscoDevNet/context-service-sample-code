package com.cisco.thunderhead.doc.examples;


import com.cisco.thunderhead.ContextObject;
import com.cisco.thunderhead.client.ContextServiceClient;

public class DeleteEntities {

    /**
     * Delete a POD.
     * @param contextServiceClient an initialized ContextServiceClient
     * @param pod a pod object to delete
     */
    public static void deletePod(ContextServiceClient contextServiceClient, ContextObject pod){
        contextServiceClient.delete(pod);
    }

    /**
     * Delete a Customer.
     * @param contextServiceClient an initialized ContextServiceClient
     * @param customer a customer object to delete
     */
    public static void deleteCustomer(ContextServiceClient contextServiceClient, ContextObject customer){
        contextServiceClient.delete(customer);
    }

    /**
     * Delete a Request
     * @param contextServiceClient an initialized ContextServiceClient
     * @param request a request object to delete
     */
    public static void deleteRequest(ContextServiceClient contextServiceClient, ContextObject request){
        contextServiceClient.delete(request);
    }

}
