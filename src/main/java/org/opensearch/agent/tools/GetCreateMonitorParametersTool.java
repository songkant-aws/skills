/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.agent.tools;

import java.security.AccessController;
import java.security.PrivilegedExceptionAction;
import java.util.Collections;
import java.util.Map;

import org.opensearch.client.Client;
import org.opensearch.core.action.ActionListener;
import org.opensearch.ml.common.spi.tools.Tool;
import org.opensearch.ml.common.spi.tools.ToolAnnotation;

import com.google.gson.Gson;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;

@Log4j2
@ToolAnnotation(GetCreateMonitorParametersTool.TYPE)
public class GetCreateMonitorParametersTool implements Tool {
    public static final String TYPE = "GetCreateMonitorParametersTool";

    private static final String DEFAULT_DESCRIPTION =
        "Use this tool to get parameters required by creating a new monitor. The goal is to parse input parameters when user asks for creating a new monitor based on the data in the cluster."
            + " The tool takes arguments with"
            + " name of the monitor,"
            + " monitor_type which defines the type of the monitor (options are query_level_monitor, bucket_level_monitor, cluster_metrics_monitor and doc_level_monitor, default is query_level_monitor),"
            + " searchType which defines the way of creating monitor via UI (options are graph and query, default is graph),"
            + " frequency which defines the period of monitoring (options are interval, daily, weekly and monthly, default is interval),"
            + " the other fields differ based on different monitor_type."
            + "1. If the monitor_type is query_level_monitor or bucket_level_monitor, an example of parameters is"
            + "         {\n"
            + "            name: 'test monitor',\n"
            + "            monitor_type: 'query_level_monitor',\n"
            + "            frequency: 'interval',\n"
            + "            timezone: 'Japan',\n"
            + "            daily: 0,\n"
            + "            interval: 1,\n"
            + "            unit: 'MINUTES',\n"
            + "            weekly_mon: false,\n"
            + "            weekly_tue: false,\n"
            + "            weekly_wed: false,\n"
            + "            weekly_thur: false,\n"
            + "            weekly_fri: false,\n"
            + "            weekly_sat: false,\n"
            + "            weekly_sun: false,\n"
            + "            monthly_type: 'day',\n"
            + "            monthly_day: 1,\n"
            + "            searchType: 'graph',\n"
            + "            index: 'opensearch_dashboards_sample_data_ecommerce',\n"
            + "            timefield: 'timestamp',\n"
            + "            aggregationType: 'count',\n"
            + "            aggregationField: 'bytes',\n"
            + "            groupByField: 'bytes',\n"
            + "            filterField: 'memory',\n"
            + "            filterFieldType: 'number',\n"
            + "            filterOperator: 'is',\n"
            + "            filterValue: 'Japan',\n"
            + "            bucketValue: 1,\n"
            + "            bucketUnitOfTime: 'h',\n"
            + "            trigger_name: 'test trigger',\n"
            + "            trigger_severity: 1,\n"
            + "            trigger_threshold_enum: 'ABOVE',\n"
            + "            trigger_threshold: 1000,\n"
            + "        }"
            + "2. If the monitor_type is cluster_metrics_monitor, an example of parameters is"
            + "{\n"
            + "                 name: 'test monitor',\n"
            + "                 monitor_type: 'cluster_metrics_monitor',\n"
            + "                \"index\": [\n"
            + "                    \"opensearch_dashboards_sample_data_logs\"\n"
            + "                ],\n"
            + "                \"schedule\": {\n"
            + "                    \"period\": {\n"
            + "                        \"unit\": \"MINUTES\",\n"
            + "                        \"interval\": 1\n"
            + "                    },\n"
            + "                    \"timezone\": \"Japan\",\n"
            + "                    \"daily\": 2,\n"
            + "                    \"monthly\": {\n"
            + "                        \"type\": \"day\",\n"
            + "                        \"day\": 1\n"
            + "                    },\n"
            + "                    \"weekly\": {\n"
            + "                        \"thu\": true,\n"
            + "                        \"tue\": true,\n"
            + "                        \"wed\": false,\n"
            + "                        \"thur\": false,\n"
            + "                        \"sat\": false,\n"
            + "                        \"fri\": false,\n"
            + "                        \"mon\": false,\n"
            + "                        \"sun\": false\n"
            + "                    },\n"
            + "                    \"frequency\": \"weekly\"\n"
            + "                },\n"
            + "                \"search\": {\n"
            + "                    \"searchType\": \"clusterMetrics\",\n"
            + "                    \"bucketValue\": 1,\n"
            + "                    \"timeField\": \"\",\n"
            + "                    \"bucketUnitOfTime\": \"h\",\n"
            + "                    \"groupBy\": [],\n"
            + "                    \"filters\": [],\n"
            + "                    \"aggregations\": []\n"
            + "                },\n"
            + "                \"triggers\": {\n"
            + "                    \"test trigger 3\": {\n"
            + "                        \"value\": 10000,\n"
            + "                        \"enum\": \"ABOVE\"\n"
            + "                    }\n"
            + "                },\n"
            + "                \"monitor_type\": \"cluster_metrics_monitor\"\n"
            + "            }"
            + "3. If the monitor_type is doc_level_monitor, an example of parameters is"
            + "{\n"
            + "                 name: 'test monitor',\n"
            + "                 monitor_type: 'doc_level_monitor',\n"
            + "                \"index\": [\n"
            + "                    \"opensearch_dashboards_sample_data_logs\"\n"
            + "                ],\n"
            + "                \"schedule\": {\n"
            + "                    \"period\": {\n"
            + "                        \"unit\": \"MINUTES\",\n"
            + "                        \"interval\": 1\n"
            + "                    },\n"
            + "                    \"timezone\": \"Japan\",\n"
            + "                    \"daily\": 4,\n"
            + "                    \"monthly\": {\n"
            + "                        \"type\": \"day\",\n"
            + "                        \"day\": 5\n"
            + "                    },\n"
            + "                    \"weekly\": {\n"
            + "                        \"tue\": false,\n"
            + "                        \"wed\": false,\n"
            + "                        \"thur\": false,\n"
            + "                        \"sat\": false,\n"
            + "                        \"fri\": false,\n"
            + "                        \"mon\": false,\n"
            + "                        \"sun\": false\n"
            + "                    },\n"
            + "                    \"frequency\": \"monthly\"\n"
            + "                },\n"
            + "                \"search\": {\n"
            + "                    \"searchType\": \"graph\"\n"
            + "                },\n"
            + "                \"triggers\": {\n"
            + "                    \"test trigger 4\": [\n"
            + "                        {\n"
            + "                            \"query\": {\n"
            + "                                \"expression\": \"name=test-query-4\",\n"
            + "                                \"field\": \"bytes\",\n"
            + "                                \"query\": 1,\n"
            + "                                \"queryName\": \"test-query\",\n"
            + "                                \"operator\": \"is\",\n"
            + "                                \"tags\": []\n"
            + "                            },\n"
            + "                            \"script\": {\n"
            + "                                \"source\": \"ctx.results[0].hits.total.value > 0\",\n"
            + "                                \"lang\": \"painless\"\n"
            + "                            }\n"
            + "                        }\n"
            + "                    ]\n"
            + "                },\n"
            + "                \"monitor_type\": \"doc_level_monitor\",\n"
            + "                \"doc_level_input\": {\n"
            + "                    \"queries\": [\n"
            + "                        {\n"
            + "                            \"field\": \"bytes\",\n"
            + "                            \"query\": 1,\n"
            + "                            \"queryName\": \"test-query\",\n"
            + "                            \"operator\": \"is\",\n"
            + "                            \"tags\": []\n"
            + "                        }\n"
            + "                    ]\n"
            + "                }\n"
            + "            }";

    @Setter
    @Getter
    private String name = TYPE;
    @Getter
    @Setter
    private String description = DEFAULT_DESCRIPTION;

    private Client client;

    private static Gson gson = new Gson();

    public GetCreateMonitorParametersTool(Client client) {
        this.client = client;
    }

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public String getVersion() {
        return null;
    }

    @Override
    public boolean validate(Map<String, String> parameters) {
        return true;
    }

    @Override
    public <T> void run(Map<String, String> parameters, ActionListener<T> listener) {
        try {
            Map<String, Object> inputParameters = getInputParameters(parameters);
            log.info("Got input parameters like {}", inputParameters);

            String index;
            if (inputParameters != null && inputParameters.containsKey("index")) {
                index = inputParameters.get("index").toString();
            } else {
                index = "";
            }
            if (index.isBlank()) {
                throw new IllegalArgumentException(
                    "Return this final answer to human directly and do not use other tools: 'Please provide specific index name'. Please try to directly send this message to human to ask for index name"
                );
            }

            listener.onResponse((T) AccessController.doPrivileged((PrivilegedExceptionAction<String>) () -> gson.toJson(inputParameters)));
        } catch (Exception e) {
            log.error("Failed to get create monitor parameters", e);
            listener.onFailure(e);
        }
    }

    public static class Factory implements Tool.Factory<GetCreateMonitorParametersTool> {

        private Client client;

        private static Factory INSTANCE;

        public static Factory getInstance() {
            if (INSTANCE != null) {
                return INSTANCE;
            }
            synchronized (GetCreateMonitorParametersTool.class) {
                if (INSTANCE != null) {
                    return INSTANCE;
                }
                INSTANCE = new Factory();
                return INSTANCE;
            }
        }

        public void init(Client client) {
            this.client = client;
        }

        @Override
        public GetCreateMonitorParametersTool create(Map<String, Object> params) {
            return new GetCreateMonitorParametersTool(client);
        }

        @Override
        public String getDefaultDescription() {
            return DEFAULT_DESCRIPTION;
        }

        @Override
        public String getDefaultType() {
            return TYPE;
        }

        @Override
        public String getDefaultVersion() {
            return null;
        }
    }

    private Map<String, Object> getInputParameters(Map<String, String> parameters) {
        if (parameters.containsKey("input")) {
            try {
                return gson.fromJson(parameters.get("input"), Map.class);
            } catch (Exception e) {
                log.warn("failed to parse input from parameters and will return original input");
                return Collections.emptyMap();
            }
        }
        return Collections.emptyMap();
    }
}
