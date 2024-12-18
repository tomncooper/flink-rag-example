package com.github.streamshub.flink.examples.rag.datastream;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.rag.content.Content;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.rag.query.Query;
import dev.langchain4j.spi.model.embedding.EmbeddingModelFactory;
import dev.langchain4j.store.embedding.EmbeddingStore;
import org.apache.flink.api.common.functions.OpenContext;
import org.apache.flink.api.common.functions.RichMapFunction;
import org.apache.flink.api.java.tuple.Tuple2;

import java.util.List;

public class ContentRetrievalMapFunction extends RichMapFunction<String, Tuple2<String, List<String>>> {

    ContentRetriever contentRetriever;
    EmbeddingModelFactory embeddingModelFactory;
    EmbeddingStoreFactory<TextSegment> embeddingStoreFactory;
    int maxResults;
    double minScore;

    public ContentRetrievalMapFunction(
            EmbeddingStoreFactory<TextSegment> embeddingStoreFactory,
            EmbeddingModelFactory embeddingModelFactory,
            int maxResults,
            double minScore
    ) {
        this.embeddingStoreFactory = embeddingStoreFactory;
        this.embeddingModelFactory = embeddingModelFactory;
        this.maxResults = maxResults;
        this.minScore = minScore;
    }

    @Override
    public void open(OpenContext openContext) throws Exception {
        EmbeddingModel embeddingModel = embeddingModelFactory.create();
        EmbeddingStore<TextSegment> embeddingStore = embeddingStoreFactory.create();

        contentRetriever = EmbeddingStoreContentRetriever.builder()
                .embeddingStore(embeddingStore)
                .embeddingModel(embeddingModel)
                .maxResults(maxResults)
                .minScore(minScore)
                .build();
    }

    @Override
    public Tuple2<String, List<String>> map(String query) throws Exception {
        List<String> relevantStrings = contentRetriever
                .retrieve(Query.from(query))
                .stream()
                .map(Content::textSegment)
                .map(TextSegment::text)
                .toList();
        return new Tuple2<>(query, relevantStrings);
    }
}
