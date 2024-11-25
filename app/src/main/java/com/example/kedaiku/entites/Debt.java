package com.example.kedaiku.entites;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "table_debt")
public class Debt {
    @PrimaryKey(autoGenerate = true)
    private int _id;
    private int creditor_id;
    private double debt_quantity;
    private double debt_paid;
    private String debt_history_paid;
    private String debt_note;

    public int get_id() {
        return _id;
    }

    public void set_id(int _id) {
        this._id = _id;
    }

    public int getCreditor_id() {
        return creditor_id;
    }

    public void setCreditor_id(int creditor_id) {
        this.creditor_id = creditor_id;
    }

    public double getDebt_quantity() {
        return debt_quantity;
    }

    public void setDebt_quantity(double debt_quantity) {
        this.debt_quantity = debt_quantity;
    }

    public double getDebt_paid() {
        return debt_paid;
    }

    public void setDebt_paid(double debt_paid) {
        this.debt_paid = debt_paid;
    }

    public String getDebt_history_paid() {
        return debt_history_paid;
    }

    public void setDebt_history_paid(String debt_history_paid) {
        this.debt_history_paid = debt_history_paid;
    }

    public String getDebt_note() {
        return debt_note;
    }

    public void setDebt_note(String debt_note) {
        this.debt_note = debt_note;
    }

    public Debt(int _id, int creditor_id, double debt_quantity, double debt_paid, String debt_history_paid, String debt_note) {
        this._id = _id;
        this.creditor_id = creditor_id;
        this.debt_quantity = debt_quantity;
        this.debt_paid = debt_paid;
        this.debt_history_paid = debt_history_paid;
        this.debt_note = debt_note;
    }

// Getters and setters
}
