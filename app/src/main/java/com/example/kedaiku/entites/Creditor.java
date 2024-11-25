package com.example.kedaiku.entites;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "table_creditor")
public class Creditor {
    @PrimaryKey(autoGenerate = true)
    private int _id;
    @NonNull
    private String creditor_name;
    @NonNull
    private String creditor_address;
    @NonNull
    private String creditor_email;
    @NonNull
    private String creditor_phone;

    public int get_id() {
        return _id;
    }

    public void set_id(int _id) {
        this._id = _id;
    }

    @NonNull
    public String getCreditor_name() {
        return creditor_name;
    }

    public void setCreditor_name(@NonNull String creditor_name) {
        this.creditor_name = creditor_name;
    }

    @NonNull
    public String getCreditor_address() {
        return creditor_address;
    }

    public void setCreditor_address(@NonNull String creditor_address) {
        this.creditor_address = creditor_address;
    }

    @NonNull
    public String getCreditor_email() {
        return creditor_email;
    }

    public void setCreditor_email(@NonNull String creditor_email) {
        this.creditor_email = creditor_email;
    }

    @NonNull
    public String getCreditor_phone() {
        return creditor_phone;
    }

    public void setCreditor_phone(@NonNull String creditor_phone) {
        this.creditor_phone = creditor_phone;
    }

    public Creditor(int _id, @NonNull String creditor_name, @NonNull String creditor_address, @NonNull String creditor_email, @NonNull String creditor_phone) {
        this._id = _id;
        this.creditor_name = creditor_name;
        this.creditor_address = creditor_address;
        this.creditor_email = creditor_email;
        this.creditor_phone = creditor_phone;
    }

    // Getters and setters
}
