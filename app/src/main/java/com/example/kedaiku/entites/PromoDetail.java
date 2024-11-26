package com.example.kedaiku.entites;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "table_promo_detail")
public class PromoDetail {
    @PrimaryKey(autoGenerate = true)
    private long _id;
    private long detail;

    public PromoDetail( long detail) {

        this.detail = detail;
    }

    public long get_id() {
        return _id;
    }

    public void set_id(long _id) {
        this._id = _id;
    }

    public long getDetail() {
        return detail;
    }

    public void setDetail(long detail) {
        this.detail = detail;
    }
// Getters and setters
}

