package com.github.streamshub.flink.examples.rag.datastream;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentParser;
import dev.langchain4j.data.document.parser.TextDocumentParser;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.segment.TextSegment;
import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.util.Collector;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class ChunkingFlatMapFunction implements FlatMapFunction<String, String> {

    int segmentSize;
    int maxOverlapSize;

    public ChunkingFlatMapFunction(int segmentSize, int maxOverlapSize) {
        this.segmentSize = segmentSize;
        this.maxOverlapSize = maxOverlapSize;
    }

    public ChunkingFlatMapFunction(int segmentSize) {
        this(segmentSize, 0);
    }

    @Override
    public void flatMap(String documentString, Collector<String> collector) {

        DocumentParser documentParser = new TextDocumentParser();
        Document document = documentParser.parse(new ByteArrayInputStream(documentString.getBytes(StandardCharsets.UTF_8)));

        List<TextSegment> segments = DocumentSplitters.recursive(segmentSize, maxOverlapSize).split(document);

        for (TextSegment segment : segments) {
            collector.collect(segment.toString());
        }

    }
}
