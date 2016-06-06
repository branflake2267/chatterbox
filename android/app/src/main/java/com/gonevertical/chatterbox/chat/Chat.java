package com.gonevertical.chatterbox.chat;

public class Chat {
    private String author;
    private String message;
    private String uid;

    public Chat() {
    }

    public Chat(String uid, String author, String message) {
        this.uid = uid;
        this.author = author;
        this.message = message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }
}
