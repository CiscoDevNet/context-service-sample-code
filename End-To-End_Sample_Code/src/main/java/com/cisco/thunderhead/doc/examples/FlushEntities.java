package com.cisco.thunderhead.doc.examples;

import com.cisco.thunderhead.client.ContextServiceClient;
import com.cisco.thunderhead.customer.Customer;
import com.cisco.thunderhead.pod.Pod;
import com.cisco.thunderhead.request.Request;
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
     * @param initialized Context Service Client
     * @throws InterruptedException, when an interrupt signal is caught during wait
     * @throws TimeoutException, when flush is not complete and the timeout expires
     */
    public static void flushAllEntities(ContextServiceClient contextServiceClient) throws InterruptedException, TimeoutException {
        LOGGER.info("Flushing workgroup data...");

        contextServiceClient.flush(Pod.class);
        contextServiceClient.flush(Request.class);
        contextServiceClient.flush(Customer.class);

        FlushStatusBean status = null;

        // Use SDK to wait for flush to complete.  In this case, allow up to 30 seconds...
        status = contextServiceClient.waitForFlushComplete(Pod.class, MAX_FLUSH_WAIT_IN_SECONDS);
        if (status.isCompleted()) {
            LOGGER.info("Flush of pods complete. Flushed " + status.getNumberFlushed() + " pods.");
        } else {
            LOGGER.info("Flush of pods not complete. Flushed " + status.getNumberFlushed() + " pods. " + (!StringUtils.isEmpty(status.getMessage()) ? status.getMessage() : ""));
            throw new TimeoutException();
        }

        status = contextServiceClient.waitForFlushComplete(Request.class, MAX_FLUSH_WAIT_IN_SECONDS);
        if (status.isCompleted()) {
            LOGGER.info("Flush of requests complete. Flushed " + status.getNumberFlushed() + " requests.");
        } else {
            LOGGER.info("Flush of requests not complete. Flushed " + status.getNumberFlushed() + " requests. " + (!StringUtils.isEmpty(status.getMessage()) ? status.getMessage() : ""));
            throw new TimeoutException();
        }

        status = contextServiceClient.waitForFlushComplete(Customer.class, MAX_FLUSH_WAIT_IN_SECONDS);
        if (status.isCompleted()) {
            LOGGER.info("Flush of customers complete. Flushed " + status.getNumberFlushed() + " customers.");
        } else {
            LOGGER.info("Flush of customers not complete. Flushed " + status.getNumberFlushed() + " customers. " + (!StringUtils.isEmpty(status.getMessage()) ? status.getMessage() : ""));
            throw new TimeoutException();
        }

        LOGGER.info("Flushed workgroup data.");

    }

    /**
     * Note: flush is only supported in "lab" (not "production") mode.
     *
     * @param contextServiceClient an initialized Context Service Client
     * @throws InterruptedException thrown if an interrupt signal is caught during the wait
     * @throws TimeoutException thrown if a flush has not completed when the timeout expires
     */
    public static void flushCustomers(ContextServiceClient contextServiceClient) throws InterruptedException, TimeoutException {
        LOGGER.info("Flushing customer data...");
        contextServiceClient.flush(Customer.class);

        FlushStatusBean status = null;
        status = contextServiceClient.waitForFlushComplete(Customer.class, MAX_FLUSH_WAIT_IN_SECONDS);
        if (status.isCompleted()) {
            LOGGER.info("Flush of customers complete. Flushed " + status.getNumberFlushed() + " customers.");
        } else {
            LOGGER.info("Flush of customers not complete. Flushed " + status.getNumberFlushed() + " customers. " + (!StringUtils.isEmpty(status.getMessage()) ? status.getMessage() : ""));
            throw new TimeoutException();
        }

        LOGGER.info("Flushed customer data.");
    }

    /**
     * Note: flush is only supported in "lab" (not "production") mode.
     *
     * @param contextServiceClient an initialized Context Service Client
     * @throws InterruptedException thrown if an interrupt signal is caught during the wait
     * @throws TimeoutException thrown if a flush has not completed when the timeout expires
     */
    public static void flushPods(ContextServiceClient contextServiceClient) throws InterruptedException, TimeoutException {
        LOGGER.info("Flushing pod data...");
        contextServiceClient.flush(Pod.class);

        FlushStatusBean status = null;
        status = contextServiceClient.waitForFlushComplete(Pod.class, MAX_FLUSH_WAIT_IN_SECONDS);
        if (status.isCompleted()) {
            LOGGER.info("Flush of pods complete. Flushed " + status.getNumberFlushed() + " pods.");
        } else {
            LOGGER.info("Flush of pods not complete. Flushed " + status.getNumberFlushed() + " pods. " + (!StringUtils.isEmpty(status.getMessage()) ? status.getMessage() : ""));
            throw new TimeoutException();
        }

        LOGGER.info("Flushed pod data.");
    }

    /**
     * Note: flush is only supported in "lab" (not "production") mode.
     *
     * @param contextServiceClient an initialized Context Service Client
     * @throws InterruptedException thrown if an interrupt signal is caught during the wait
     * @throws TimeoutException thrown if a flush has not completed when the timeout expires
     */
    public static void flushRequests(ContextServiceClient contextServiceClient) throws InterruptedException, TimeoutException {
        LOGGER.info("Flushing request data...");
        contextServiceClient.flush(Request.class);

        FlushStatusBean status = null;
        status = contextServiceClient.waitForFlushComplete(Request.class, MAX_FLUSH_WAIT_IN_SECONDS);
        if (status.isCompleted()) {
            LOGGER.info("Flush of requests complete. Flushed " + status.getNumberFlushed() + " requests.");
        } else {
            LOGGER.info("Flush of requests not complete. Flushed " + status.getNumberFlushed() + " requests. " + (!StringUtils.isEmpty(status.getMessage()) ? status.getMessage() : ""));
            throw new TimeoutException();
        }

        LOGGER.info("Flushed request data.");
    }

}
