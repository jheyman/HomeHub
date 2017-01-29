package com.gbbtbb.homehub.musicplayer;

public class SongItem {

        private String id;
        private String trackNum;
        private String title;
        private String duration;

        public SongItem(String id, String trackNum, String title, String duration) {
            this.id = id;
            this.trackNum = trackNum;
            this.title = title;
            this.duration = duration;
        }

        public String getId() { return id; }
        public String getTrackNum() { return trackNum; }
        public String getTitle() { return title  ; }
        public String getDuration() { return duration; }

        @Override
        public String toString() {
            return title + "\n";
        }
}
