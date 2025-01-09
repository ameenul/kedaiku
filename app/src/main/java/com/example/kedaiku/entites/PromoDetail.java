package com.example.kedaiku.entites;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "table_promo_detail")
public class PromoDetail {
    @PrimaryKey(autoGenerate = true)
    private int _id;

    @NonNull
    private String detail;

    // Constructor
    public PromoDetail(@NonNull String detail) {
        this.detail = detail;
    }

    // Getters and Setters
    public int getId() {
        return _id;
    }

    public void setId(int _id) {
        this._id = _id;
    }

    @NonNull
    public String getDetail() {
        return detail;
    }

    public void setDetail(@NonNull String detail) {
        this.detail = detail;
    }



}
