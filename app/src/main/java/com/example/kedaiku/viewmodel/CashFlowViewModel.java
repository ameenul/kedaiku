package com.example.kedaiku.viewmodel;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.kedaiku.entites.CashFlow;
import com.example.kedaiku.repository.CashFlowRepository;

import java.util.List;

public class CashFlowViewModel extends AndroidViewModel {

    private CashFlowRepository cashFlowRepository;

    public CashFlowViewModel(Application application) {
        super(application);
        cashFlowRepository = new CashFlowRepository(application);
    }

    public LiveData<List<CashFlow>> getCashFlowByCashId(long cashId) {
        return cashFlowRepository.getCashFlowByCashId(cashId);
    }

    public LiveData<List<CashFlow>> getCashFlowByCashIdAndDateRange(long cashId, long startDate, long endDate) {
        return cashFlowRepository.getCashFlowByCashIdAndDateRange(cashId, startDate, endDate);
    }
}
