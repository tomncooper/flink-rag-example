package com.github.streamshub.flink.examples.rag.datastream;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.output.Response;
import dev.langchain4j.rag.content.Content;
import dev.langchain4j.rag.content.injector.ContentInjector;
import dev.langchain4j.rag.content.injector.DefaultContentInjector;
import org.apache.flink.api.common.functions.OpenContext;
import org.apache.flink.api.common.functions.RichMapFunction;
import org.apache.flink.api.java.tuple.Tuple2;

import java.util.List;

public class LLMRequestMapFunction extends RichMapFunction<Tuple2<String, List<String>>, String> {

    ChatLanguageModelFactory chatLanguageModelFactory;
    ChatLanguageModel chatLanguageModel;
    ContentInjector contentInjector;

    public LLMRequestMapFunction(ChatLanguageModelFactory chatLanguageModelFactory) {
        this.chatLanguageModelFactory = chatLanguageModelFactory;
    }

    @Override
    public void open(OpenContext openContext) throws Exception {
        chatLanguageModel = chatLanguageModelFactory.create();
        contentInjector = DefaultContentInjector.builder().build();
    }

    @Override
    public String map(Tuple2<String, List<String>> value) throws Exception {
        List<Content> contentList = value.f1.stream().map(Content::new).toList();
        ChatMessage prompt = contentInjector.inject(contentList, new AiMessage(value.f0));
        Response<AiMessage> response = chatLanguageModel.generate(prompt);
        return response.content().text();
    }

}
