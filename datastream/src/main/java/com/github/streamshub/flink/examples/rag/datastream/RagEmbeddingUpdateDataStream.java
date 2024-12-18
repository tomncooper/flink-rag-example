package com.github.streamshub.flink.examples.rag.datastream;

import dev.langchain4j.spi.model.embedding.EmbeddingModelFactory;
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

import static com.github.streamshub.flink.examples.rag.datastream.RagDataStreamConstants.EMBEDDING_STORE_INFINISPAN_CONFIG_CACHE_NAME;
import static com.github.streamshub.flink.examples.rag.datastream.RagDataStreamConstants.EMBEDDING_STORE_INFINISPAN_CONFIG_DIMENSION;
import static com.github.streamshub.flink.examples.rag.datastream.RagDataStreamConstants.EMBEDDING_STORE_INFINISPAN_CONFIG_HOSTNAME;
import static com.github.streamshub.flink.examples.rag.datastream.RagDataStreamConstants.EMBEDDING_STORE_INFINISPAN_CONFIG_PASSWORD;
import static com.github.streamshub.flink.examples.rag.datastream.RagDataStreamConstants.EMBEDDING_STORE_INFINISPAN_CONFIG_PORT;
import static com.github.streamshub.flink.examples.rag.datastream.RagDataStreamConstants.EMBEDDING_STORE_INFINISPAN_CONFIG_USERNAME;
import static com.github.streamshub.flink.examples.rag.datastream.RagDataStreamConstants.getLocalInifinispanProperties;


public class RagEmbeddingUpdateDataStream {

    public static void main(String[] args) throws Exception {

        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        KafkaSource<String> source = KafkaSource.<String>builder()
                .setBootstrapServers("localhost:9092")
                .setTopics("documents")
                .setGroupId("embedding-update-group")
                .setStartingOffsets(OffsetsInitializer.latest())
                .setValueOnlyDeserializer(new SimpleStringSchema())
                .build();

        FlatMapFunction<String, String> chunkingFunction = new ChunkingFlatMapFunction(300);
        EmbeddingModelFactory embeddingModelFactory = new InProcessEmbeddingModelFactory(EmbeddingModelType.ONYX_QUANTIZED, new Properties());
        MapFunction<String, Tuple2<String, List<Float>>> embeddingFunction = new EmbeddingMapFunction(embeddingModelFactory);
        Sink<Tuple2<String, List<Float>>> embeddingSink = getInfinispanEmbeddingSink();

        env.fromSource(source, WatermarkStrategy.noWatermarks(), "Kafka Document Source")
                .flatMap(chunkingFunction)
                .map(embeddingFunction)
                .sinkTo(embeddingSink);

        env.execute("RAG Embedding Update Data Stream");

    }

    protected static Sink<Tuple2<String, List<Float>>> getInfinispanEmbeddingSink() {
        return new InfinispanEmbeddingStoreSinkFunction(getLocalInifinispanProperties());
    }

}
