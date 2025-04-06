package com.example.medgenie;

public class Post {
    private String imageUrl;
    private String caption;

    public Post() {} // Empty constructor for Firebase

    public Post(String imageUrl, String caption) {
        this.imageUrl = imageUrl;
        this.caption = caption;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getCaption() {
        return caption;
    }
}
