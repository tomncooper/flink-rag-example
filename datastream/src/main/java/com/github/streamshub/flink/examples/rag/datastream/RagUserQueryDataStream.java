package com.github.streamshub.flink.examples.rag.datastream;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.spi.model.embedding.EmbeddingModelFactory;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.connector.base.DeliveryGuarantee;
import org.apache.flink.connector.kafka.sink.KafkaRecordSerializationSchema;
import org.apache.flink.connector.kafka.sink.KafkaSink;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

import java.util.Properties;

import static com.github.streamshub.flink.examples.rag.datastream.RagDataStreamConstants.getLocalInifinispanProperties;

public class RagUserQueryDataStream {

    public static void main(String[] args) throws Exception {

        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        KafkaSource<String> userQueryKafkaSource = KafkaSource.<String>builder()
                .setBootstrapServers("localhost:9092")
                .setTopics("user-queries")
                .setGroupId("user-queries-group")
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

        LocalAIChatLanguageModelFactory localAIChatLanguageModelFactory = new LocalAIChatLanguageModelFactory();
        LLMRequestMapFunction llmRequestMapFunction = new LLMRequestMapFunction(localAIChatLanguageModelFactory);

        KafkaSink<String> llmResponseKafkaSink = KafkaSink.<String>builder()
                .setBootstrapServers("localhost:9092")
                .setRecordSerializer(KafkaRecordSerializationSchema.builder()
                        .setTopic("llm-responses")
                        .setValueSerializationSchema(new SimpleStringSchema())
                        .build()
                )
                .setDeliveryGuarantee(DeliveryGuarantee.AT_LEAST_ONCE)

                .build();

        env.fromSource(userQueryKafkaSource, WatermarkStrategy.noWatermarks(), "Kafka User Query Source")
                .map(contentRetrievalMapFunction)
                .map(llmRequestMapFunction)
                .sinkTo(llmResponseKafkaSink);

        env.execute("RAG User Query Data Stream");

    }
}
