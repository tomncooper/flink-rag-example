package com.github.streamshub.flink.examples.rag.datastream;

import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.spi.model.embedding.EmbeddingModelFactory;
import org.apache.flink.api.common.functions.OpenContext;
import org.apache.flink.api.common.functions.RichMapFunction;
import org.apache.flink.api.java.tuple.Tuple2;

import java.util.List;

public class EmbeddingMapFunction extends RichMapFunction<String, Tuple2<String, List<Float>>> {

    private final EmbeddingModelFactory embeddingModelFactory;
    private EmbeddingModel embeddingModel;

    public EmbeddingMapFunction(EmbeddingModelFactory embeddingModelFactory) {
        this.embeddingModelFactory = embeddingModelFactory;
    }

    @Override
    public void open(OpenContext openContext) throws Exception {
       embeddingModel = embeddingModelFactory.create();
    }

    @Override
    public Tuple2<String, List<Float>> map(String segment) throws Exception {
        return new Tuple2<>(segment, embeddingModel.embed(segment).content().vectorAsList());
    }
}
