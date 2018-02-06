package com.cisco.thunderhead.doc.examples;


import com.cisco.thunderhead.ContextObject;
import com.cisco.thunderhead.client.ContextServiceClient;

public class DeleteEntities {

    /**
     * Delete a ContextObject
     * @param contextServiceClient an initialized ContextServiceClient
     * @param object the ContextObject to be deleted
     */
    public static void deleteContextObject(ContextServiceClient contextServiceClient, ContextObject object){
        contextServiceClient.delete(object);
    }

}
