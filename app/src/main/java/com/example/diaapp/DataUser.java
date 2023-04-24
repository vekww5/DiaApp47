package com.example.diaapp;

import java.util.Map;

public class DataUser {
    private String inject_long, inject_short, glucose, xe, string_date, string_time;
    private long timestamp;

    DataUser (){ }

    public DataUser(String inject_long, String inject_short, String glucose, String xe, String string_date, String string_time, long timestamp) {
        this.inject_long = inject_long;
        this.inject_short = inject_short;
        this.glucose = glucose;
        this.xe = xe;
        this.string_date = string_date;
        this.string_time = string_time;
        this.timestamp = timestamp;
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

    public String getInject_short() {
        return inject_short;
    }

    public void setInject_short(String inject_short) {
        this.inject_short = inject_short;
    }

    public String getInject_long() {
        return inject_long;
    }

    public void setInject_long(String inject_long) {
        this.inject_long = inject_long;
    }

    public String getXe() {
        return xe;
    }

    public void setXe(String xe) {
        this.xe = xe;
    }

    public String getGlucose() {
        return glucose;
    }

    public void setGlucose(String glucose) {
        this.glucose = glucose;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
