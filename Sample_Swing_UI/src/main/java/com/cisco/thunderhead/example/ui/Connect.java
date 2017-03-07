package com.cisco.thunderhead.example.ui;

/**
 * This is a simple example to show how you can call the Context Service Client.
 */
public class Connect {

    public static void main(String args[]) {
        Utils.runIt(ConnectionData.getConnectionData(), (contextServiceClient) -> {
            System.out.println("\n\nXXX\nStatus:\n" + contextServiceClient.getStatus() + "\nXXXXXX\n");
        });
    }

}