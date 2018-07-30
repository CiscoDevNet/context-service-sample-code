# Run Context Service SDK End-To-End Sample Code
The end-to-end sample code helps you understand the general workflow of the Context Service SDK. Create a project directory and download the example code to this directory.

## Prerequisites
This project requires:

* Java 7+
* Maven

## Getting Started
This procedure is for Mac OS and Linux. Windows users should use Git Bash to follow this procedure.

To set up sample code components:

1. Create a project directory.
2. Download the End-To-End example code from the [Context Service Sample Code GitHub](https://github.com/CiscoDevNet/context-service-sample-code) to your project directory.
3. Download the Context Service SDK from [Context Service Downloads](https://pubhub.devnetcloud.com/media/context-service/docs/downloads/context-service-sdk-2.0.5.tar.gz).

    You receive context-service-sdk-X.X.X.tar.gz where "X.X.X" is the current version of the SDK.
4. Change to the End-To-End_Sample_Code directory, then run:

  `./prepareSDK.sh <path-to-context-service-sdk-X.X.X.tar.gz>`

  For example:

  `./prepareSDK.sh ~/Downloads/context-service-sdk-2.0.5.tar.gz`


    This installs the Context Service SDK in your project and configures your connector.properties file.
5. Run `mvn clean install`.
6. [Register with Context Service](https://developer.cisco.com/site/context-service/docs/#register-your-application) to receive the connection data string.

>The connection data string is required to connect to Context Service.

7. Save your connection data string to connectiondata.txt in your project directory.

## Compiling and Testing the End-To-End Sample Code

* To compile the code without running tests, run:
    `mvn -U clean install -DskipTests`
* To compile the code and run all necessary tests, run:
    `mvn -U clean install`

For more information, see [Getting Started with the Context Service SDK](https://developer.cisco.com/docs/context-service/#getting-started-with-context-service).

## Running the Example
After you have installed the SDK components, open the project in an IDE to explore the examples and tests.

## Packaging
After building, a ZIP file is available for distribution.

## Command-line Examples
To view command-line examples, run:

`./run.sh`

Examples:
* Generating a registration URL
* Parsing a connection data string
* Searching for pod, customer, or request

For example, to search for all customers with a Context_City of Boston and save to a file:


`./run.sh search --query "Context_City:Boston" --type customer --output customer.txt`

## Disclaimer
This sample code is only a sample and is NOT guaranteed to be bug free and production quality. This is NOT intended to be used in production environment. You must adapt the code to work with your custom application.

## Support Notice

DevNet provides sample support on a “best effort” basis. Like any custom deployment, it is the responsibility of the partner and/or customer to ensure that the customization works correctly.

[https://developer.cisco.com/site/context-service](https://developer.cisco.com/site/context-service)
