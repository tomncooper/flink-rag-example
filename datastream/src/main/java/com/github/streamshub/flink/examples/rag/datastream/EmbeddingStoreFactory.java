package com.github.streamshub.flink.examples.rag.datastream;

import dev.langchain4j.store.embedding.EmbeddingStore;

public interface EmbeddingStoreFactory<T> {

    EmbeddingStore<T> create();
}
