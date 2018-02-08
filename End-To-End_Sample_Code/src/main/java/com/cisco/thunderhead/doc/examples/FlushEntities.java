package com.cisco.thunderhead.doc.examples;

import com.cisco.thunderhead.ContextObject;
import com.cisco.thunderhead.client.ContextServiceClient;
import com.cisco.thunderhead.rest.FlushStatusBean;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeoutException;

public class FlushEntities {
    // Logger
    private final static Logger LOGGER = LoggerFactory.getLogger(FlushEntities.class);

    // Maximum time, in seconds, to wait for flush to complete before throwing an exception
    private final static int MAX_FLUSH_WAIT_IN_SECONDS = 30;

    /**
     * Flush all entities (pods, request, customers) at once and wait for each flush to complete.
     * Note: Flush is only supported in lab mode.
     *
     * @param contextServiceClient Context Service Client
     * @throws InterruptedException, when an interrupt signal is caught during wait
     * @throws TimeoutException, when flush is not complete and the timeout expires
     */
    public static void flushAllEntities(ContextServiceClient contextServiceClient) throws InterruptedException, TimeoutException {
        LOGGER.info("Flushing workgroup data...");

        contextServiceClient.flush(ContextObject.Types.POD);
        contextServiceClient.flush(ContextObject.Types.REQUEST);
        contextServiceClient.flush(ContextObject.Types.CUSTOMER);

        waitForFlushComplete(contextServiceClient, ContextObject.Types.POD);
        waitForFlushComplete(contextServiceClient, ContextObject.Types.CUSTOMER);
        waitForFlushComplete(contextServiceClient, ContextObject.Types.REQUEST);

        LOGGER.info("Flushed workgroup data.");
    }

    /**
     * waitForFlushComplete
     * @param contextServiceClient the contextServiceClient
     * @param contextObjectType the type of the ContextObject to be flushed
     * @throws TimeoutException
     */
    private static void waitForFlushComplete(ContextServiceClient contextServiceClient, String contextObjectType) throws TimeoutException {
        FlushStatusBean status;// Use SDK to wait for flush to complete.  In this case, allow up to 30 seconds...
        status = contextServiceClient.waitForFlushComplete(contextObjectType, MAX_FLUSH_WAIT_IN_SECONDS);
        if (status.isCompleted()) {
            LOGGER.info("Flush of " + contextObjectType + "s complete. Flushed " + status.getNumberFlushed() + " " + contextObjectType + "s.");
        } else {
            LOGGER.info("Flush of " + contextObjectType + "s not complete. Flushed " + status.getNumberFlushed() + " " + contextObjectType + "s." + (!StringUtils.isEmpty(status.getMessage()) ? status.getMessage() : ""));
            throw new TimeoutException();
        }
    }

    /**
     * Note: flush is only supported in "lab" (not "production") mode.
     *
     * @param contextServiceClient an initialized Context Service Client
     * @throws InterruptedException thrown if an interrupt signal is caught during the wait
     * @throws TimeoutException thrown if a flush has not completed when the timeout expires
     */
    public static void flushCustomers(ContextServiceClient contextServiceClient) throws InterruptedException, TimeoutException {
        flushContextObject(contextServiceClient, ContextObject.Types.CUSTOMER);
    }

    /**
     * Note: flush is only supported in "lab" (not "production") mode.
     *
     * @param contextServiceClient an initialized Context Service Client
     * @throws InterruptedException thrown if an interrupt signal is caught during the wait
     * @throws TimeoutException thrown if a flush has not completed when the timeout expires
     */
    public static void flushPods(ContextServiceClient contextServiceClient) throws InterruptedException, TimeoutException {
        flushContextObject(contextServiceClient, ContextObject.Types.POD);
    }

    /**
     * Note: flush is only supported in "lab" (not "production") mode.
     *
     * @param contextServiceClient an initialized Context Service Client
     * @throws InterruptedException thrown if an interrupt signal is caught during the wait
     * @throws TimeoutException thrown if a flush has not completed when the timeout expires
     */
    public static void flushRequests(ContextServiceClient contextServiceClient) throws InterruptedException, TimeoutException {
        flushContextObject(contextServiceClient, ContextObject.Types.REQUEST);
    }

    /**
     * flush context object
     * @param contextServiceClient
     * @param contextObjectType
     * @throws TimeoutException
     */
    private static void flushContextObject(ContextServiceClient contextServiceClient, String contextObjectType) throws TimeoutException {
        LOGGER.info("Flushing " + contextObjectType + " data...");
        contextServiceClient.flush(contextObjectType);

        FlushStatusBean status = null;
        status = contextServiceClient.waitForFlushComplete(contextObjectType, MAX_FLUSH_WAIT_IN_SECONDS);
        if (status.isCompleted()) {
            LOGGER.info("Flush of "  + contextObjectType + " requests complete. Flushed " + status.getNumberFlushed() + contextObjectType + ".");
        } else {
            LOGGER.info("Flush of requests not complete. Flushed " + status.getNumberFlushed() + contextObjectType + "."  + (!StringUtils.isEmpty(status.getMessage()) ? status.getMessage() : ""));
            throw new TimeoutException();
        }

        LOGGER.info("Flushed " + contextObjectType + " data.");
    }
}
