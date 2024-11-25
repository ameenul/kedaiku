package com.example.kedaiku.entites;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "table_wholesale")
public class Wholesale {
    @PrimaryKey(autoGenerate = true)
    private int _id;
    private int product_id;
    @NonNull
    private String name;
    private double price;
    private int qty;
    private int status;

    public Wholesale(int _id, int product_id, @NonNull String name, double price, int qty, int status) {
        this._id = _id;
        this.product_id = product_id;
        this.name = name;
        this.price = price;
        this.qty = qty;
        this.status = status;
    }

    public int get_id() {
        return _id;
    }

    public void set_id(int _id) {
        this._id = _id;
    }

    public int getProduct_id() {
        return product_id;
    }

    public void setProduct_id(int product_id) {
        this.product_id = product_id;
    }

    @NonNull
    public String getName() {
        return name;
    }

    public void setName(@NonNull String name) {
        this.name = name;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public int getQty() {
        return qty;
    }

    public void setQty(int qty) {
        this.qty = qty;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    // Getters and setters
}

