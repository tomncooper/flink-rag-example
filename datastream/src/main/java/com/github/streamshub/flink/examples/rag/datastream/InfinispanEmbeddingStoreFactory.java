package com.github.streamshub.flink.examples.rag.datastream;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.infinispan.InfinispanEmbeddingStore;
import org.infinispan.client.hotrod.configuration.ConfigurationBuilder;

import java.io.Serializable;
import java.util.Properties;

import static com.github.streamshub.flink.examples.rag.datastream.RagDataStreamConstants.EMBEDDING_STORE_INFINISPAN_CONFIG_CACHE_NAME;
import static com.github.streamshub.flink.examples.rag.datastream.RagDataStreamConstants.EMBEDDING_STORE_INFINISPAN_CONFIG_DIMENSION;
import static com.github.streamshub.flink.examples.rag.datastream.RagDataStreamConstants.EMBEDDING_STORE_INFINISPAN_CONFIG_HOSTNAME;
import static com.github.streamshub.flink.examples.rag.datastream.RagDataStreamConstants.EMBEDDING_STORE_INFINISPAN_CONFIG_PASSWORD;
import static com.github.streamshub.flink.examples.rag.datastream.RagDataStreamConstants.EMBEDDING_STORE_INFINISPAN_CONFIG_PORT;
import static com.github.streamshub.flink.examples.rag.datastream.RagDataStreamConstants.EMBEDDING_STORE_INFINISPAN_CONFIG_USERNAME;

public class InfinispanEmbeddingStoreFactory implements EmbeddingStoreFactory<TextSegment>, Serializable {

    Properties properties;

    public InfinispanEmbeddingStoreFactory(Properties properties) {
        this.properties = properties;
    }

    @Override
    public EmbeddingStore<TextSegment> create() {

        // TODO: This will fail silently as the default values are set. It will only fail on first connection attempt to infinispan.
        String host = properties.getProperty(EMBEDDING_STORE_INFINISPAN_CONFIG_HOSTNAME, "localhost");
        int port = Integer.parseInt(properties.getProperty(EMBEDDING_STORE_INFINISPAN_CONFIG_PORT, "11222"));
        String username = properties.getProperty(EMBEDDING_STORE_INFINISPAN_CONFIG_USERNAME, "admin");
        String password = properties.getProperty(EMBEDDING_STORE_INFINISPAN_CONFIG_PASSWORD, "secret");
        String cacheName = properties.getProperty(EMBEDDING_STORE_INFINISPAN_CONFIG_CACHE_NAME, "default");
        int embeddingDimension = Integer.parseInt(properties.getProperty(EMBEDDING_STORE_INFINISPAN_CONFIG_DIMENSION, "384"));

        ConfigurationBuilder builder = new ConfigurationBuilder();
        builder.addServer()
                .host(host)
                .port(port)
                .security()
                .authentication()
                .username(username)
                .password(password);

        // TODO: Figure out how to get passwords from secrets
        return InfinispanEmbeddingStore.builder()
                .cacheName(cacheName)
                .dimension(embeddingDimension)
                .infinispanConfigBuilder(builder)
                .build();
    }
}
