package com.gonevertical.chatterbox.group;

public class Group {

    private String name;
    private String uid;

    public Group() {
    }

    public Group(String name) {
        this.name = name;
    }

    public Group(String name, String uid) {
        this.name = name;
        this.uid = uid;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public String getUid() {
        return uid;
    }

}