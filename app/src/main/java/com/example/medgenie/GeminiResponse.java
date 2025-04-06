package com.example.medgenie;

import java.util.List;

public class GeminiResponse {
    public List<Candidate> candidates;

    public static class Candidate {
        public GeminiRequest.Content content;
    }
}
