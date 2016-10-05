package com.gbbtbb.homehub.shoppinglist;

import com.gbbtbb.homehub.R;

public class ShoppingListRowItem {
    private static int imageId = R.drawable.paperpad_one_row;

    private String title;

    public ShoppingListRowItem(String title) {
        this.title = title;
    }
    public int getImageId() {
        return imageId;
    }
    public void setImageId(int imageId) {
        this.imageId = imageId;
    }

    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }
    @Override
    public String toString() {
        return title + "\n";
    }
}

