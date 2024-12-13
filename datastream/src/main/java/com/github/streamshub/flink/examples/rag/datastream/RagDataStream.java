package com.github.streamshub.flink.examples.rag.datastream;

import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.api.connector.sink2.Sink;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

import java.util.List;
import java.util.Properties;

public class RagDataStream {

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

    public static void main(String[] args) throws Exception {

        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        KafkaSource<String> source = KafkaSource.<String>builder()
                .setBootstrapServers("localhost:9092")
                .setTopics("documents")
                .setGroupId("datastream-group")
                .setStartingOffsets(OffsetsInitializer.latest())
                .setValueOnlyDeserializer(new SimpleStringSchema())
                .build();

        FlatMapFunction<String, String> chunkingFunction = new ChunkingFlatMapFunction(300);
        MapFunction<String, Tuple2<String, List<Float>>> embeddingFunction = new EmbeddingMapFunction("onnx_quantized");
        Sink<Tuple2<String, List<Float>>> embeddingSink = getInfinispanEmbeddingSink();

        env.fromSource(source, WatermarkStrategy.noWatermarks(), "Kafka Document Source")
                .flatMap(chunkingFunction)
                .map(embeddingFunction)
                .sinkTo(embeddingSink);

        env.execute("RAG Data Stream");

    }

    private static Sink<Tuple2<String, List<Float>>> getInfinispanEmbeddingSink() {
        Properties infinispanProperties = new Properties();
        infinispanProperties.put(EMBEDDING_STORE_INFINISPAN_CONFIG_HOSTNAME, "localhost");
        infinispanProperties.put(EMBEDDING_STORE_INFINISPAN_CONFIG_PORT, "11222");
        infinispanProperties.put(EMBEDDING_STORE_INFINISPAN_CONFIG_USERNAME, "admin");
        infinispanProperties.put(EMBEDDING_STORE_INFINISPAN_CONFIG_PASSWORD, "secret");
        infinispanProperties.put(EMBEDDING_STORE_INFINISPAN_CONFIG_CACHE_NAME, "default");
        infinispanProperties.put(EMBEDDING_STORE_INFINISPAN_CONFIG_DIMENSION, "384");
        return new InfinispanEmbeddingStoreSinkFunction(infinispanProperties);
    }
}
