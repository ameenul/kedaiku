package com.example.kedaiku.entites;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "table_purchase")
public class Purchase {
    @PrimaryKey(autoGenerate = true)
    private long _id;
    @NonNull
    private long date;
    private long product_id;
    private long cash_id;
    @NonNull
    private String purchase_detail;



    public Purchase(@NonNull long date, long product_id, long cash_id, @NonNull String purchase_detail) {

        this.date = date;
        this.product_id = product_id;
        this.cash_id = cash_id;
        this.purchase_detail = purchase_detail;
    }

    public long get_id() {
        return _id;
    }

    public void set_id(long _id) {
        this._id = _id;
    }

    @NonNull
    public long getDate() {
        return date;
    }

    public void setDate(@NonNull long date) {
        this.date = date;
    }

    public long getProduct_id() {
        return product_id;
    }

    public void setProduct_id(long product_id) {
        this.product_id = product_id;
    }

    public long getCash_id() {
        return cash_id;
    }

    public void setCash_id(long cash_id) {
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
