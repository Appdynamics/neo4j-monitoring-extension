neo4j-monitoring-extension
==========================

An AppDynamics extension to be used with a stand alone Java machine agent to provide metrics for Neo4j servers.


## Use Case ##

Neo4j is a highly scalable, robust (fully ACID) native graph database. The Neo4j monitoring extension captures statistics from the Neo4j servers and displays them in the AppDynamics Metric Browser.

## Prerequisites ##

By default, a neo4j server does not have remote JMX enabled.
Please make sure you uncomment out the following JMX parameters in the neo4j-wrapper.conf.

```
    ﻿wrapper.java.additional=-Dcom.sun.management.jmxremote.port=3637
    wrapper.java.additional=-Dcom.sun.management.jmxremote.authenticate=true
    wrapper.java.additional=-Dcom.sun.management.jmxremote.ssl=false
    wrapper.java.additional=-Dcom.sun.management.jmxremote.password.file=conf/jmx.password
    wrapper.java.additional=-Dcom.sun.management.jmxremote.access.file=conf/jmx.access

    # Some systems cannot discover host name automatically, and need this line configured:
    wrapper.java.additional=-Djava.rmi.server.hostname=<YOUR IP>
```




## Metrics Provided ##

* Node Cache and Relationship Cache metrics.
* Store Sizes, Primitive Count metrics.
* Transaction metrics. etc.


In addition to the above metrics, we also add a metric called "HEALTH_CHECK" with a value -1 when an error occurs and 1 when the metrics collection is successful.

Note : By default, a Machine agent or a AppServer agent can send a fixed number of metrics to the controller. To change this limit, please follow the instructions mentioned [here](http://docs.appdynamics.com/display/PRO14S/Metrics+Limits).

## Installation ##

1. Run "mvn clean install" and find the Neo4jMonitor.zip file in the "target" folder. You can also download the Neo4jMonitor.zip from [AppDynamics Exchange][].
2. Unzip as "Neo4jMonitor" and copy the "Neo4jMonitor" directory to `<MACHINE_AGENT_HOME>/monitors`



## Configuration ##

Note : Please make sure to not use tab (\t) while editing yaml files. You may want to validate the yaml file using a [yaml validator](http://yamllint.com/)

1. Configure the Neo4j instances by editing the config.yml file in `<MACHINE_AGENT_HOME>/monitors/Neo4jMonitor/`.
2. Configure the MBeans in the config.yml. By default, "org.neo4j" is all that you may need. But you can add more mbeans as per your requirement.
   You can also add excludePatterns (regex) to exclude any metric tree from showing up in the AppDynamics controller.

   For eg.

   ```
        servers:
          - host: "192.168.57.102"
            port: 3637
            username: "monitor"
            password: "Neo4j"
            displayName: "myUbuntu"
          - host: "192.168.57.103"
            port: 3637
            username: "monitor"
            password: "Neo4j"
            displayName: "myDebian"


        # neo4j mbeans. Exclude patterns with regex can be used to exclude any unwanted metrics.
        mbeans:
          - domainName: "org.neo4j"
            excludePatterns: [
              "kernel#0|Store file sizes|.*"
            ]

        # number of concurrent tasks
        numberOfThreads: 10

        #timeout for the thread
        threadTimeout: 30

        #prefix used to show up metrics in AppDynamics
        metricPrefix:  "Custom Metrics|Neo4j|"
   ```

   In the above config file, metrics are being pulled from one mbean domains. Note that the patterns mentioned in the "excludePatterns" will be excluded from showing up in the AppDynamics dashboard.

3. Configure the path to the config.yml file by editing the <task-arguments> in the monitor.xml file in the `<MACHINE_AGENT_HOME>/monitors/Neo4jMonitor/` directory. Below is the sample

     ```
     <task-arguments>
         <!-- config file-->
         <argument name="config-file" is-required="true" default-value="monitors/Neo4jMonitor/config.yml" />
          ....
     </task-arguments>
    ```

###Cluster level metrics : 

We support cluster level metrics only if each node in the cluster has a separate machine agent installed on it. There are two configurations required for this setup 

1. Make sure that nodes belonging to the same cluster has the same <tier-name> in the <MACHINE_AGENT_HOME>/conf/controller-info.xml, we can gather cluster level metrics.  The tier-name here should be your cluster name. 

2. Make sure that in every node in the cluster, the <MACHINE_AGENT_HOME>/monitors/Neo4jMonitor/config.yaml should emit the same metric path. To achieve this make the displayName to be empty string and remove the trailing "|" in the metricPrefix.  

To make it more clear,assume that Neo4j "Node A" and Neo4j "Node B" belong to the same cluster "ClusterAB". In order to achieve cluster level as well as node level metrics, you should do the following
        
1. Both Node A and Node B should have separate machine agents installed on them. Both the machine agent should have their own Neo4j extension.
    
2. In the Node A's and Node B's machine agents' controller-info.xml make sure that you have the tier name to be your cluster name , "ClusterAB" here. Also, nodeName in controller-info.xml is "Node A" and "Node B" resp.
        
3. The config.yaml for Node A and Node B should be

```
    servers:
      - host: "192.168.57.102"
        port: 3637
        username: "monitor"
        password: "Neo4j"
        displayName: ""


    # neo4j mbeans. Exclude patterns with regex can be used to exclude any unwanted metrics.
    mbeans:
      - domainName: "org.neo4j"
        excludePatterns: [
          "kernel#0|Store file sizes|.*"
        ]

    # number of concurrent tasks
    numberOfThreads: 10

    #timeout for the thread
    threadTimeout: 30

    #prefix used to show up metrics in AppDynamics
    metricPrefix:  "Custom Metrics|Neo4j"
```


( Note :: Neo4jMonitor extension would report a lot of metrics. If you don't want to show some metrics in your dashboard use the excludePatterns in the config.yaml to filter them. Also, by default, a Machine agent can send a fixed number of metrics to the controller. To change this limit, please follow the instructions mentioned http://docs.appdynamics.com/display/PRO14S/Metrics+Limits.)
        
Now, if Node A and Node B are reporting say a metric called ReadLatency to the controller, with the above configuration they will be reporting it using the same metric path.
        
Node A reports Custom Metrics | ClusterAB | ReadLatency = 50 
Node B reports Custom Metrics | ClusterAB | ReadLatency = 500
        
The controller will automatically average out the metrics at the cluster (tier) level as well. So you should be able to see the cluster level metrics under
        
Application Performance Management | Custom Metrics | ClusterAB | ReadLatency = 225
        
Also, now if you want to see individual node metrics you can view it under
        
Application Performance Management | Custom Metrics | ClusterAB | Individual Nodes | Node A | ReadLatency = 50 
Application Performance Management | Custom Metrics | ClusterAB | Individual Nodes | Node B | ReadLatency = 500



Please note that for now the cluster level metrics are obtained by the averaging all the individual node level metrics in a cluster.


## Contributing ##

Always feel free to fork and contribute any changes directly via [GitHub][].

## Community ##

Find out more in the [AppDynamics Exchange][].

## Support ##

For any questions or feature request, please contact [AppDynamics Center of Excellence][].

**Version:** 1.0.0
**Controller Compatibility:** 3.7+
**Neo4j Version Tested On:** Enterprise 2.0.2

[Github]: https://github.com/Appdynamics/neo4j-monitoring-extension
[AppDynamics Exchange]: http://community.appdynamics.com/t5/AppDynamics-eXchange/idb-p/extensions
[AppDynamics Center of Excellence]: mailto:ace-request@appdynamics.com
