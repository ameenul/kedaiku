package com.example.kedaiku.entites;


import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

@Entity(tableName = "table_cash_flow",
        foreignKeys = @ForeignKey(entity = Cash.class,
                parentColumns = "id",
                childColumns = "cash_id",
                onDelete = ForeignKey.CASCADE))
public class CashFlow {
    @PrimaryKey(autoGenerate = true)
    private long id;

    private long cash_id;


    private long cash_date;

    @NonNull
    private String cash_description = "";

    private double cash_value;

    // Constructor, Getter dan Setter


    public CashFlow(long cash_id, long cash_date, @NonNull String cash_description, double cash_value) {

        this.setCash_id(cash_id);
        this.setCash_date(cash_date);
        this.setCash_description(cash_description);
        this.setCash_value(cash_value);

    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getCash_id() {
        return cash_id;
    }

    public void setCash_id(long cash_id) {
        this.cash_id = cash_id;
    }


    public long getCash_date() {
        return cash_date;
    }

    public void setCash_date(long cash_date) {
        this.cash_date = cash_date;
    }

    @NonNull
    public String getCash_description() {
        return cash_description;
    }

    public void setCash_description(@NonNull String cash_description) {
        this.cash_description = cash_description;
    }

    public double getCash_value() {
        return cash_value;
    }

    public void setCash_value(double cash_value) {
        this.cash_value = cash_value;
    }
}
