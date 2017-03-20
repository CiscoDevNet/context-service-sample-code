# cs-java-sdk-swing-ui
Java Swing UI that uses the Context Service Java SDK

## Introduction 
This demonstrates the following:

* Getting status
* Getting JMX metrics
* Connection state listener
* Field and Field Set create/retrieve/update/delete (CRUD) operations
* Pod, Customer, and Request CRUD operations  

## Prerequisites
The following is needed for this project:

* Java 8
* IntelliJ with the UI Designer plugin to edit the UI.
* Maven

## Getting Started
To get started:

1. Go to https://developer.cisco.com/site/context-service/discover/getting-started/
2. There you will see a link to "Context Service Downloads": https://developer.cisco.com/fileMedia/download/dcf47513-a2cb-407c-b8a5-cc0d8f620405
3. You will get a context-service-sdk-X.X.X.tar.gz file
4. Run `prepareSDK.sh` with parameters [targz-filename]
* This sets up your connector.properties file
5. Run `mvn clean install`
6. Create the connectiondata.txt file (see below)
7. Run the script `run.sh` to start the application

## Creating connectiondata.txt file
Register with Context Service. You need the ConnectionData string to connect to Context Service. The connection data string is generated when you register with Context Service. Once you register and receive the connection data string, save the string to connectiondata.txt file in your project directory.

## Usage notes
* When creating Customers, Requests, and Pods, you can optionally specify the fieldset to use when making that type.
* To search, specify key:value pairs, where each key/value pair is colon delimited, and each pair is space-delimited.
  * For instance: Context_City:Boxborough
  
## Adjusting the UI
If you need to adjust the UI, change the following setting in IntelliJ:

* Settings > Editor > GUI Designer > Generate GUI into: Java source code

## Creating your own application
The Utils.runIt() method can be used to create a simple application.  The method takes a function which can use a local version of the Context Service SDK.

For instance, this will call the `getStatus()` API.

     public static void main(String args[]) {
            Utils.runIt(ConnectionData.getConnectionData(), (contextServiceClient) -> {
                System.out.println("\n\n\nStatus:\n" + contextServiceClient.getStatus() + "\n\n");
            });
        }

## Disclaimer
This sample code is only a sample and is NOT guaranteed to be bug free and production quality. This is NOT intended to be used in production environment. You must adapt the code to work with your custom application.

## Support Notice

Support for the sample is provided on a "best effort" basis via DevNet. Like any custom deployment, it is the responsibility of the partner and/or customer to ensure that the customization works correctly.

[https://developer.cisco.com/site/context-service](https://developer.cisco.com/site/context-service)
