package com.appdynamics.extensions.neo4j;


import com.google.common.collect.Maps;
import com.singularity.ee.agent.systemagent.api.exception.TaskExecutionException;
import org.junit.Test;

import java.util.Map;

public class Neo4jMonitorTest {

    public static final String CONFIG_ARG = "config-file";

    @Test
    public void testNeo4jMonitorExtension() throws TaskExecutionException {
        Neo4jMonitor neo4jMonitor = new Neo4jMonitor();
        Map<String, String> taskArgs = Maps.newHashMap();
        taskArgs.put(CONFIG_ARG, "src/test/resources/conf/config.yml");
        neo4jMonitor.execute(taskArgs, null);
    }
}
