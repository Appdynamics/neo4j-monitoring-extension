/*
 *   Copyright 2018. AppDynamics LLC and its affiliates.
 *   All Rights Reserved.
 *   This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 *   The copyright notice above does not evidence any actual or intended publication of such source code.
 *
 */

package com.appdynamics.extensions.neo4j;


import com.appdynamics.extensions.jmx.JMXConnectionConfig;
import com.appdynamics.extensions.jmx.JMXConnectionUtil;
import com.appdynamics.extensions.jmx.MBeanKeyPropertyEnum;
import com.appdynamics.extensions.neo4j.config.MBeanData;
import com.appdynamics.extensions.neo4j.config.Server;
import com.appdynamics.extensions.util.MetricUtils;
import com.google.common.base.Strings;
import org.apache.log4j.Logger;

import javax.management.MBeanAttributeInfo;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

public class Neo4jMonitorTask implements Callable<Neo4jMetrics> {

    public static final String METRICS_SEPARATOR = "|";
    private Server server;
    private MBeanData[] mbeansData;
    private JMXConnectionUtil jmxConnector;
    private Map<String,MBeanData> mbeanLookup;
    public static final Logger logger = Logger.getLogger(Neo4jMonitorTask.class);

    public Neo4jMonitorTask(Server server, MBeanData[] mbeansData) {
        this.server = server;
        this.mbeansData = mbeansData;
        createMBeansLookup(mbeansData);
    }

    private void createMBeansLookup(MBeanData[] mbeansData) {
        mbeanLookup = new HashMap<String, MBeanData>();
        if(mbeansData != null){
            for(MBeanData mBeanData : mbeansData){
                mbeanLookup.put(mBeanData.getDomainName(),mBeanData);
            }
        }
    }

    @Override
    public Neo4jMetrics call() throws Exception {
        Neo4jMetrics neo4jMetrics = new Neo4jMetrics();
        neo4jMetrics.setDisplayName(server.getDisplayName());
        try{
            jmxConnector = new JMXConnectionUtil(new JMXConnectionConfig(server.getHost(),server.getPort(),server.getUsername(),server.getPassword()));
            JMXConnector connector = jmxConnector.connect();
            if(connector != null){
                Set<ObjectInstance> allMbeans = jmxConnector.getAllMBeans();
                if(allMbeans != null) {
                    Map<String, String> filteredMetrics = applyExcludePatternsAndExtractMetrics(allMbeans);
                    neo4jMetrics.setMetrics(filteredMetrics);
                    neo4jMetrics.getMetrics().put("HEALTH_CHECK","1");
                }
            }
        }
        catch(Exception e){
            logger.error("Error JMX-ing into the server :: " +neo4jMetrics.getDisplayName() + e);
            neo4jMetrics.getMetrics().put("HEALTH_CHECK","-1");
        }
        finally{
            jmxConnector.close();
        }
        return neo4jMetrics;
    }



    private Map<String, String> applyExcludePatternsAndExtractMetrics(Set<ObjectInstance> allMbeans) {
        Map<String,String> filteredMetrics = new HashMap<String, String>();
        for(ObjectInstance mbean : allMbeans){
            ObjectName objectName = mbean.getObjectName();
            //consider only the the metric domains (org.apache.cassandra.metrics) mentioned in the config
            if(isDomainConfigured(objectName)){
                MBeanData mBeanData = mbeanLookup.get(objectName.getDomain());
                Set<String> excludePatterns = mBeanData.getExcludePatterns();
                MBeanAttributeInfo[] attributes = jmxConnector.fetchAllAttributesForMbean(objectName);
                if(attributes != null) {
                    for (MBeanAttributeInfo attr : attributes) {
                        // See we do not violate the security rules, i.e. only if the attribute is readable.
                        if (attr.isReadable()) {
                            Object attribute = jmxConnector.getMBeanAttribute(objectName, attr.getName());
                            //AppDynamics only considers number values
                            if (attribute != null && attribute instanceof Number) {
                                String metricKey = getMetricsKey(objectName,attr);
                                if (!isKeyExcluded(metricKey, excludePatterns)) {
                                    if (logger.isDebugEnabled()) {
                                        logger.debug("Metric key:value before rounding = "+ metricKey + ":" + String.valueOf(attribute));
                                    }
                                    String attribStr = MetricUtils.toWholeNumberString(attribute);
                                    filteredMetrics.put(metricKey, attribStr);
                                } else {
                                    if (logger.isDebugEnabled()) {
                                        logger.info(metricKey + " is excluded");
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return filteredMetrics;
    }



    /**
     * Checks if the given metric key matches any exclude patterns
     *
     * @param metricKey
     * @param excludePatterns
     * @return true if match, false otherwise
     */
    private boolean isKeyExcluded(String metricKey, Set<String> excludePatterns) {
        for(String excludePattern : excludePatterns){
            if(metricKey.matches(escapeText(excludePattern))){
                return true;
            }
        }
        return false;
    }

    private String escapeText(String excludePattern) {
        return excludePattern.replaceAll("\\|","\\\\|");
    }

    private String getMetricsKey(ObjectName objectName,MBeanAttributeInfo attr) {
        // Standard jmx keys. {instance,name,name0 etc.}
        String instance = objectName.getKeyProperty("instance");
        String name = objectName.getKeyProperty(MBeanKeyPropertyEnum.NAME.toString());
        String name0 = objectName.getKeyProperty("name0");
        StringBuilder metricsKey = new StringBuilder();
        metricsKey.append(Strings.isNullOrEmpty(instance) ? "" : instance + METRICS_SEPARATOR);
        metricsKey.append(Strings.isNullOrEmpty(name) ? "" : name + METRICS_SEPARATOR);
        metricsKey.append(Strings.isNullOrEmpty(name0) ? "" : name0 + METRICS_SEPARATOR);
        metricsKey.append(attr.getName());

        return metricsKey.toString();
    }


    private boolean isDomainConfigured(ObjectName objectName) {
        return (mbeanLookup.get(objectName.getDomain()) != null);
    }
}
