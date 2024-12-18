package com.github.streamshub.flink.examples.rag.datastream;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.spi.model.embedding.EmbeddingModelFactory;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

import java.util.Properties;

import static com.github.streamshub.flink.examples.rag.datastream.RagDataStreamConstants.getLocalInifinispanProperties;

public class RagUserQueryDataStream {

    public static void main(String[] args) throws Exception {

        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        KafkaSource<String> source = KafkaSource.<String>builder()
                .setBootstrapServers("localhost:9092")
                .setTopics("user-query")
                .setGroupId("user-query-group")
                .setStartingOffsets(OffsetsInitializer.latest())
                .setValueOnlyDeserializer(new SimpleStringSchema())
                .build();


        EmbeddingStoreFactory<TextSegment> embeddingStoreFactory = new InfinispanEmbeddingStoreFactory(getLocalInifinispanProperties());
        EmbeddingModelFactory embeddingModelFactory = new InProcessEmbeddingModelFactory(EmbeddingModelType.ONYX_QUANTIZED, new Properties());

        ContentRetrievalMapFunction contentRetrievalMapFunction = new ContentRetrievalMapFunction(
                embeddingStoreFactory,
                embeddingModelFactory,
                2,
                0.5
        );

        env.fromSource(source, WatermarkStrategy.noWatermarks(), "Kafka User Query Source").map(contentRetrievalMapFunction);

        env.execute("RAG User Query Data Stream");

    }
}
