package com.slrnd.assistant.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class Task {

    @PrimaryKey(autoGenerate = true)
    private Integer id;

    @ColumnInfo(name = "title")
    private String title;
    @ColumnInfo(name = "note")
    private String note;
    @ColumnInfo(name = "is_done")
    private int is_done;
    @ColumnInfo(name = "date")
    private long date;

    @ColumnInfo(name = "string_date")
    private String string_date;
    @ColumnInfo(name = "string_time")
    private String string_time;

    public Task(String title, String note, long date) {

        this.title = title;
        this.note = note;
        this.is_done = 0;
        this.date = date;
    }

    public Integer getId() {
        return id;
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

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public int getIs_done() {
        return is_done;
    }

    public void setIs_done(int is_done) {
        this.is_done = is_done;
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public String getString_date() {
        return string_date;
    }

    public void setString_date(String string_date) {
        this.string_date = string_date;
    }

    public String getString_time() {
        return string_time;
    }

    public void setString_time(String string_time) {
        this.string_time = string_time;
    }
}