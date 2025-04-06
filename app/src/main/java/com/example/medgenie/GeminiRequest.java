package com.example.medgenie;

import java.util.Collections;
import java.util.List;

public class GeminiRequest {
    public List<Content> contents;

    public GeminiRequest(String text) {
        this.contents = Collections.singletonList(new Content(text));
    }

    public static class Content {
        public List<Part> parts;
        public String role = "user";

        public Content(String text) {
            this.parts = Collections.singletonList(new Part(text));
        }
    }

    public static class Part {
        public String text;

        public Part(String text) {
            this.text = text;
        }
    }
}
