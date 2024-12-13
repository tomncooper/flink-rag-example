package com.github.streamshub.flink.examples.rag.datastream;

import org.apache.flink.api.connector.sink2.Sink;
import org.apache.flink.api.connector.sink2.SinkWriter;
import org.apache.flink.api.connector.sink2.WriterInitContext;
import org.apache.flink.api.java.tuple.Tuple2;

import java.io.IOException;
import java.util.List;
import java.util.Properties;


public class InfinispanEmbeddingStoreSinkFunction implements Sink<Tuple2<String, List<Float>>> {

    Properties properties;

    public InfinispanEmbeddingStoreSinkFunction(Properties properties) {
        this.properties = properties;
    }

    public SinkWriter<Tuple2<String,List<Float>>> createWriter(InitContext initContext) throws IOException {
        throw new RuntimeException("Not Implemented");
    }

    @Override
    public SinkWriter<Tuple2<String, List<Float>>> createWriter(WriterInitContext context) throws IOException {
        return new InfinispanEmbeddingWriter(properties);
    }
}
