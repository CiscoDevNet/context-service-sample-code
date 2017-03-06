package com.cisco.thunderhead.doc.examples;

import javax.management.InstanceNotFoundException;
import javax.management.JMException;
import javax.management.MBeanException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import java.lang.management.ManagementFactory;

public class CSJmxStats {
    public static final String CONTEXT_OBJECT_NAME = "com.cisco.context:type=ContextServiceClient";
    public static ObjectName  contextObjName;
    public static MBeanServer mbeanServer;

    /**
     * Create ContextServiceClient object
     */
    public static void registerMbean() throws MBeanException, InstanceNotFoundException, ReflectionException {
        mbeanServer = ManagementFactory.getPlatformMBeanServer();
        try {
             contextObjName = new ObjectName(CONTEXT_OBJECT_NAME);
        } catch (MalformedObjectNameException e) {
            e.printStackTrace();
        }
    }

    /**
     * Get minimum latency of PODs deleted from the time the stats was last reset
     * @return Double - It returns the minimum latency of PODs deleted
     */
    public static <T> Double invokeStatsValueMethod() throws MBeanException, InstanceNotFoundException, ReflectionException {
        final String statsOpName= "statsValue";
        final Object[] statsParams= {"Pod.Delete", "min"};
        final String[] statsValueSignature= new String[]{"java.lang.String", "java.lang.String"};
        Object result = mbeanServer.invoke(contextObjName, statsOpName, statsParams, statsValueSignature);
        return (Double) result.getClass().cast(result);
    }

    /**
     * Get the number of PODs created from the time the counter was last reset
     * @return Long - It returns the count of PODs created
     */
    public static <T> Long invokeCountValueMethod() throws MBeanException, InstanceNotFoundException, ReflectionException {
        final String countOpName= "countValue";
        final String[] countValueSignature= new String[]{"java.lang.String"};
        final Object[] countParams= {"Pod.Create"};
        Object result =  mbeanServer.invoke(contextObjName, countOpName, countParams, countValueSignature);
        return (Long) result.getClass().cast(result);
    }

    /**
     * Get the number of notFound errors thrown when trying to get a Pod since the counter was last reset
     * @return Long -It returns the count of notFound PODs
     */
    public static <T> Long invokeCountErrorValueMethod() throws MBeanException, InstanceNotFoundException, ReflectionException {

        final String countOpName= "countValue";
        final String[] countValueSignature= new String[]{"java.lang.String"};
        final Object[] errorCountParams= {"Pod.Get.error.notFound"};
        Object result  =  mbeanServer.invoke(contextObjName, countOpName, errorCountParams, countValueSignature);
        return (Long) result.getClass().cast(result);
    }

    /**
     * Get summary of al statistics measured from the time the stats/counter was reset
     * @return String - It returns a JSON String that contains all the stats and counters including the timestamp when the collection started and the timestamp when the request was made
     */
    public static <T> String invokeStatsSummaryMethod() throws MBeanException, InstanceNotFoundException, ReflectionException {
        final String summaryOpName= "statsSummary";
        Object result =  mbeanServer.invoke(contextObjName, summaryOpName, null, null);
        return (String) result.getClass().cast(result);
    }

    /**
     * Reset all statistics and counters
     */
    public static void invokeResetMethod() throws MBeanException, InstanceNotFoundException, ReflectionException {
        final String resetOpName= "resetStats";
        mbeanServer.invoke(contextObjName, resetOpName, null, null);
    }

    /**
     * Get minimum latency all PODs deleted from the time the client was initialized.
     * @return Double - It returns the minimum latency of PODSs deleted
     */
    public static <T> Double invokeAbsoluteStatsValueMethod() throws MBeanException, InstanceNotFoundException, ReflectionException {
        final String statsOpName= "absoluteStatsValue";
        final Object[] statsParams= {"Pod.Delete", "min"};
        final String[] statsValueSignature= new String[]{"java.lang.String", "java.lang.String"};
        Object result = mbeanServer.invoke(contextObjName, statsOpName, statsParams, statsValueSignature);
        return (Double) result.getClass().cast(result);
    }

    /**
     * Get the count of all POSs created from the time the client was initialized.
     * @return Long - It returns the number of PODs created.
     */
    public static <T> Long invokeAbsoluteCountValueMethod() throws MBeanException, InstanceNotFoundException, ReflectionException {
        final String countOpName= "absoluteCountValue";
        final String[] countValueSignature= new String[]{"java.lang.String"};
        final Object[] countParams= {"Pod.Create"};
        Object result =  mbeanServer.invoke(contextObjName, countOpName, countParams, countValueSignature);
        return (Long) result.getClass().cast(result);
    }

    /**
     * Get a summary of all statistics measured since client was initialized
     * @return String - It returns a JSON String that contains all the stats and counters including the timestamp when the client was initialized and the timestamp when the request was made.
     */
    public static <T> String invokeAbsoluteStatsSummaryMethod() throws MBeanException, InstanceNotFoundException, ReflectionException {
        final String summaryOpName= "absoluteStatsSummary";
        Object result = mbeanServer.invoke(contextObjName, summaryOpName, null, null);
        return (String) result.getClass().cast(result);
    }

    /**
     * unregister Mbean from MBServer
     */
    public static void unregisterMbean() {
        try {
            ObjectName contextObjName = new ObjectName(CONTEXT_OBJECT_NAME);
            if (mbeanServer.isRegistered(contextObjName))
                mbeanServer.unregisterMBean(contextObjName);
        } catch (JMException ex) {
            throw new IllegalStateException(ex);
        }
    }

}