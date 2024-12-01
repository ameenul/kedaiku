package com.example.kedaiku.entites;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "table_cash")
public class Cash {
    @PrimaryKey(autoGenerate = true)
    private long id;

    @NonNull
    private String cash_name = "";

    private double cash_value;

    // Constructor, Getter dan Setter

    public Cash( @NonNull String cash_name, double cash_value) {

        this.setCash_name(cash_name);
        this.setCash_value(cash_value);
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @NonNull
    public String getCash_name() {
        return cash_name;
    }

    public void setCash_name(@NonNull String cash_name) {
        this.cash_name = cash_name;
    }

    public double getCash_value() {
        return cash_value;
    }

    public void setCash_value(double cash_value) {
        this.cash_value = cash_value;
    }

    @NonNull
    @Override
    public String toString() {
        return cash_name;
    }
}
