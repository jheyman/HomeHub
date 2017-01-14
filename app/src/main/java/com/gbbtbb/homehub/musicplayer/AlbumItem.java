package com.gbbtbb.homehub.musicplayer;

import android.graphics.Bitmap;

public class AlbumItem {

        private String album_title;
        private String album_artist;
        private Bitmap album_cover;

        public AlbumItem(String name, String artist, Bitmap cover) {
            this.album_title = name;
            this.album_artist = artist;
            this.album_cover = cover;
        }

        public String getAlbumTitle() { return album_title; }
        public String getAlbumArtist() { return album_artist; }
        public Bitmap getAlbumCover() { return album_cover; }

        @Override
        public String toString() {
            return album_title + "\n";
        }
}
