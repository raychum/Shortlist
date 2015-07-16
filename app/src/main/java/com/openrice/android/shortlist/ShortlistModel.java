package com.openrice.android.shortlist;

/**
 * Created by raychum on 16/7/15.
 */
public class ShortlistModel {
    public int id;
    public String img;
    public boolean isRMS;

    public ShortlistModel() {
    }

    public ShortlistModel(int id, String img, boolean isRMS) {
        this.id = id;
        this.img = img;
        this.isRMS = isRMS;
    }
}
