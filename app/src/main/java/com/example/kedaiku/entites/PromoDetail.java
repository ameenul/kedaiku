package com.example.kedaiku.entites;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "table_promo_detail")
public class PromoDetail {
    @PrimaryKey(autoGenerate = true)
    private int _id;
    private int detail;

    public PromoDetail(int _id, int detail) {
        this._id = _id;
        this.detail = detail;
    }

    public int get_id() {
        return _id;
    }

    public void set_id(int _id) {
        this._id = _id;
    }

    public int getDetail() {
        return detail;
    }

    public void setDetail(int detail) {
        this.detail = detail;
    }
// Getters and setters
}

