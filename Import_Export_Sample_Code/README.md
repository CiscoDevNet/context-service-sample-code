# Run Context Service SDK Import/Export Sample Code

## Import/Export Sample Code
The import/export sample code shows how to use the Context Service Java SDK to extract and insert data in Context Service.

Use the *export sample code* to extract your organization's objects in a set of JSON formatted files. You can export Customer, Request and POD objects that were modified within a specified time range.

The *import sample code* uses the JSON file created by the export sample code to insert Customer, Request, and POD objects for your organization. The import sample code does not preserve the original creation and last modified timestamps for objects. Although relationship between objects is maintained, object IDs are recreated and no longer have the original IDs.

Consider using the sample codes in Lab mode to test and build custom apps for your production use.

After compiling with `mvn clean install` you can run `export.sh` to export or `import.sh` to import.

## Getting Started
This procedure is for Mac OS and Linux. Windows users should use Git Bash to follow this procedure.

To set up sample code components:

1. Download the Context Service SDK from [Context Service Downloads](https://pubhub.devnetcloud.com/media/context-service/docs/downloads/context-service-sdk-2.0.5.tar.gz).

    You receive context-service-sdk-X.X.X.tar.gz where "X.X.X" is the current version of the SDK.

2. Change to the Import_Export_Sample_Code directory, then run:

  `./prepareSDK.sh <path-to-context-service-sdk-X.X.X.tar.gz>`

  For example:

  `./prepareSDK.sh ~/Downloads/context-service-sdk-2.0.5.tar.gz`


    This installs the Context Service SDK in your project and configures your connector.properties file.

3. Run `mvn clean install`.
4. [Register with Context Service](https://developer.cisco.com/site/context-service/docs/#register-your-application) to receive the connection data string.

>The connection data string is required to connect to Context Service.

5. Save your connection data string to connectiondata.txt in your project directory.

## Disclaimer
This sample code is only a sample and is NOT guaranteed to be bug free and production quality. This is NOT intended to be used in production environment. You must adapt the code to work with your custom application.

## Support Notice

DevNet provides sample support on a “best effort” basis. Like any custom deployment, it is the responsibility of the partner and/or customer to ensure that the customization works correctly.

[https://developer.cisco.com/site/context-service](https://developer.cisco.com/site/context-service)
