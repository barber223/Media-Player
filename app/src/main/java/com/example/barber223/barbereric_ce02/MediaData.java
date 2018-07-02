//Eric Barber
//MDF3 - 1804
//MediaData.java

package com.example.barber223.barbereric_ce02;

import android.net.Uri;

public class MediaData {
    private Uri musicPath;
    private String title;
    private String author;
    private int musicArtInt;


    public MediaData(Uri musicPath, String title, String author, int musicArt) {
        this.musicPath = musicPath;
        this.title = title;
        this.author = author;
        this.musicArtInt = musicArt;
    }

    public int getMusicArtInt() {
        return musicArtInt;
    }
    public Uri getMusicPath() {
        return musicPath;
    }
    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }
    public String getAuthor() {
        return author;
    }
}
