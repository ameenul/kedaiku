package com.example.kedaiku.ifaceDao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import com.example.kedaiku.entites.Debt;
import com.example.kedaiku.entites.DebtWithCreditor;
import com.example.kedaiku.entites.SaleWithDetails;

import java.util.List;

@Dao
public interface DebtDao {
    @Insert
    void insert(Debt debt);

    @Update
    void update(Debt debt);

    @Delete
    void delete(Debt debt);

    @Query("SELECT * FROM table_debt")
    LiveData<List<Debt>> getAllDebts();

    @Transaction
    @Query("SELECT * FROM table_debt WHERE _id = :saleId LIMIT 1")
    LiveData<DebtWithCreditor> getDebtWithCreditorByIdLive(long saleId);

    @Transaction
    @Query("""
    SELECT table_debt.* FROM table_debt
    INNER JOIN table_creditor ON table_debt.creditor_id = table_creditor._id
    WHERE (:startDate IS NULL OR debt_date >= :startDate)
      AND (:endDate IS NULL OR debt_date <= :endDate)
      AND (
          (:filterByCreditor = 0 AND debt_note LIKE '%' || :searchString || '%')
          OR
          (:filterByCreditor = 1 AND creditor_name LIKE '%' || :searchString || '%')
      )
    ORDER BY debt_date DESC
""")
    LiveData<List<DebtWithCreditor>> getFilteredDebtsWithSearch(long startDate, long endDate, String searchString, boolean filterByCreditor);


    @Query("SELECT * FROM table_debt WHERE _id = :debtId LIMIT 1")
    Debt getDebtByIdSync(long debtId);

}
