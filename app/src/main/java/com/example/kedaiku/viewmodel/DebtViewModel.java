package com.example.kedaiku.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.example.kedaiku.entites.Debt;
import com.example.kedaiku.entites.DebtWithCreditor;
import com.example.kedaiku.helper.DateHelper;
import com.example.kedaiku.repository.DebtRepository;
import com.example.kedaiku.repository.OnTransactionCompleteListener;

import java.util.List;

public class DebtViewModel extends AndroidViewModel implements DateRangeFilter {

    private final DebtRepository repository;
    private final MutableLiveData<FilterParams> currentFilterParams = new MutableLiveData<>();
    private final LiveData<List<DebtWithCreditor>> filteredDebts;

    // Kelas untuk menampung parameter filter
    public static class FilterParams {
        public String filterName;         // Misal: "Hari Ini", "Bulan Ini", "Pilih Tanggal", "Semua Waktu", dll.
        public Long startDate;            // Rentang tanggal mulai (millis)
        public Long endDate;              // Rentang tanggal akhir (millis)
        public String searchString;       // Query pencarian, bisa untuk debt_note atau nama creditor
        public boolean filterByCreditor;  // Jika true, filter berdasarkan nama creditor; jika false, berdasarkan debt_note

        public FilterParams() {}

        public FilterParams(String filterName, Long startDate, Long endDate, String searchString, boolean filterByCreditor) {
            this.filterName = filterName;
            this.startDate = startDate;
            this.endDate = endDate;
            this.searchString = searchString;
            this.filterByCreditor = filterByCreditor;
        }
    }

    public DebtViewModel(@NonNull Application application) {
        super(application);
        repository = new DebtRepository(application);
        // Inisialisasi filter default: "Semua Waktu", tanpa pencarian, dan filtering berdasarkan debt_note (filterByCreditor = false)
        currentFilterParams.setValue(new FilterParams("Semua Waktu", 0L, DateHelper.getEndOfDay(), "", false));

        filteredDebts = Transformations.switchMap(currentFilterParams, params -> {
            long startDate, endDate;
            // Jika ada rentang tanggal di parameter, gunakan itu; jika tidak, jika filterName tidak "Pilih Tanggal", gunakan perhitungan dari filterName; jika tidak, gunakan default.
            if (params.startDate != null && params.endDate != null) {
                startDate = params.startDate;
                endDate = params.endDate;
            } else if (params.filterName != null && !params.filterName.equalsIgnoreCase("Pilih Tanggal")) {
                long[] range = DateHelper.calculateDateRange(params.filterName);
                startDate = range[0];
                endDate = range[1];
            } else {
                startDate = 0L;
                endDate = DateHelper.getEndOfDay();
            }
            String search = (params.searchString != null) ? params.searchString : "";
            boolean byCreditor = params.filterByCreditor;
            return repository.getFilteredDebtsWithSearch(startDate, endDate, search, byCreditor);
        });
    }

    public LiveData<List<DebtWithCreditor>> getFilteredDebts() {
        return filteredDebts;
    }

    /**
     * Mengupdate filter berdasarkan nama filter (misal "Hari Ini", "Bulan Ini", dsb).
     */
    public void setFilter(String filterName) {
        FilterParams params = currentFilterParams.getValue();
        if (params == null) {
            params = new FilterParams();
        }
        params.filterName = filterName;
        if (!"Pilih Tanggal".equalsIgnoreCase(filterName)) {
            long[] range = DateHelper.calculateDateRange(filterName);
            params.startDate = range[0];
            params.endDate = range[1];
        }
        currentFilterParams.setValue(params);
    }

    /**
     * Mengupdate filter berdasarkan rentang tanggal kustom.
     *
     * @param startDate Tanggal mulai (millis)
     * @param endDate   Tanggal akhir (millis)
     */
    public void setDateRangeFilter(long startDate, long endDate) {
        FilterParams params = currentFilterParams.getValue();
        if (params == null) {
            params = new FilterParams();
        }
        params.filterName = "Pilih Tanggal";
        params.startDate = startDate;
        params.endDate = endDate;
        currentFilterParams.setValue(params);
    }

    /**
     * Mengupdate query pencarian (misal berdasarkan teks pada EditText).
     *
     * @param searchString Query pencarian
     */
    public void setSearchQuery(String searchString) {
        FilterParams params = currentFilterParams.getValue();
        if (params == null) {
            params = new FilterParams();
        }
        params.searchString = searchString;
        currentFilterParams.setValue(params);
    }

    /**
     * Mengatur apakah pencarian akan dilakukan berdasarkan nama creditor.
     *
     * @param filterByCreditor Jika true, pencarian berdasarkan nama creditor; jika false, berdasarkan debt_note.
     */
    public void setIsFilterByCreditor(boolean filterByCreditor) {
        FilterParams params = currentFilterParams.getValue();
        if (params == null) {
            params = new FilterParams();
        }
        params.filterByCreditor = filterByCreditor;
        currentFilterParams.setValue(params);
    }

    // Operasi CRUD dasar melalui repository

    public void insertDebt(Debt debt) {
        repository.insert(debt);
    }

    public void updateDebt(Debt debt) {
        repository.update(debt);
    }

    public void deleteDebt(Debt debt) {
        repository.delete(debt);
    }

    public LiveData<DebtWithCreditor> getDebtWithCreditorByIdLive(long saleId) {
        return repository.getDebtWithCreditorByIdLive(saleId);
    }


    /**
     * Transaksi untuk menambahkan hutang dan mengupdate kas secara atomik.
     *
     * @param cashId   ID kas yang akan diupdate
     * @param amount   Jumlah uang masuk (nominal hutang)
     * @param debt     Objek Debt baru yang akan disimpan
     * @param listener Listener untuk menangani hasil transaksi
     */
    public void addDebtAndUpdateCash(long cashId, double amount, Debt debt, OnTransactionCompleteListener listener) {
        repository.addDebtAndUpdateCash(debt, cashId, amount, listener);
    }




    public void payDebt(long debtId, double amount, long cashId, OnTransactionCompleteListener listener) {
        repository.payDebt(debtId,  cashId,amount, listener);
    }

}
