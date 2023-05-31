package com.ShowYourThings.coed_test;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class User {
    @PrimaryKey(autoGenerate = true)
    private int id = 0;
    private String server;
    private float ttsspeed;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getServer() {
        return server;
    }

    public void setServer(String server) {
        this.server = server;
    }

    public float getTtsspeed() {
        return ttsspeed;
    }

    public void setTtsspeed(float ttsspeed) {
        this.ttsspeed = ttsspeed;
    }
}
