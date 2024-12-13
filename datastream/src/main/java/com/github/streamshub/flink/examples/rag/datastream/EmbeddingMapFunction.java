package com.github.streamshub.flink.examples.rag.datastream;

import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.embedding.onnx.allminilml6v2.AllMiniLmL6V2EmbeddingModel;
import dev.langchain4j.model.embedding.onnx.allminilml6v2q.AllMiniLmL6V2QuantizedEmbeddingModel;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.java.tuple.Tuple2;

import java.util.List;

public class EmbeddingMapFunction implements MapFunction<String, Tuple2<String, List<Float>>> {

    private final String modelType;

    public EmbeddingMapFunction(String modelType) {
        this.modelType = modelType;
    }

    protected EmbeddingModel getEmbeddingModel() {

        if (modelType.equals("onnx")) {
            return new AllMiniLmL6V2EmbeddingModel();
        } else if (modelType.equals("onnx_quantized")) {
            return new AllMiniLmL6V2QuantizedEmbeddingModel();
        }
        throw new IllegalArgumentException("Cannot create embedding model for type: " + modelType);
    }

    @Override
    public Tuple2<String, List<Float>> map(String segment) throws Exception {
        // TODO: This is very non-optimal. We should look at spinning up a local embedding model with LocalAI.
        EmbeddingModel embeddingModel = getEmbeddingModel();
        return new Tuple2<>(segment, embeddingModel.embed(segment).content().vectorAsList());
    }
}
