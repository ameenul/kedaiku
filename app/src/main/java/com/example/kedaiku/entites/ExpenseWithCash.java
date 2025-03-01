package com.example.kedaiku.entites;

import androidx.room.Embedded;
import androidx.room.Relation;

public class ExpenseWithCash {
    @Embedded
    private Expense expense;

    @Relation(
            parentColumn = "expense_cash_id",
            entityColumn = "id"
    )
    private Cash cash;

    public Expense getExpense() {
        return expense;
    }

    public void setExpense(Expense expense) {
        this.expense = expense;
    }

    public Cash getCash() {
        return cash;
    }

    public void setCash(Cash cash) {
        this.cash = cash;
    }
}
