package com.gonevertical.chatterbox.group;

public class Group {


    private String name;

    /**
     * User key
     */
    private String uid;

    /**
     * This is the default group for the user, created for the user.
     */
    private boolean defaultGroup;

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

    public void setUid(String uid) {
        this.uid = uid;
    }

    public boolean isDefaultGroup() {
        return defaultGroup;
    }

    public void setDefaultGroup(boolean defaultGroup) {
        this.defaultGroup = defaultGroup;
    }
}