package com.gbbtbb.homehub.agendaviewer;

public class AgendaItem {

    private long datetime;
    private String title;

    public AgendaItem(long datetime, String title) {
        this.datetime = datetime;
        this.title = title;
    }

    public long getDatetime() { return datetime; }
    public void setDatetime(long datetime) { this.datetime = datetime; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
}
