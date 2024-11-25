package com.example.kedaiku.ifaceDao;


import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.kedaiku.entites.CashFlow;

import java.util.List;

@Dao
public interface CashFlowDao {

    @Insert
    void insert(CashFlow cashFlow);

    @Query("SELECT * FROM table_cash_flow WHERE cash_id = :cashId ORDER BY cash_date DESC")
    LiveData<List<CashFlow>> getCashFlowByCashId(long cashId);

    @Query("SELECT * FROM table_cash_flow WHERE cash_id = :cashId AND cash_date BETWEEN :startDate AND :endDate ORDER BY cash_date DESC")
    LiveData<List<CashFlow>> getCashFlowByCashIdAndDateRange(long cashId, long startDate, long endDate);
}
