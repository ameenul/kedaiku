//package com.example.kedaiku.viewmodel;
//
//import android.app.Application;
//
//import androidx.lifecycle.AndroidViewModel;
//import androidx.lifecycle.LiveData;
//
//import com.example.kedaiku.entites.CashFlow;
//import com.example.kedaiku.repository.CashFlowRepository;
//
//import java.util.List;
//
//public class CashFlowViewModel extends AndroidViewModel {
//
//    private CashFlowRepository cashFlowRepository;
//
//    public CashFlowViewModel(Application application) {
//        super(application);
//        cashFlowRepository = new CashFlowRepository(application);
//    }
//
//    public LiveData<List<CashFlow>> getCashFlowByCashId(long cashId) {
//        return cashFlowRepository.getCashFlowByCashId(cashId);
//    }
//
//    public LiveData<List<CashFlow>> getCashFlowByCashIdAndDateRange(long cashId, long startDate, long endDate) {
//        return cashFlowRepository.getCashFlowByCashIdAndDateRange(cashId, startDate, endDate);
//    }
//}


package com.example.kedaiku.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.example.kedaiku.entites.CashFlow;
import com.example.kedaiku.helper.DateHelper;
import com.example.kedaiku.repository.CashFlowRepository;

import java.util.Calendar;
import java.util.List;

public class CashFlowViewModel extends AndroidViewModel {

    private final CashFlowRepository repository;

    // Kelas untuk menampung parameter filter
    private static class FilterParams {
        Long cashId;
        String filterName;
        Long startDate;
        Long endDate;

        FilterParams() {}

        FilterParams(Long cashId, String filterName, Long startDate, Long endDate) {
            this.cashId = cashId;
            this.filterName = filterName;
            this.startDate = startDate;
            this.endDate = endDate;
        }
    }

    private final MutableLiveData<FilterParams> currentFilterParams = new MutableLiveData<>();

    private final LiveData<List<CashFlow>> filteredCashFlows;

    public CashFlowViewModel(@NonNull Application application) {
        super(application);
        repository = new CashFlowRepository(application);

        // Inisialisasi filter default
        currentFilterParams.setValue(new FilterParams(null, "Semua Waktu", null, null));

        filteredCashFlows = Transformations.switchMap(currentFilterParams, params -> {
            if (params == null || params.cashId == null) {
                return new MutableLiveData<>(null);
            }

            long cashId = params.cashId;
            long startDate, endDate;

            if (params.startDate != null && params.endDate != null) {
                startDate = params.startDate;
                endDate = params.endDate;
            } else if (params.filterName != null) {
                long[] dateRange = DateHelper.calculateDateRange(params.filterName);
                startDate = dateRange[0];
                endDate = dateRange[1];
            } else {
                // Default ke "Semua Waktu"
                startDate = 0;
                endDate = System.currentTimeMillis();
            }

            return repository.getCashFlowByCashIdAndDateRange(cashId, startDate, endDate);
        });
    }

    // Getter untuk filtered cash flows
    public LiveData<List<CashFlow>> getFilteredCashFlows() {
        return filteredCashFlows;
    }

    // Setter untuk cashId
    public void setCashId(long cashId) {
        FilterParams params = currentFilterParams.getValue();
        if (params == null) {
            params = new FilterParams();
        }
        params.cashId = cashId;
        currentFilterParams.setValue(params);
    }

    // Setter untuk filter berdasarkan nama filter
    public void setFilter(String filterName) {
        FilterParams params = currentFilterParams.getValue();
        if (params == null) {
            params = new FilterParams();
        }
        params.filterName = filterName;
        params.startDate = null;
        params.endDate = null;
        currentFilterParams.setValue(params);
    }

    // Setter untuk filter berdasarkan rentang tanggal
    public void setDateRangeFilter(long startDate, long endDate) {
        FilterParams params = currentFilterParams.getValue();
        if (params == null) {
            params = new FilterParams();
        }
        params.filterName = null;
        params.startDate = startDate;
        params.endDate = endDate;
        currentFilterParams.setValue(params);
    }

        public LiveData<List<CashFlow>> getCashFlowByCashId(long cashId) {
        return repository.getCashFlowByCashId(cashId);
    }

}
