package com.example.kedaiku.entites;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "table_inventory",
        foreignKeys = @ForeignKey(
                entity = Product.class,
                parentColumns = "id",
                childColumns = "stock_product_id",
                onDelete = ForeignKey.CASCADE
        ),
        indices = @Index(value = "stock_product_id")
)
public class Inventory {

    @PrimaryKey(autoGenerate = true)
    private long id; // Ubah tipe data menjadi long

    @NonNull
    private long stock_date;

    private long stock_product_id; // Ubah tipe data menjadi long

    @NonNull
    private String stock_note;

    private double stock_in;
    private double stock_out;
    private double stock_balance;

    // Constructor
    public Inventory(@NonNull long stock_date, long stock_product_id, @NonNull String stock_note, double stock_in, double stock_out, double stock_balance) {
        this.stock_date = stock_date;
        this.stock_product_id = stock_product_id;
        this.stock_note = stock_note;
        this.stock_in = stock_in;
        this.stock_out = stock_out;
        this.stock_balance = stock_balance;
    }

    // Getters and Setters
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @NonNull
    public long getStock_date() {
        return stock_date;
    }

    public void setStock_date(@NonNull long stock_date) {
        this.stock_date = stock_date;
    }

    public long getStock_product_id() {
        return stock_product_id;
    }

    public void setStock_product_id(long stock_product_id) {
        this.stock_product_id = stock_product_id;
    }

    @NonNull
    public String getStock_note() {
        return stock_note;
    }

    public void setStock_note(@NonNull String stock_note) {
        this.stock_note = stock_note;
    }

    public double getStock_in() {
        return stock_in;
    }

    public void setStock_in(double stock_in) {
        this.stock_in = stock_in;
    }

    public double getStock_out() {
        return stock_out;
    }

    public void setStock_out(double stock_out) {
        this.stock_out = stock_out;
    }

    public double getStock_balance() {
        return stock_balance;
    }

    public void setStock_balance(double stock_balance) {
        this.stock_balance = stock_balance;
    }
}
