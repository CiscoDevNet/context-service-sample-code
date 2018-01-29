package com.cisco.thunderhead.doc.examples;

import com.cisco.thunderhead.ContextObject;
import com.cisco.thunderhead.errors.ApiErrorType;
import com.cisco.thunderhead.errors.ApiException;
import org.apache.commons.lang3.StringUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.ReflectionException;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

public class CSJmxStatsTest extends BaseExamplesTest {
    private long count = 5;
    @BeforeClass
    public static void registerMbean() throws Exception {
        populateData();
        CSJmxStats.registerMbean();
    }

    @AfterClass
    public static void unregisterMbean() throws Exception {
        CSJmxStats.invokeResetMethod();
        CSJmxStats.unregisterMbean();

    }

    @Test
    public void testInvokeStatsValueMethod() throws MBeanException, InstanceNotFoundException, ReflectionException {
        //check if the statsValue operation is available outside and validate the value
        double result = CSJmxStats.invokeStatsValueMethod();
        //Result should be greater than 0
        assertEquals(true,  result > 0);
    }

    @Test
    public void testInvokeCountValueMethod() throws MBeanException, InstanceNotFoundException, ReflectionException {
        //check if the countValue operation is available outside and validate the value
       long result = CSJmxStats.invokeCountValueMethod();
        //Result should be 5
        assertEquals(count, result);
    }

    @Test
    public void testInvokeCountErrorValueMethod() throws MBeanException, InstanceNotFoundException, ReflectionException {
        //check if the countValue operation for errors is available outside and validate the value
        Object result = CSJmxStats.invokeCountErrorValueMethod();
        //Result should be 5
        assertEquals(count, result);
    }

    @Test
    public void testInvokeStatsSummary() throws MBeanException, InstanceNotFoundException, ReflectionException {
        //statsSummary to validate
        String  statsSummary = "\"Pod.Create.Count\"" + ":" + "\"5\"" ;
        String requestDate = "statsRequestDateTime";
        //check if the summaryStats operation is available outside and validate the value
        String result = CSJmxStats.invokeStatsSummaryMethod();
        assertEquals(true, StringUtils.contains(result.toString(), statsSummary));
        assertEquals(true, StringUtils.contains(result.toString(), requestDate));

    }

    @Test
    public void testInvokeAbsoluteStatsValueMethod() throws MBeanException, InstanceNotFoundException, ReflectionException {
        //check if the statsValue operation is available outside and validate the value
        double result = CSJmxStats.invokeAbsoluteStatsValueMethod();
        //Result should be greater than 0
        assertEquals(true, result > 0);
    }
    @Test
    public void testInvokeAbsoluteCountValueMethod() throws MBeanException, InstanceNotFoundException, ReflectionException {
        //check if the countValue operation is available outside and validate the value
        long result = CSJmxStats.invokeAbsoluteCountValueMethod();
        //Result should be 5 or greater because it will not reset
        assertEquals(true, result >= count);
    }
    @Test
    public void testInvokeAbsoluteStatsSummary() throws MBeanException, InstanceNotFoundException, ReflectionException {
        //absoluteStatsSummary to validate
        String  statsSummary = "\"Pod.Create.Count\"";
        String requestDate = "statsRequestDateTime";
        //check if the summaryStats operation is available outside and validate the value
        String result = CSJmxStats.invokeAbsoluteStatsSummaryMethod();
        assertEquals(true, StringUtils.contains(result.toString(), statsSummary));
        assertEquals(true, StringUtils.contains(result.toString(), requestDate));
    }

    @Test
    public void testInvokeResetMethod() throws MBeanException, InstanceNotFoundException, ReflectionException {
        //invoke flush the counts
        CSJmxStats.invokeResetMethod();
        //invoke countValue operation and validate the value to be 0
        Object result = CSJmxStats.invokeCountValueMethod();
        assertEquals(0L, result);
        //repopulate
        populateData();
    }


    /**
     * creates/delete and get 5 pods in order to generate counts and stats
     */
    private static void populateData() {
        // create multiple pods - 5 pods
        ContextObject customer = CreateEntities.createCustomerWithBaseFieldset(contextServiceClient);
        List<ContextObject> pods = CreateEntities.createMultiplePodsWithSameCustomer(contextServiceClient, customer);
        for (ContextObject pod: pods) {
            assertNotNull(pod.getId());
            assertEquals(customer.getId(), pod.getCustomerId());
        }

        //delete the pods
        for (ContextObject pod: pods) {
            DeleteEntities.deletePod(contextServiceClient, pod);
        }

        //Get not existing pods that will throw notFound exception and increment Pod.Get.error.notFound count
        for (ContextObject pod : pods) {
            try{
                GetEntities.getPod(contextServiceClient, pod.getId());
                fail("testEncryptDecryptSearchFailures - getAndDecrypt failed to throw exception");
            }catch (ApiException e){
                assertEquals(ApiErrorType.NOT_FOUND, e.getError().getErrorType() );
            }
        }
    }

}
