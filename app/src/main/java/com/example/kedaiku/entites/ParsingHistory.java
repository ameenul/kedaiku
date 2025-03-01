package com.example.kedaiku.entites;

public class ParsingHistory {
    private long date;
    private double paid;

    public ParsingHistory(long date, double paid) {
        this.date = date;
        this.paid = paid;
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public double getPaid() {
        return paid;
    }

    public void setPaid(double paid) {
        this.paid = paid;
    }

    @Override
    public String toString() {
        return "ParsingHistory{" +
                "date=" + date +
                ", paid=" + paid +
                '}';
    }
}
