package com.github.streamshub.flink.examples.rag.datastream;

import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.embedding.onnx.allminilml6v2.AllMiniLmL6V2EmbeddingModel;
import dev.langchain4j.model.embedding.onnx.allminilml6v2q.AllMiniLmL6V2QuantizedEmbeddingModel;
import dev.langchain4j.spi.model.embedding.EmbeddingModelFactory;

import java.io.Serializable;
import java.util.Properties;

public class InProcessEmbeddingModelFactory implements EmbeddingModelFactory, Serializable {

    EmbeddingModelType modelType;
    Properties properties;

    public InProcessEmbeddingModelFactory(EmbeddingModelType modelType, Properties properties) {
        this.modelType = modelType;
        this.properties = properties;
    }

    protected EmbeddingModel getEmbeddingModel() {

        if (modelType == EmbeddingModelType.ONYX) {
            return new AllMiniLmL6V2EmbeddingModel();
        } else if (modelType == EmbeddingModelType.ONYX_QUANTIZED) {
            return new AllMiniLmL6V2QuantizedEmbeddingModel();
        }
        throw new IllegalArgumentException("Cannot create embedding model for type: " + modelType);
    }

    @Override
    public EmbeddingModel create() {
        return getEmbeddingModel();
    }
}
