# Simple Workitem Handler


## To install the WIH

1. Set up your local maven setting to point to guvnor (the BPM maven repository) 
2. Issue `maven deploy`
3. From **Project Editor**:

    - select **Dependenties**
    - click **Add from repository**
    - choose this maven project
     
4. From **Project Editor** open **Deployment Descriptor** and add the new WIH 

    - `SimpleWIH`
    - `new example.SimpleWorkItemHandler()`

5. Open Work Item Definitions file and replace the last square bracket with the following 

	  [
	    "name" : "SimpleWIH",
	    "displayName" : "SimpleWIH",
	    "parameters" : [
	        "count" : new StringDataType()
	    ],
	    "icon" : "db_insert.png"
	  ]

 