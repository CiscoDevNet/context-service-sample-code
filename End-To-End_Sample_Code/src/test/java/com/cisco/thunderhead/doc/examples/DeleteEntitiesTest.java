package com.cisco.thunderhead.doc.examples;


import com.cisco.thunderhead.ContextObject;
import org.junit.Test;

public class DeleteEntitiesTest extends BaseExamplesTest{

    @Test
    public void deletePodTest(){
        ContextObject pod = CreateEntities.createPodWithBaseFieldset(contextServiceClient);
        DeleteEntities.deleteContextObject(contextServiceClient,pod);
    }

    @Test
    public void deleteCustomerTest(){
        ContextObject customer = CreateEntities.createCustomerWithBaseFieldset(contextServiceClient);
        DeleteEntities.deleteContextObject(contextServiceClient,customer);
    }

    @Test
    public void deleteRequestTest(){
        ContextObject customer = CreateEntities.createCustomerWithBaseFieldset(contextServiceClient);
        ContextObject request = CreateEntities.createRequestWithBaseFieldset(contextServiceClient, customer);
        DeleteEntities.deleteContextObject(contextServiceClient,request);
    }
}
