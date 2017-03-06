package com.cisco.thunderhead.doc.examples;

import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.TimeoutException;

import static org.junit.Assert.fail;

public class FlushEntitiesTest extends BaseExamplesTest {

    @Before
    public void createDataToFlush() {
        CreateEntities.createPodWithBaseFieldset(contextServiceClient);
        CreateEntities.createCustomerWithBaseFieldset(contextServiceClient);
        CreateEntities.createRequestWithBaseFieldset(contextServiceClient);
    }

    @Test
    public void testFlushPods() {
        try {
            FlushEntities.flushPods(contextServiceClient);
        } catch (InterruptedException e) {
            fail("Caught InterruptedException.");
        } catch (TimeoutException e) {
            fail("Flush did not complete within timeout.");
        }
    }

    @Test
    public void testFlushRequests() {
        try {
            FlushEntities.flushRequests(contextServiceClient);
        } catch (InterruptedException e) {
            fail("Caught InterruptedException.");
        } catch (TimeoutException e) {
            fail("Flush did not complete within timeout.");
        }
    }

    @Test
    public void testFlushCustomers() {
        try {
            FlushEntities.flushCustomers(contextServiceClient);
        } catch (InterruptedException e) {
            fail("Caught InterruptedException.");
        } catch (TimeoutException e) {
            fail("Flush did not complete within timeout.");
        }
    }

    @Test
    public void testFlushAllEntities() {
        try {
            FlushEntities.flushAllEntities(contextServiceClient);
        } catch (InterruptedException e) {
            fail("Caught InterruptedException.");
        } catch (TimeoutException e) {
            fail("Flush did not complete within timeout.");
        }
    }

}
