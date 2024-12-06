package com.example.kedaiku.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.example.kedaiku.database.AppDatabase;
import com.example.kedaiku.entites.Purchase;
import com.example.kedaiku.entites.PurchaseWithProduct;
import com.example.kedaiku.helper.DateHelper;
import com.example.kedaiku.repository.PurchaseRepository;

import java.util.List;

public class PurchaseViewModel extends AndroidViewModel {

    private final PurchaseRepository repository;

    // Kelas untuk menampung parameter filter
    private static class FilterParams {
        String filterName;
        Long startDate;
        Long endDate;

        FilterParams() {}

        FilterParams(String filterName, Long startDate, Long endDate) {
            this.filterName = filterName;
            this.startDate = startDate;
            this.endDate = endDate;
        }
    }

    private final MutableLiveData<FilterParams> currentFilterParams = new MutableLiveData<>();
    private final LiveData<List<PurchaseWithProduct>> filteredPurchasesWithProductName;

    public PurchaseViewModel(@NonNull Application application) {
        super(application);
        repository = new PurchaseRepository(application);

        // Inisialisasi filter default ke "Semua Waktu"
        currentFilterParams.setValue(new FilterParams("Semua Waktu", null, null));

        filteredPurchasesWithProductName = Transformations.switchMap(currentFilterParams, params -> {
            if (params == null) {
                return repository.getFilteredPurchasesWithProductName(0, System.currentTimeMillis());
            }

            long startDate, endDate;

            if (params.startDate != null && params.endDate != null) {
                // Jika filter adalah rentang tanggal khusus
                startDate = params.startDate;
                endDate = params.endDate;
            } else if (params.filterName != null) {
                // Jika filter berdasarkan nama (misalnya "Hari Ini", "Bulan Ini", dll.)
                long[] dateRange = DateHelper.calculateDateRange(params.filterName);
                startDate = dateRange[0];
                endDate = dateRange[1];
            } else {
                // Default ke "Semua Waktu"
                startDate = 0;
                endDate = System.currentTimeMillis();
            }

            return repository.getFilteredPurchasesWithProductName(startDate, endDate);
        });
    }

    // Getter untuk hasil filter
    public LiveData<List<PurchaseWithProduct>> getFilteredPurchasesWithProductName() {
        return filteredPurchasesWithProductName;
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

    // Setter untuk filter berdasarkan rentang tanggal spesifik
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

    // Operasi insert, update, delete melalui repository
    public void insert(Purchase purchase) {
        repository.insert(purchase);
    }

    public void update(Purchase purchase) {
        repository.update(purchase);
    }

    public void delete(Purchase purchase) {
        repository.delete(purchase);
    }
}
