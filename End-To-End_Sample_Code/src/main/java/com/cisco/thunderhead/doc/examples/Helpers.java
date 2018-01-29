package com.cisco.thunderhead.doc.examples;

import com.cisco.thunderhead.ContextObject;
import com.cisco.thunderhead.client.ClientResponse;
import com.cisco.thunderhead.client.ContextServiceClient;
import com.cisco.thunderhead.util.RFC3339Date;
import com.cisco.thunderhead.util.SDKUtils;

public class Helpers {
    /**
     * Get Pod ID from the ClientResponse returned from ContextServiceClient.create()
     * Pod.getId() is preferred!
     * @param contextServiceClient an initialized context service client
     * @param pod a Pod object that hasn't been created in Context Service yet
     * @return the id of the newly created Pod
     */
    public static String getIdFromCreatePodResponse(ContextServiceClient contextServiceClient, ContextObject pod) {
        ClientResponse response = contextServiceClient.create(pod);
        return SDKUtils.getIdFromResponse(response);
    }

    /**
     * Create a pod and get the ID.
     * This is the preferred way to get an id from a new pod.
     * @param contextServiceClient an initialized context service client
     * @param pod a Pod object that hasn't been created in Context Service yet
     * @return the id of the newly created Pod
     */
    public static String getIdFromCreatePod(ContextServiceClient contextServiceClient, ContextObject pod) {
        contextServiceClient.create(pod);
        return pod.getId().toString();
    }

    /**
     * Get the lastUpdated timestamp from the ClientResponse returned from ContextServiceClient.create()
     * Pod.getLastUpdated() is preferred!
     * @param contextServiceClient an initialized context service client
     * @param pod a Pod object that hasn't been created in Context Service yet
     * @return the lastUpdated time of the new Pod
     */
    public static RFC3339Date getLastUpdatedFromCreatePodResponse(ContextServiceClient contextServiceClient, ContextObject pod) {
        ClientResponse response = contextServiceClient.create(pod);
        return SDKUtils.getLastUpdatedFromResponse(response);
    }

    /**
     * Create a Pod and get the lastUpdated timestamp.
     * This is the preferred way to get the lastUpdated time!
     * @param contextServiceClient an initialized context service client
     * @param pod a Pod object that hasn't been created in Context Service yet
     * @return the lastUpdated time of the new Pod
     */
    public static RFC3339Date getLastUpdatedFromCreatePod(ContextServiceClient contextServiceClient, ContextObject pod) {
        contextServiceClient.create(pod);
        return pod.getLastUpdated();
    }
}
