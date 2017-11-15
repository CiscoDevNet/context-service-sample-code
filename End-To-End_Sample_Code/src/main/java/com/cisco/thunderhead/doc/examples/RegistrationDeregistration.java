package com.cisco.thunderhead.doc.examples;

import com.cisco.thunderhead.connector.ManagementConnector;
import com.cisco.thunderhead.connector.RegisteringApplication;
import com.cisco.thunderhead.errors.ApiException;
import com.cisco.thunderhead.plugin.ConnectorFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RegistrationDeregistration {

    private final static Logger LOGGER = LoggerFactory.getLogger(RegistrationDeregistration.class);

    /**
     * Follow the registration workflow on https://developer.cisco.com/site/context-service/documents/context-service-sdk-guide/index.gsp#registration-workflow
     * to authorize your applications for Context Service.
     * 
     * Prerequisite:
     * 1. A Callback Url: web page hosted in your application server or local jetty server to capture connection data.
     *
     * @return a registration url
     */
    public static String generateRegistrationUrl(){
        String registrationURL = null;
        try {
            String callbackUrl = "https://localhost:7443/productCallback";
            String APPLICATION_TYPE = "custom";

            //Instantiating RegistrationApplication connector from ConnectorFactory
            RegisteringApplication registerApp = ConnectorFactory.getConnector(RegisteringApplication.class);

            //Making a registration request for `custom` application type
            registrationURL = registerApp.createRegistrationRequest(callbackUrl, APPLICATION_TYPE);
            LOGGER.info("\n \n *** Generated registration url. Open Registration URL: " + registrationURL);

        } catch (ApiException e) {
            LOGGER.info("Error generating Registration Url! The Error is:" + e);
        }

        return registrationURL;
    }


    /**
     * Follow the deregistration workflow on https://developer.cisco.com/site/context-service/documents/context-service-sdk-guide/index.gsp#deregister
     *
     * Prerequisite
     * 1. A Callback Url: webpage hosted in your application server or local jetty server to receive response of deregistration request.
     *
     * @param managementConnector a ManagementConnector instance in a `Registered` ConnectorState
     * @return a deregistration url
     */
    public static String generateDeregistrationUrl(ManagementConnector managementConnector){
        String deregistrationURL = null;
        try {
            // callbackUrl will be used to receive response of the generate de-registration url request
            String callbackUrl = "https://localhost:7443/productCallback";
            deregistrationURL = managementConnector.deregister(callbackUrl);
            LOGGER.info("\n \n *** Generated deregistration url. Open This URL: " + deregistrationURL);
        } catch (ApiException e) {
            LOGGER.info("Error creating de-registration Url! The Error is: " + e);
        }

        return deregistrationURL;
    }
}
