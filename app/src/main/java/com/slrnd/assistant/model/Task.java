package com.slrnd.assistant.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class Task {

    @PrimaryKey(autoGenerate = true)
    private Integer id;

    @ColumnInfo(name = "title")
    public String title;
    @ColumnInfo(name = "note")
    public String note;
    @ColumnInfo(name = "is_done")
    public int is_done;
    @ColumnInfo(name = "date")
    public long date;

    public Task(String title, String note, long date) {

        this.title = title;
        this.note = note;
        this.is_done = 0;
        this.date = date;
    }

    public Integer getId() {
        return this.id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}