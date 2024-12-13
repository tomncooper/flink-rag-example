package com.github.streamshub.flink.examples.rag.datagen;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Iterator;
import java.util.Properties;

public class KafkaGenerator {

    private static final String DOCUMENT_TOPIC_NAME = "documents";

    public static void main(String[] args) throws Exception {

        if (args.length != 2) {
            System.err.println("Usage: <boostrap> <per second message rate>");
            System.exit(1);
        }

        String bootstrapServers = args[0];
        double perSecondRate = Double.parseDouble(args[1]);
        if (perSecondRate < 0.00001) {
            System.err.println("Per second message rate must be greater than zero");
            System.exit(1);
        }
        long intervalMs = Math.round(1000/perSecondRate);

        Iterator<Document> documentLoader = new ShakespeareLoader();

        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.LINGER_MS_CONFIG, 1);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, DocumentSerializer.class.getName());

        System.out.println("Starting Kafka Document Loader...");

        try (KafkaProducer<String, Document> producer = new KafkaProducer<>(props)) {
            while (documentLoader.hasNext()) {
                Document document = documentLoader.next();
                producer.send(new ProducerRecord<>(DOCUMENT_TOPIC_NAME, document));

                Thread.sleep(intervalMs);
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }


    }

}
