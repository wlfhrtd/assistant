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

    @ColumnInfo(name = "datetime")
    private long datetime; // unix time in SECONDS
    @ColumnInfo(name = "date")
    private int date; // yyyyMMdd

    public Task(String title, String note, long datetime, int date) {

        this.title = title;
        this.note = note;
        this.is_done = 0;
        this.datetime = datetime;
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

    protected long getDatetime() {
        return datetime;
    } // required for ORDER BY datetime in DAO

    public long getDatetimeInSeconds() {
        return datetime;
    }

    public void setDatetimeInSeconds(long datetime) {
        this.datetime = datetime;
    }

    public long getDatetimeInMillis() {
        return datetime * 1000L;
    }

    public void setDatetimeInMillis(long datetimeInMillis) {
        this.datetime = datetimeInMillis / 1000L;
    }

    public int getDate() {
        return date;
    }

    public void setDate(int date) {
        this.date = date;
    }
}