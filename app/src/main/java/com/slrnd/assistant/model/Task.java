package com.slrnd.assistant.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class Task {

    @PrimaryKey(autoGenerate = true)
    private Integer uuid;

    @ColumnInfo(name = "title")
    public String title;
    @ColumnInfo(name = "note")
    public String note;
    @ColumnInfo(name = "priority")
    public int priority;
    @ColumnInfo(name = "is_done")
    public int is_done;
    @ColumnInfo(name = "task_date")
    public long task_date;

    public Task(String title, String note, int priority, long task_date) {
        this.title = title;
        this.note = note;
        this.priority = priority;
        this.is_done = 0;
        this.task_date = task_date;
    }

    public Integer getUuid() {
        return uuid;
    }

    public void setUuid(Integer uuid) {
        this.uuid = uuid;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

}