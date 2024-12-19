package com.github.streamshub.flink.examples.rag.datastream;

import dev.langchain4j.model.chat.ChatLanguageModel;

public interface ChatLanguageModelFactory {
    ChatLanguageModel create();
}
