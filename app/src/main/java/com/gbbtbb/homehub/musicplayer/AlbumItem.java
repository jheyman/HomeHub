package com.gbbtbb.homehub.musicplayer;

public class AlbumItem {

        private String folder_name;

        public AlbumItem(String name) {
            this.folder_name = name;
        }

        public String getItemName() { return folder_name; }
        public void setItemName(String folder_name) { this.folder_name = folder_name; }

        @Override
        public String toString() {
            return folder_name + "\n";
        }

}
