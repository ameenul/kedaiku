package com.example.kedaiku.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.example.kedaiku.ifaceDao.CashFlowDao;
import com.example.kedaiku.database.AppDatabase;
import com.example.kedaiku.entites.CashFlow;

import java.util.List;

public class CashFlowRepository {

    private CashFlowDao cashFlowDao;

    public CashFlowRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        cashFlowDao = db.cashFlowDao();
    }

    public LiveData<List<CashFlow>> getCashFlowByCashId(long cashId) {
        return cashFlowDao.getCashFlowByCashId(cashId);
    }

    public LiveData<List<CashFlow>> getCashFlowByCashIdAndDateRange(long cashId, long startDate, long endDate) {
        return cashFlowDao.getCashFlowByCashIdAndDateRange(cashId, startDate, endDate);
    }
}
