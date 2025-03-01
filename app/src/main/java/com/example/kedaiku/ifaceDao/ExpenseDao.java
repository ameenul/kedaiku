package com.example.kedaiku.ifaceDao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;
import com.example.kedaiku.entites.Expense;
import com.example.kedaiku.entites.ExpenseWithCash;

import java.util.List;

@Dao
public interface ExpenseDao {
    @Transaction
    @Query("SELECT * FROM table_expense")
    LiveData<List<ExpenseWithCash>> getAllExpensesWithCash();

    @Insert
    long insertExpense(Expense expense);

    @Update
    void updateExpense(Expense expense);

    @Delete
    void deleteExpense(Expense expense);

    @Transaction
    @Query("""
    SELECT table_expense.*, table_cash.*
    FROM table_expense
    INNER JOIN table_cash ON table_expense.expense_cash_id = table_cash.id
    WHERE expense_date BETWEEN :startDate AND :endDate
      AND expense_name LIKE '%' || :searchString || '%'
""")
    LiveData<List<ExpenseWithCash>> getFilteredExpenseWithSearch(long startDate, long endDate, String searchString);

    @Transaction
    @Query("SELECT * FROM table_expense WHERE _id = :Id LIMIT 1")
    LiveData<ExpenseWithCash> getExpenseWithCashByIdLive(long Id);

}

