


package com.example.kedaiku.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.example.kedaiku.entites.Inventory;
import com.example.kedaiku.helper.DateHelper;
import com.example.kedaiku.repository.InventoryRepository;

import java.util.Calendar;
import java.util.List;

public class InventoryViewModel extends AndroidViewModel {

    private final InventoryRepository repository;

    // Kelas untuk menampung parameter filter
    private static class FilterParams {
        Long stockProductId;
        String filterName;
        Long startDate;
        Long endDate;

        FilterParams() {}

        FilterParams(Long stockProductId, String filterName, Long startDate, Long endDate) {
            this.stockProductId = stockProductId;
            this.filterName = filterName;
            this.startDate = startDate;
            this.endDate = endDate;
        }
    }

    private final MutableLiveData<FilterParams> currentFilterParams = new MutableLiveData<>();

    private final LiveData<List<Inventory>> filteredInventories;

    public InventoryViewModel(@NonNull Application application) {
        super(application);
        repository = new InventoryRepository(application);

        // Inisialisasi filter default
        currentFilterParams.setValue(new FilterParams(null, "Semua Waktu", null, null));

        filteredInventories = Transformations.switchMap(currentFilterParams, params -> {
            if (params == null || params.stockProductId == null) {
                return new MutableLiveData<>(null);
            }

            long stockProductId = params.stockProductId;
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

            return repository.getFilteredInventoryByStockProductId(stockProductId, startDate, endDate);
        });
    }

    // Getter untuk filtered inventories
    public LiveData<List<Inventory>> getFilteredInventories() {
        return filteredInventories;
    }

    // Setter untuk stockProductId
    public void setStockProductId(long stockProductId) {
        FilterParams params = currentFilterParams.getValue();
        if (params == null) {
            params = new FilterParams();
        }
        params.stockProductId = stockProductId;
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


}

