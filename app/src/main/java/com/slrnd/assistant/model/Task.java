package com.slrnd.assistant.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.text.SimpleDateFormat;

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
        // yyyyMMdd eg 20990501
        return date;
    }

    public void setDate(int date) {
        // yyyyMMdd eg 20990501
        this.date = date;
    }

    public int getYear() {

        return Integer.parseInt(String.valueOf(this.date).substring(0, 4));
    }

    // IMPORTANT NOTE!!!
    // CALENDAR USES 0 FOR JANUARY 11 DECEMBER
    // EVERY TIME BUILDING CALENDAR FROM TASK OBJECTS SHOULD FIX MONTH LIKE 0 FOR JANUARY 11 DECEMBER INSTEAD OF 1 FOR JANUARY 12 DECEMBER
    // SO JUST SUBTRACT 1 FROM MONTH E.G.:
    // Calendar calendar = Calendar.getInstance();
    // calendar.set(year, month - 1, day, hour, minute); // CALENDAR MONTH FIX -1
    public int getMonth() {
        // STORING MONTH AS 1 FOR JANUARY 12 DECEMBER; WITH LEADING ZERO FOR SINGLE DIGIT MONTH VALUES E.G 01 02 03
        return Integer.parseInt(String.valueOf(this.date).substring(4, 6));
    }

    public int getDayOfMonth() {
        // STORING WITH LEADING ZERO FOR SINGLE DIGIT DAY VALUES E.G 01 02 03
        return Integer.parseInt(String.valueOf(this.date).substring(6));
    }

    public String getStringTime() {
        // HH:mm; formatter requires datetime in millis or seconds*1000L
        return new SimpleDateFormat("HH:mm").format(this.getDatetimeInMillis());
    }
}