package com.example.diaapp;

public class InfoUser {

    private String first_name, last_name,  height, weight, type_dia, pol, type_short_insulin, type_long_insulin;
    private long birthday;

    InfoUser(){};

    public InfoUser(String first_name, String last_name, String height, String weight, String type_dia, String pol, long birthday,
                     String type_short_insulin, String type_long_insulin) {
        this.first_name = first_name;
        this.last_name = last_name;
        this.height = height;
        this.weight = weight;
        this.type_dia = type_dia;
        this.pol = pol;
        this.birthday = birthday;
        this.type_short_insulin = type_short_insulin;
        this.type_long_insulin = type_long_insulin;
    }

    public String getHeight() {
        return height;
    }

    public void setHeight(String height) {
        this.height = height;
    }

    public String getWeight() {
        return weight;
    }

    public void setWeight(String weight) {
        this.weight = weight;
    }

    public String getType_dia() {
        return type_dia;
    }

    public void setType_dia(String type_dia) {
        this.type_dia = type_dia;
    }

    public String getPol() {
        return pol;
    }

    public void setPol(String pol) {
        this.pol = pol;
    }

    public long getBirthday() {
        return birthday;
    }

    public void setBirthday(long birthday) {
        this.birthday = birthday;
    }

    public String getType_short_insulin() {
        return type_short_insulin;
    }

    public void setType_short_insulin(String type_short_insulin) {
        this.type_short_insulin = type_short_insulin;
    }

    public String getType_long_insulin() {
        return type_long_insulin;
    }

    public void setType_long_insulin(String type_long_insulin) {
        this.type_long_insulin = type_long_insulin;
    }

    public String getFirst_name() {
        return first_name;
    }

    public void setFirst_name(String first_name) {
        this.first_name = first_name;
    }

    public String getLast_name() {
        return last_name;
    }

    public void setLast_name(String last_name) {
        this.last_name = last_name;
    }
}
