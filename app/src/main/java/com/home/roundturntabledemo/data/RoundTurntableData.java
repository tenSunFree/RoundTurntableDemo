package com.home.roundturntabledemo.data;

import android.graphics.Bitmap;

public class RoundTurntableData {

    public String text;
    public Bitmap bitmap;
    public ItemInfo info;

    public RoundTurntableData(String text, Bitmap bitmap) {
        this.text = text;
        this.bitmap = bitmap;
        this.info = new ItemInfo();
    }

    public static class ItemInfo {
        public Bitmap itemBitmap; // 背景小圖標文字合成的bitmap
    }
}
