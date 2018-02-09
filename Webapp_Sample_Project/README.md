# WebApp Sample Code
A simple REST interface that wraps the Context Service SDK to facilitate integration with non-Java-based applications.

## Introduction
This sample provides a REST interface for the following operations:

* Create (POST) an object
* Retrieve (GET) a single object by ID
* Update (PUT) an object
* Delete (DELETE) an object
* Search (POST) using various search parameters
* Get status (GET)

This sample also provides examples of how an application can register and deregister with the Context Service.

This sample does not provide for multi-tenancy; only one organization's objects can be managed.

## Prerequisites
This project requires:

* Java 8
* Maven 3.x
* Tomcat 8.x

## Getting Started
This procedure is for Mac OS and Linux. Windows users should use Bash or Cygwin to follow this procedure.

To set up sample code components:

1. Create a project directory.

2. Download the sample code from the [Context Service Sample Code GitHub](https://github.com/CiscoDevNet/context-service-sample-code) and extract context-service-sample-code to your project directory.

3. Download the Context Service SDK from [Context Service Downloads](https://pubhub.devnetcloud.com/media/context-service/docs/downloads/context-service-sdk-2.0.3.tar.gz).

    You receive context-service-sdk-X.X.X.tar.gz where "X.X.X" is the current version of the SDK.

4. Change to the Webapp_Sample_Project directory, then run:

  `./prepareSDK.sh <path-to-context-service-sdk-X.X.X.tar.gz>`

  For example:

  `./prepareSDK.sh ~/Downloads/context-service-sdk-2.0.3.tar.gz`


  This installs the Context Service SDK in your project and configures your connector.properties file.

5. Change to the context-service-sample-code root directory, then run:

 `mvn clean install -DskipTests`

   After running Maven, webapp artifacts are generated in the following directories:
   * REST_API_Sample/target/rest.war
   * Mgmt_Connector_Sample/target/management.war

   >Each of these webapps must run in their own Java Virtual Machine. In this sample, the two webapps run in their own instance of Tomcat.

6. Download a copy of Tomcat 8 in tar.gz format.

7. Prepare the webapps to run in Tomcat. Change to the Webapp_Sample_Project/Deploy_Server directory, then run:

   `./prepareTomcat.sh <path-to-tomcat.tgz> <path-to-connectiondata.properties>`

   This sets the path to the connectiondata.properties file used to store connection data.

   >You can set the path to the connectiondata.properties file before creating it.

   For example:

   `./prepareTomcat.sh ~/Downloads/apache-tomcat-8.5.16.tar.gz ~/connectiondata.properties`

   This configures each Tomcat instance containing the `connection.info.file` to point to a file that will store the connection data string.

8. Start the Tomcat instances:

   `./startTomcats.sh`

9. Launch the Management webapp at [http://localhost:8082/management](http://localhost:8082/management)

10. Click the Register with Context Service button. Ensure that the Enable Hybrid box is checked.  

    After several redirects, the application saves your connection data to the connection data properties file and displays a "Registration Complete" message.

    >You can also use this application to deregister with Context Service.

11. Restart the Tomcat instances to pick up the change:

    ```
    ./stopTomcats.sh
    ./startTomcats.sh
    ```

12. Start using the REST API and follow the examples below.

    The application is available at `http://localhost:8080/rest`

## Management Connector Webapp Notes

The Management Connector webapp is crucial to ensuring your application runs without downtime. This webapp is responsible for listening to the credentials changed event and updating the connection data as needed. If the connection data string is not kept up-to-date, the Context Service SDK may fail to initialize.  

The sample Management Connector webapp writes the connection data file to a plain text file as specified by the environment variable `connection.info.file` on the local filesystem. A more secure solution is outside the scope of this sample.

If you already have a connection data string, you can put it into the file in this format:

```
connection.data=YOUR_CONNECTION_DATA_STRING_HERE
```

## REST API Examples

### Prerequisites
Before running the REST API Examples, you must:

* Use the Management Connector to successfully register with Context Service
* Store the connection data string to the file specified by the `connection.info.file` property

These examples can be run with a REST client such as Postman.

### Create Context Service Object
The "type" field can be "pod" (activity), "customer", or "request".

The "fieldsets" parameter must specify one of the available fieldsets.

The items in "dataElements" must be from fields in the specified fieldsets.

The example below demonstrates how to create an activity. The type must be "pod" to create an activity.

```
POST http://localhost:8080/rest
{
	"type" : "pod",
	"fieldsets": ["cisco.base.pod"],
	"dataElements": [
        {
            "key": "Context_Notes",
            "value": "testing at 3:16"
        }
    ]
}
```

### Get Context Service Object
Returns a single context object by ID.

URL Syntax:
```
GET http://localhost:8080/rest/<type>/<your-id>
```

The <type> in the URL must be one of `pod`, `customer`, or `request`.

Example response:
```
{
    "id": "<your-id>",
    "type": "pod",
    "fieldsets": [
        "cisco.base.pod"
    ],
    "dataElements": [
        {
            "key": "Context_Notes",
            "value": "testing at 3:16",
            "type": "string"
        }
    ],
    "created": {
        "date": "2017-07-16T07:49:44.184Z"
    },
    "lastUpdated": {
        "date": "2017-07-16T07:49:44.184Z"
    }
}
```

### Update Context Service Object
To update a pod, you must include the "lastUpdated" field and match the existing object's lastUpdated value.

URL syntax:
```
PUT http://localhost:8080/rest/<type>/<your-id>
```

For example:
```
PUT http://localhost:8080/rest/pod/<your-id>
{
	"type" : "pod",
	"fieldsets": ["cisco.base.pod"],
	"dataElements": [
        {
            "key": "Context_Notes",
            "value": "testing at 4:25"
        }
    ],
    "lastUpdated": {
        "date": "2017-07-15T15:51:35.214Z"
    }
}
```

### Delete Context Service Object
Delete is only available in lab mode.

URL Syntax:
```
DELETE http://localhost:8080/rest/<type>/<your-id>
```

### Search
The "operation" field can be "or" or "and".

For information on how search works and the special search keys used to search for object metadata, see the [Context Service SDK guide](https://developer.cisco.com/docs/context-service/#search-objects).

URL syntax:
```
POST http://localhost:8080/rest/search/<type>
```

This example shows searching for a "pod" (activity):
```
POST http://localhost:8080/rest/search/pod
{
	"operation" : "or",
	"query" : {
		"Context_Notes" : ["testing at 3:16"]
	}
}
```

This example shows searching with a date range:
```
POST http://localhost:8080/rest/search/pod
{
	"operation" : "or",
	"query" : {
		"Context_Notes" : ["testing at 4:25"],
		"startDate" : ["2017-07-15T15:51:35.214Z"]
	}
}
```

### Get Status
Get Status returns the Context Service Client SDK status. See the [Context Service SDK guide](https://developer.cisco.com/docs/context-service/#connect-to-context-service/check-connection-state) for more details.
```
GET http://localhost:8080/rest/status
```

## Disclaimer
This sample code is only a sample and is NOT guaranteed to be bug free and production quality. This is NOT intended to be used in production environment. You must adapt the code to work with your custom application.

## Support Notice

DevNet provides sample support on a “best effort” basis. Like any custom deployment, it is the responsibility of the partner and/or customer to ensure that the customization works correctly.

[https://developer.cisco.com/site/context-service](https://developer.cisco.com/site/context-service)
