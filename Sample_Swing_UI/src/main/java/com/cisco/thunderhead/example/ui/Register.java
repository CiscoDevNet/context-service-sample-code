package com.cisco.thunderhead.example.ui;

import com.cisco.thunderhead.connector.RegisteringApplication;
import com.cisco.thunderhead.plugin.ConnectorFactory;

/**
 * Example of how to create a registration request URL.
 */
public class Register {
    public static void main(String args[]) {

        RegisteringApplication registerApp = ConnectorFactory.getConnector(RegisteringApplication.class);
        try{
            String productCallbackUrl = "http://fake";
            String APPLICATION_TYPE = "custom";
            String registrationURL = registerApp.createRegistrationRequest(productCallbackUrl, APPLICATION_TYPE);
            System.out.println("\n\n*** Open This URL: " + registrationURL);
        }
        catch (Exception e) {
            System.out.println("Error Creating Request! The Error is: " + e);
        }

    }
}
