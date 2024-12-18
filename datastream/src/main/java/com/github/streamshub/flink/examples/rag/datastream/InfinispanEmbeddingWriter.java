package com.github.streamshub.flink.examples.rag.datastream;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingStore;
import org.apache.flink.api.connector.sink2.SinkWriter;
import org.apache.flink.api.java.tuple.Tuple2;

import java.io.IOException;
import java.util.List;
import java.util.Properties;

public class InfinispanEmbeddingWriter implements SinkWriter<Tuple2<String, List<Float>>> {

    EmbeddingStore<TextSegment> embeddingStore;

    public InfinispanEmbeddingWriter(Properties properties) {
        InfinispanEmbeddingStoreFactory factory = new InfinispanEmbeddingStoreFactory(properties);
        this.embeddingStore = factory.create();
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
