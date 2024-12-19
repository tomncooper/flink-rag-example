package com.github.streamshub.flink.examples.rag.datastream;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.localai.LocalAiChatModel;

import java.io.Serializable;

public class LocalAIChatLanguageModelFactory implements ChatLanguageModelFactory, Serializable {

    @Override
    public ChatLanguageModel create() {
        return   LocalAiChatModel.builder()
                .baseUrl("http://localhost:8080")
                .modelName("gpt-4")
                .maxTokens(3)
                .logRequests(true)
                .logResponses(true)
                .build();
    }
}
