package com.appdynamics.extensions.neo4j;


import java.util.HashMap;
import java.util.Map;

public class Neo4jMetrics {

    private String displayName;
    private Map<String,String> metrics;

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public Map<String, String> getMetrics() {
        if(metrics == null){
            metrics = new HashMap<String, String>();
        }
        return metrics;
    }

    public void setMetrics(Map<String, String> metrics) {
        this.metrics = metrics;
    }
}
