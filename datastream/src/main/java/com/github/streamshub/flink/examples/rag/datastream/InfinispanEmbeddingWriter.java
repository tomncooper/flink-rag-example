package com.github.streamshub.flink.examples.rag.datastream;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.infinispan.InfinispanEmbeddingStore;
import org.apache.flink.api.connector.sink2.SinkWriter;
import org.apache.flink.api.java.tuple.Tuple2;
import org.infinispan.client.hotrod.configuration.ConfigurationBuilder;

import java.io.IOException;
import java.util.List;
import java.util.Properties;

import static com.github.streamshub.flink.examples.rag.datastream.RagDataStream.EMBEDDING_STORE_INFINISPAN_CONFIG_CACHE_NAME;
import static com.github.streamshub.flink.examples.rag.datastream.RagDataStream.EMBEDDING_STORE_INFINISPAN_CONFIG_DIMENSION;
import static com.github.streamshub.flink.examples.rag.datastream.RagDataStream.EMBEDDING_STORE_INFINISPAN_CONFIG_HOSTNAME;
import static com.github.streamshub.flink.examples.rag.datastream.RagDataStream.EMBEDDING_STORE_INFINISPAN_CONFIG_PASSWORD;
import static com.github.streamshub.flink.examples.rag.datastream.RagDataStream.EMBEDDING_STORE_INFINISPAN_CONFIG_PORT;
import static com.github.streamshub.flink.examples.rag.datastream.RagDataStream.EMBEDDING_STORE_INFINISPAN_CONFIG_USERNAME;

public class InfinispanEmbeddingWriter implements SinkWriter<Tuple2<String, List<Float>>> {

    EmbeddingStore<TextSegment> embeddingStore;

    public InfinispanEmbeddingWriter(Properties properties) {
        this.embeddingStore = getInfinispanEmbeddingStore(properties);
    }

    protected static EmbeddingStore<TextSegment> getInfinispanEmbeddingStore(Properties properties) {

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

    @Override
    public void write(Tuple2<String, List<Float>> input, Context context) throws IOException, InterruptedException {
        TextSegment segment = TextSegment.from(input.f0);
        Embedding embedding = Embedding.from(input.f1);
        embeddingStore.add(embedding, segment);
    }

    @Override
    public void flush(boolean b) throws IOException, InterruptedException {

    }

    @Override
    public void close() throws Exception {

    }
}
