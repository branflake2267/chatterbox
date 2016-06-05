package com.gonevertical.chatterbox.room;

public class Room {

    private String name;
    private String uid;

    /**
     * This is the default group for the user, created for the user.
     */
    private boolean defaultRoom;

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

    public void setUid(String uid) {
        this.uid = uid;
    }

}