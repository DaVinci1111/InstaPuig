package com.example.insta;

public class Comment {
    public String id;
    public String postId;
    public String author;
    public String content;
    public long timeStamp;

    // Constructor vacio requerido por Firestore
    public Comment() {}

    public Comment(String id, String postId, String author, String content, long timeStamp) {
        this.id = id;
        this.postId = postId;
        this.author = author;
        this.content = content;
        this.timeStamp = timeStamp;
    }
}