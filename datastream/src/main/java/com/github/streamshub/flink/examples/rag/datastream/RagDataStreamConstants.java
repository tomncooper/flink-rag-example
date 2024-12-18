package com.github.streamshub.flink.examples.rag.datastream;

import java.util.Properties;

public class RagDataStreamConstants {

    public static final String CONFIG_PREFIX = "streamshub.flink.sql.ai";
    public static final String EMBEDDING_CONFIG_PREFIX = CONFIG_PREFIX + ".embedded";

    public static final String EMBEDDING_STORE_CONFIG_PREFIX = EMBEDDING_CONFIG_PREFIX + ".store";
    public static final String EMBEDDING_STORE_INFINISPAN_PREFIX = EMBEDDING_STORE_CONFIG_PREFIX + ".infinispan";
    public static final String EMBEDDING_STORE_INFINISPAN_CONFIG_HOSTNAME = EMBEDDING_STORE_INFINISPAN_PREFIX + ".hostname";
    public static final String EMBEDDING_STORE_INFINISPAN_CONFIG_PORT = EMBEDDING_STORE_INFINISPAN_PREFIX + ".port";
    public static final String EMBEDDING_STORE_INFINISPAN_CONFIG_USERNAME = EMBEDDING_STORE_INFINISPAN_PREFIX + ".username";
    public static final String EMBEDDING_STORE_INFINISPAN_CONFIG_PASSWORD = EMBEDDING_STORE_INFINISPAN_PREFIX + ".password";
    public static final String EMBEDDING_STORE_INFINISPAN_CONFIG_CACHE_NAME = EMBEDDING_STORE_INFINISPAN_PREFIX + ".cachename";
    public static final String EMBEDDING_STORE_INFINISPAN_CONFIG_DIMENSION = EMBEDDING_STORE_INFINISPAN_PREFIX + ".dimension";

    public static Properties getLocalInifinispanProperties() {
        Properties infinispanProperties = new Properties();
        infinispanProperties.put(EMBEDDING_STORE_INFINISPAN_CONFIG_HOSTNAME, "localhost");
        infinispanProperties.put(EMBEDDING_STORE_INFINISPAN_CONFIG_PORT, "11222");
        infinispanProperties.put(EMBEDDING_STORE_INFINISPAN_CONFIG_USERNAME, "admin");
        infinispanProperties.put(EMBEDDING_STORE_INFINISPAN_CONFIG_PASSWORD, "secret");
        infinispanProperties.put(EMBEDDING_STORE_INFINISPAN_CONFIG_CACHE_NAME, "default");
        infinispanProperties.put(EMBEDDING_STORE_INFINISPAN_CONFIG_DIMENSION, "384");
        return infinispanProperties;
    }

}
