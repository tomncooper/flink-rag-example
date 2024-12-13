package com.github.streamshub.flink.examples.rag.datagen;

public class Document {
    private String title;
    private String text;

    public Document(String title, String text) {
        this.title = title;
        this.text = text;
    }

    public String getTitle() {
        return title;
    }

    public String getText() {
        return text;
    }

}
