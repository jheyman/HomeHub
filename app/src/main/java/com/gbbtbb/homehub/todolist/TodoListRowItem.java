package com.gbbtbb.homehub.todolist;

public class TodoListRowItem {

    private String item_name;
    private int priority;
    private String creationDate;

    public TodoListRowItem(String name, int priority, String creationDate) {
        this.item_name = name;
        this.priority = priority;
        this.creationDate = creationDate;
    }

    public String getItemName() { return item_name; }
    public void setItemName(String name) { this.item_name = item_name; }

    public int getPriority() {
        return priority;
    }
    public void setPriority(int priority) {
        this.priority = priority;
    }

    public String getCreationDate() { return creationDate; }
    public void setCreationDate(String date) { this.creationDate = date; }

    @Override
    public String toString() {
        return item_name + "\n";
    }
}

