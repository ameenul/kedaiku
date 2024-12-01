package com.example.kedaiku.entites;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "table_expense")
public class Expense {
    @PrimaryKey(autoGenerate = true)
    private long _id;
    @NonNull
    private String expense_date;
    @NonNull
    private String expense_name;
    private int expense_cash_id;
    private double expense_amount;

    public Expense( @NonNull String expense_date, @NonNull String expense_name, int expense_cash_id, double expense_amount) {

        this.expense_date = expense_date;
        this.expense_name = expense_name;
        this.expense_cash_id = expense_cash_id;
        this.expense_amount = expense_amount;
    }

    public long get_id() {
        return _id;
    }

    public void set_id(long _id) {
        this._id = _id;
    }

    @NonNull
    public String getExpense_date() {
        return expense_date;
    }

    public void setExpense_date(@NonNull String expense_date) {
        this.expense_date = expense_date;
    }

    @NonNull
    public String getExpense_name() {
        return expense_name;
    }

    public void setExpense_name(@NonNull String expense_name) {
        this.expense_name = expense_name;
    }

    public int getExpense_cash_id() {
        return expense_cash_id;
    }

    public void setExpense_cash_id(int expense_cash_id) {
        this.expense_cash_id = expense_cash_id;
    }

    public double getExpense_amount() {
        return expense_amount;
    }

    public void setExpense_amount(double expense_amount) {
        this.expense_amount = expense_amount;
    }
// Getters and setters
}

