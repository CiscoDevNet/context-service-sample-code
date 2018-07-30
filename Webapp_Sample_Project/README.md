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
This procedure is for Mac OS and Linux. Windows users should use Git Bash to follow this procedure.

To set up sample code components:

1. Create a project directory.

2. Download the sample code from the [Context Service Sample Code GitHub](https://github.com/CiscoDevNet/context-service-sample-code) and extract context-service-sample-code to your project directory.

3. Download the Context Service SDK from [Context Service Downloads](https://pubhub.devnetcloud.com/media/context-service/docs/downloads/context-service-sdk-2.0.5.tar.gz).

    You receive context-service-sdk-X.X.X.tar.gz where "X.X.X" is the current version of the SDK.

4. Change to the Webapp_Sample_Project directory, then run:

  `./prepareSDK.sh <path-to-context-service-sdk-X.X.X.tar.gz>`

  For example:

  `./prepareSDK.sh ~/Downloads/context-service-sdk-2.0.5.tar.gz`


  This installs the Context Service SDK in your project and configures your connector.properties file.
  
>You can connect to Context Service through a proxy by adding `System.setProperty("contextservice.proxyURL", "http://<proxy_host>:<port_number>");` to the `getInitializedContextServiceClient()` and `getInitializedManagementConnector()` methods in Utils.java.

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

   This configures each Tomcat instance containing the `connection.info.file` to point to the file you are using to store the connection data string.

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

If you already have a connection data string, you can enter it into the file in this format:

```
connection.data=YOUR_CONNECTION_DATA_STRING_HERE
```

## REST API Examples

### Prerequisites
Before running the REST API Examples, you must:

* Use the Management Connector to successfully register with Context Service.
* Store the connection data string to the file specified by the `connection.info.file` property.

These examples can be run with a REST client such as Postman.

### Create Context Service Object

URL Syntax:
```
POST http://localhost:8080/rest
```

The `type` field must be one of `pod` (activity), `customer`, `request`, or `detail`. The `detail` type can also use the subtypes `detail.comment` and `detail.feedback`.
 
Use the `fieldsets` parameter to assign fieldsets to an object. Each object must have at least one fieldset assigned to it.


Use the `dataElements` array to add field information to an object. `dataElements` is an array of objects that contain the `key`, `value`, and `type` properties. The `key` property contains the field name as a string. The `value` property contains the field value. The `type` property contains the Context Service field data type as a string. 

| Type Name in Cisco Webex Control Hub | Context Service Field Data Type |
|------|------|
| Toggle | boolean |
| Decimal | double |
| Number | integer | 
| Short Text | string |

The items in the `dataElements` array must be fields from the fieldsets assigned to the object.

Each type of object in Context Service has different association restrictions:

 * `pod`—You can optionally associate a `pod` with a customer or a request. You can also associate a `pod` with a customer and a request. Enter the `id` value of a `customer` type object in the `customerId` parameter. Enter the `id` value of a `request` type object in the `parentId` parameter.
 * `customer`—You cannot associate a `customer` with another type of Context Service object.
 * `request`—You must associate a `request` with a customer. Enter the `id` value of a `customer` type object in the `customerId` parameter.
 * `detail`—You must associate a `detail` with either a request or an activity. Enter the `id` value of either a `request` or an `activity` type object in the `parentId` parameter.
 
![](https://pubhub.devnetcloud.com/media/context-service/docs/cs-sdk-guide/images/ContextObjectAssociations.png)

>Activity type objects can also use the optional `mediaType` parameter to set the activity mediaType. Valid values are `chat`, `email`, `event`, `mobile`, `social`, `video`, `voice`, or `web`.

This example demonstrates how to create an activity. The type must be `pod` to create an activity:

```
POST http://localhost:8080/rest
{
    "type" : "pod",
    "mediaType" : "chat",
    "fieldsets": ["cisco.base.pod"],
    "dataElements": [
        {
            "key": "Context_Notes",
            "value": "testing at 3:16",
            "type": "string"
        }
    ]
}
```

This example demonstrates how to create a request:
```
POST http://localhost:8080/rest
{
    "type" : "request",
    "customerId" : "<your-customerId-value>",
    "fieldsets": ["cisco.base.request"],
    "dataElements": [
        {
            "key": "Context_Description",
            "value": "Test description",
            "type": "string"
        }
    ]
}
```

This example demonstrates how to create an activity with a custom boolean field:
```
POST http://localhost:8080/rest
{
    "type" : "pod",
    "mediaType" : "chat",
    "fieldsets": ["<your-custom-fieldset>"],
    "dataElements": [
        {
            "key": "<your-custom-field>",
            "value": true,
            "type": "boolean"
        }
    ]
}
```

This example demonstrates how to create an activity with a custom integer field:
```
POST http://localhost:8080/rest
{
    "type" : "pod",
    "mediaType" : "chat",
    "fieldsets": ["<your-custom-fieldset>"],
    "dataElements": [
        {
            "key": "<your-custom-field>",
            "value": 123,
            "type": "integer"
        }
    ]
}
```

This example demonstrates how to create an activity with a custom double field:
```
POST http://localhost:8080/rest
{
    "type" : "pod",
    "mediaType" : "chat",
    "fieldsets": ["<your-custom-fieldset>"],
    "dataElements": [
        {
            "key": "<your-custom-field>",
            "value": 123.4,
            "type": "double"
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

The \<type\> in the URL must be one of `pod`, `customer`, `request`, or `detail`. The `detail` type can also use the subtypes `detail.comment` and `detail.feedback`.

Example response:
```
{
    "id": "<your-id>",
    "type": "pod",
    "mediaType: "voice",
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
To update an object, you must include the `lastUpdated` field and match the existing object's lastUpdated value.

URL syntax:
```
PUT http://localhost:8080/rest/<type>/<your-id>
```

The \<type\> in the URL must be one of `pod`, `customer`, `request`, or `detail`. The `detail` type can also use the subtypes `detail.comment` and `detail.feedback`.

>You cannot update closed `request` type objects. Context Service automatically closes each request 5 days after the last update on the request or any activities associated with the request. `detail` type objects are automatically created closed and cannot be updated.

For example:
```
PUT http://localhost:8080/rest/pod/<your-id>
{
	"type" : "pod",
	"fieldsets": ["cisco.base.pod"],
	"dataElements": [
        {
            "key": "Context_Notes",
            "value": "testing at 4:25",
            "type": "string"
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

The \<type\> in the URL must be one of `pod`, `customer`, `request`, or `detail`. The `detail` type can also use the subtypes `detail.comment` and `detail.feedback`.

### Search
The "operation" field can be "or" or "and".

For information on how search works and the special search keys used to search for object metadata, see the [Context Service SDK guide](https://developer.cisco.com/docs/context-service/#search-objects).

URL syntax:
```
POST http://localhost:8080/rest/search/<type>
```

The \<type\> in the URL must be one of `pod`, `customer`, `request`, or `detail`. The `detail` type can also use the subtypes `detail.comment` and `detail.feedback`.

This example shows searching for a `pod` (activity):

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
