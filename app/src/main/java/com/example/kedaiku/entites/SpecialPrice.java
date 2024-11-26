package com.example.kedaiku.entites;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "table_special_price")
public class SpecialPrice {
    @PrimaryKey(autoGenerate = true)
    private long _id;
    @NonNull
    private String name;
    private long product_id;
    private int group_id;
    private double precent;
    private int status;

    public SpecialPrice( @NonNull String name, long product_id, int group_id, double precent, int status) {

        this.name = name;
        this.product_id = product_id;
        this.group_id = group_id;
        this.precent = precent;
        this.status = status;
    }



    public long get_id() {
        return _id;
    }

    public void set_id(long _id) {
        this._id = _id;
    }

    @NonNull
    public String getName() {
        return name;
    }

    public void setName(@NonNull String name) {
        this.name = name;
    }

    public long getProduct_id() {
        return product_id;
    }

    public void setProduct_id(long product_id) {
        this.product_id = product_id;
    }

    public int getGroup_id() {
        return group_id;
    }

    public void setGroup_id(int group_id) {
        this.group_id = group_id;
    }

    public double getPrecent() {
        return precent;
    }

    public void setPrecent(double precent) {
        this.precent = precent;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    // Getters and setters
}
