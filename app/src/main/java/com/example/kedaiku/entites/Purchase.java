package com.example.kedaiku.entites;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "table_purchase")
public class Purchase {
    @PrimaryKey(autoGenerate = true)
    private int _id;
    @NonNull
    private String date;
    private int product_id;
    private int cash_id;
    @NonNull
    private String purchase_detail;

    public Purchase(int _id, @NonNull String date, int product_id, int cash_id, @NonNull String purchase_detail) {
        this._id = _id;
        this.date = date;
        this.product_id = product_id;
        this.cash_id = cash_id;
        this.purchase_detail = purchase_detail;
    }

    public int get_id() {
        return _id;
    }

    public void set_id(int _id) {
        this._id = _id;
    }

    @NonNull
    public String getDate() {
        return date;
    }

    public void setDate(@NonNull String date) {
        this.date = date;
    }

    public int getProduct_id() {
        return product_id;
    }

    public void setProduct_id(int product_id) {
        this.product_id = product_id;
    }

    public int getCash_id() {
        return cash_id;
    }

    public void setCash_id(int cash_id) {
        this.cash_id = cash_id;
    }

    @NonNull
    public String getPurchase_detail() {
        return purchase_detail;
    }

    public void setPurchase_detail(@NonNull String purchase_detail) {
        this.purchase_detail = purchase_detail;
    }
// Getters and setters
}
