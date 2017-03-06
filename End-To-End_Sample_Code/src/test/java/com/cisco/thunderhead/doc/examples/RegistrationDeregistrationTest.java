package com.cisco.thunderhead.doc.examples;

import org.junit.Test;

import java.net.MalformedURLException;
import java.net.URL;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class RegistrationDeregistrationTest extends BaseExamplesTest {

    @Test
    public void testGenerateRegistrationUrl(){

        URL url = null;
        String registrationUrl = RegistrationDeregistration.generateRegistrationUrl();
        assertNotNull(registrationUrl);
        try{
            url = new URL(registrationUrl);
            assertEquals("ccfs.ciscoccservice.com",url.getHost());
            assertEquals("/v1/authorize",url.getPath());
            assertEquals("callbackUrl=https%3A%2F%2Flocalhost%3A7443%2FproductCallback&appType=custom",url.getQuery());

        }catch (MalformedURLException e){
            fail("failed to validate registration url: "+registrationUrl);
        }
    }

    @Test
    public void testGenerateDeregistrationUrl(){

        String deregistrationUrl = RegistrationDeregistration.generateDeregistrationUrl(mgmtConnector);
        try{
            URL url = new URL(deregistrationUrl);
            assertEquals("ccfs.ciscoccservice.com",url.getHost());
            assertEquals("/v1/unauthorize",url.getPath());
            assertTrue(url.getQuery().contains("callbackUrl=https%3A%2F%2Flocalhost%3A7443%2FproductCallback&connectionData="));

        }catch (MalformedURLException e){
            fail("failed to validate deregistration url: "+deregistrationUrl);
        }

    }
}
