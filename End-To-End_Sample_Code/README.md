# Run Context Service SDK End-To-End Sample Code
The end-to-end sample code helps you understand the general workflow of the Context Service SDK. Create a project directory and download the example code to this directory.

## Prerequisites
The following is needed for this project:

* Java 7
* Maven

## Getting Started
To get started:

1. Go to https://developer.cisco.com/site/context-service/discover/getting-started/
2. There you will see a link to "Context Service Downloads": https://developer.cisco.com/fileMedia/download/dcf47513-a2cb-407c-b8a5-cc0d8f620405
3. You will get a context-service-sdk-X.X.X.tar.gz file
4. Run `prepareSDK.sh` with parameters [targz-filename]
* This sets up your connector.properties file
5. Run `mvn clean install`
6. Create the connectiondata.txt file 

## Creating connectiondata.txt file
Register with Context Service. You need the ConnectionData string to connect to Context Service. The connection data string is generated when you register with Context Service. Once you register and receive the connection data string, save the string to connectiondata.txt file in your project directory.

## Running the code
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