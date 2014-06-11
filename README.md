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


## Contributing ##

Always feel free to fork and contribute any changes directly via [GitHub][].

## Community ##

Find out more in the [AppDynamics Exchange][].

## Support ##

For any questions or feature request, please contact [AppDynamics Center of Excellence][].

**Version:** 1.0.0
**Controller Compatibility:** 3.7+

[Github]: https://github.com/Appdynamics/neo4j-monitoring-extension
[AppDynamics Exchange]: http://community.appdynamics.com/t5/AppDynamics-eXchange/idb-p/extensions
[AppDynamics Center of Excellence]: mailto:ace-request@appdynamics.com