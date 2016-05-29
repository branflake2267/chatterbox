package com.gonevertical.chatterbox.room;

public class Room {

    private String name;
    private String uid;

    public Room() {
    }

    public Room(String name) {
        this.name = name;
    }

    public Room(String name, String uid) {
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