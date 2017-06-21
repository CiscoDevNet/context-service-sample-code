# cs-java-sdk-swing-ui
Java Swing UI that uses the Context Service Java SDK

## Introduction 
This sample demonstrates:

* Getting status
* Getting JMX metrics
* Connection state listener
* Field and Field Set create/retrieve/update/delete (CRUD) operations
* Pod, Customer, and Request CRUD operations  

## Prerequisites
This project requires:

* Java 8
* IntelliJ with the UI Designer plugin (for UI editing)
* Maven

## Getting Started
To set up sample code components:

1. Create a project directory.
2. Download the Swing UI example code from the [Context Service Sample Code GitHub](https://github.com/CiscoDevNet/context-service-sample-code) to your project directory.
3. Download the Context Service SDK from [Context Service Downloads](https://developer.cisco.com/fileMedia/download/dcf47513-a2cb-407c-b8a5-cc0d8f620405).
    
    You receive context-service-sdk-X.X.X.tar.gz where "X.X.X" is the current version of the SDK.
4. Run `prepareSDK.sh` with parameters \[targz-filename\].
    
    This sets up your connector.properties file.
5. Run `mvn clean install`.
7. [Register with Context Service](https://developer.cisco.com/site/context-service/documents/context-service-sdk-guide/index.gsp#register-your-application-with-context-service) to receive the connectionData string. 

    The connectionData string is required to connect to Context Service.
8. Save your connectionData string to connectiondata.txt in your project directory.

9. Run the script `run.sh` to start the application.

## Registering Application
To register with the Context Service and obtain a connection data string:

1. Run the script `register.sh` to begin registration.
2. It will launch a web browser.  Login using your organization admin account.
3. Click **Allow** to allow the application to access the Context Service.
4. A few seconds later, the browser will redirect and print the connection data string.  The connection data string will also be saved to a file with the name `connectiondata.txt` if it does not already exist.

## Usage notes

* To search, specify **key:value pairs** where: 
    * each key/value pair is colon delimited 
    * each pair is space-delimited.
   
   For example: Context_City:Boxborough
* (Optional) When creating Customers, Requests, and Pods you can specify which fieldset to use when making your chosen type.
  
## Adjusting the UI
To adjust the UI, change the following setting in IntelliJ:

* Settings > Editor > GUI Designer > Generate GUI into: Java source code

## Creating your own application
Use the `Utils.runIt()` method to create a simple application. The `Utils.runIt()` method takes a function which can use a local version of the Context Service SDK.

For example, this will call the `getStatus()` API:

     public static void main(String args[]) {
            Utils.runIt(ConnectionData.getConnectionData(), (contextServiceClient) -> {
                System.out.println("\n\n\nStatus:\n" + contextServiceClient.getStatus() + "\n\n");
            });
        }

## Disclaimer
This sample code is only a sample and is NOT guaranteed to be bug free and production quality. This is NOT intended to be used in production environment. You must adapt the code to work with your custom application.

## Support Notice

DevNet provides sample support on a “best effort” basis. Like any custom deployment, it is the responsibility of the partner and/or customer to ensure that the customization works correctly.

[https://developer.cisco.com/site/context-service](https://developer.cisco.com/site/context-service)
