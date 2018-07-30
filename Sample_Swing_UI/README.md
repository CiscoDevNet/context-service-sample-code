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
This procedure is for Mac OS and Linux. Windows users should use Git Bash to follow this procedure.

To set up sample code components:

1. Create a project directory.
2. Download the Swing UI example code from the [Context Service Sample Code GitHub](https://github.com/CiscoDevNet/context-service-sample-code) to your project directory.
3. Download the Context Service SDK from [Context Service Downloads](https://pubhub.devnetcloud.com/media/context-service/docs/downloads/context-service-sdk-2.0.5.tar.gz).

    You receive context-service-sdk-X.X.X.tar.gz where "X.X.X" is the current version of the SDK.
4.  Change to the Sample_Swing_UI directory, then run:

  `./prepareSDK.sh <path-to-context-service-sdk-X.X.X.tar.gz>`

  For example:

  `./prepareSDK.sh ~/Downloads/context-service-sdk-2.0.5.tar.gz`

  This installs the Context Service SDK in your project and configures your connector.properties file.

5. Run `mvn clean install`.
7. [Register with Context Service](https://developer.cisco.com/site/context-service/docs/#register-your-application) to receive the connection data string.

    The connection data string is required to connect to Context Service.
8. Save your connection data string to connectiondata.txt in your project directory.

9. Start the application:
`./run.sh`

## Register Your Application
To register with Context Service and obtain a connection data string:

1. Run the script `register.sh` to begin registration.

  The registration web page opens in a browser.
2. Login using your organization admin account.
3. Click **Allow** to allow the application to access the Context Service.

  After a few seconds, the browser redirects and displays the connection data string in the URL. The connection data string is also saved to a file with the name `connectiondata.txt` if it does not already exist.

## Usage notes

* To search, specify **key:value pairs** where:
    * each key/value pair is colon delimited
    * each pair is space-delimited.

   For example: Context_City:Boxborough
* (Optional) When creating Customers, Requests, and Pods you can specify which fieldset to use when making your chosen type.

### Adjusting the UI
To adjust the UI, change the following setting in IntelliJ:

* Settings > Editor > GUI Designer > Generate GUI into: Java source code

### Create Your Own Application
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
