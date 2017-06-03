# Run Context Service SDK Import/Export Sample Code

## Import/Export Sample Code
The import/export sample code shows how to use the Context Service Java SDK to extract and insert data in Context Service.

Use the **export sample code** to extract your organization's objects in a set of JSON formatted files. You can export Customer, Request and POD objects that were modified within a specified time range.

The **import sample code** uses the JSON file created by the export sample code to insert Customer, Request, and POD objects for your organization. The import sample code does not preserve the original creation and last modified timestamps for objects. Although relationship between objects is maintained, object IDs are recreated and no longer have the original IDs. 

Consider using the sample codes in Lab mode to test and build custom apps for your production use.

After compiling with `mvn clean install` you can run `export.sh` to export or `import.sh` to import.

w## Getting Started
To set up sample code components:

1. Download the Context Service SDK from [Context Service Downloads](https://developer.cisco.com/fileMedia/download/dcf47513-a2cb-407c-b8a5-cc0d8f620405).
    
    You receive context-service-sdk-X.X.X.tar.gz where "X.X.X" is the current version of the SDK.
    
2. Run `prepareSDK.sh` with parameter \[targz-filename\] 
    
    This sets up your connector.properties file.
    
3. Run `mvn clean install`.
4. [Register with Context Service](https://developer.cisco.com/site/context-service/documents/context-service-sdk-guide/index.gsp#register-your-application-with-context-service) to receive the connectionData string. 
    The connectionData string is required to connect to Context Service.
5. Save your connectionData string to connectiondata.txt in your project directory.

## Disclaimer
This sample code is only a sample and is NOT guaranteed to be bug free and production quality. This is NOT intended to be used in production environment. You must adapt the code to work with your custom application.

## Support Notice

DevNet provides sample support on a “best effort” basis. Like any custom deployment, it is the responsibility of the partner and/or customer to ensure that the customization works correctly.

[https://developer.cisco.com/site/context-service](https://developer.cisco.com/site/context-service)

