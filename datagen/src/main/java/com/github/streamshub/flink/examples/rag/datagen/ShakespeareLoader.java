package com.github.streamshub.flink.examples.rag.datagen;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class ShakespeareLoader implements Iterator<Document> {

    private static final String SOURCE_DOCUMENT = "shakespeare.txt";

    private final Map<String, String> documents;
    private final Set<String> documentTitles;
    private final List<String> documentOrder;
    private int currentIndex;

    public ShakespeareLoader() {
        this.documents = new HashMap<>();
        this.documentTitles = new HashSet<>();
        System.out.println("Loading source documents from " + SOURCE_DOCUMENT);
        try {
            loadDocumentTitles();
            loadDocuments();
        } catch (IOException e) {
            throw new RuntimeException("Unable to load " + SOURCE_DOCUMENT +  " file", e);
        }
        System.out.println("Found " + documentTitles.size() + " documents in " + SOURCE_DOCUMENT);
        documentOrder = new ArrayList<>();
        // HashSet has no guaranteed order so we might not need to randomize
        documentOrder.addAll(documents.keySet());
        currentIndex = -1;
    }

    private void loadDocuments() throws IOException {

        Map<String, Integer> titleCounts = new HashMap<>();
        boolean firstDocument = true;

        try (InputStream shakespeareStream = getClass().getClassLoader().getResourceAsStream("shakespeare.txt")) {

            BufferedReader shakespeareReader = new BufferedReader(new InputStreamReader(Objects.requireNonNull(shakespeareStream)));

            StringBuilder documentBuilder = new StringBuilder();
            String documentName = "PLACEHOLDER";

            for (String line; (line = shakespeareReader.readLine()) != null; ) {

                String strippedLine = line.trim();

                if (documentTitles.contains(strippedLine)) {
                    // The first time we see the title will be in the main contents so we need to skip over that
                    int currentCount = titleCounts.getOrDefault(strippedLine, 0) + 1;
                    titleCounts.put(strippedLine, currentCount);
                    if (currentCount == 2) {
                        if (firstDocument) {
                            // If this is the first document then there is no previous document to store so we skip that
                            firstDocument = false;
                        } else {
                            // We are at the start of a new document so store the last one
                            documents.put(documentName, documentBuilder.toString());
                        }
                        // Reset for new document
                        documentName = strippedLine;
                        documentBuilder = new StringBuilder();
                    }
                } else {
                    documentBuilder.append(strippedLine);
                }
            }
        }
    }

    private void loadDocumentTitles() throws IOException {
        try (InputStream shakespeareStream = getClass().getClassLoader().getResourceAsStream("shakespeare.txt")) {
            BufferedReader shakespeareReader = new BufferedReader(new InputStreamReader(Objects.requireNonNull(shakespeareStream)));
            boolean inContents = false;
            for (String line; (line = shakespeareReader.readLine()) != null; ) {
                String strippedLine = line.trim();
                if (strippedLine.contains("Contents")) {
                    inContents = true;
                    continue;
                }
                if (inContents) {
                    if (documentTitles.contains(strippedLine)) {
                        // If we have seen this title again we have left the contents
                        return;
                    } else {
                        if (!strippedLine.isEmpty()) {
                            documentTitles.add(strippedLine);
                        }
                    }
                }
            }
        }
    }

    public static void main(String[] args) {
        Iterator<Document> loader = new ShakespeareLoader();
        for (int i = 0; i < 44 ; i++) {
            Document doc = loader.next();
            System.out.println("Document: " + doc.getTitle());
            System.out.println("Length: " + doc.getText().length());
        }
    }

    @Override
    public boolean hasNext() {
        return true;
    }

    @Override
    public Document next() {
        currentIndex = (currentIndex + 1) % documentOrder.size();
        String title = documentOrder.get(currentIndex);
        return new Document(title, documents.get(title));
    }
}
