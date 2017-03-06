# Run Context Service SDK End-To-End Sample Code

## End-To-End Sample Code
The end-to-end sample code helps you understand the general workflow of the Context Service SDK. Create a project directory and download the example code to this directory.

## Prerequisites
Before you run the example:

* Register with Context Service. You need the **ConnectionData** string to connect to Context Service. The connection data string is generated when you register with Context Service. Once you register and receive the connection data string, save the string to **connectiondata.txt** file in your project directory.
* Run this command in the root of your project directory to load the Context Service SDK and POM into the maven project:

    ```
./getSDK.sh
./updateBaseSDK.sh
``` 
   For more information, see [Getting Started](https://developer.cisco.com/site/context-service/discover/getting-started)

* To compile the code without running tests, run:
    `mvn -U clean install -DskipTests`
* To compile code and run all the necessary tests, run:
    `mvn -U clean install`

## Run the Example
Once you have installed all the SDK components, open the project in an IDE and explore the examples and tests.

## Disclaimer
This sample code is only a sample and is NOT guaranteed to be bug free and production quality. This is NOT intended to be used in production environment. You must adapt the code to work with your custom application.

## Support Notice

Support for the sample is provided on a "best effort" basis via DevNet. Like any custom deployment, it is the responsibility of the partner and/or customer to ensure that the customization works correctly.

[https://developer.cisco.com/site/context-service](https://developer.cisco.com/site/context-service)