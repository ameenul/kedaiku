package com.example.kedaiku.viewmodel;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.example.kedaiku.UI.penjualan_menu.CartItem;
import com.example.kedaiku.entites.DetailSale;
import com.example.kedaiku.entites.PromoDetail;
import com.example.kedaiku.entites.Sale;
import com.example.kedaiku.entites.SaleWithDetails;
import com.example.kedaiku.helper.DateHelper;
import com.example.kedaiku.repository.OnTransactionCompleteListener;
import com.example.kedaiku.repository.SaleRepository;

import java.util.List;

public class PiutangViewModel extends AndroidViewModel implements DateRangeFilter {

    private final SaleRepository repository;

    private final MutableLiveData<FilterParams> currentFilterParamsPiutang = new MutableLiveData<>();
    private final LiveData<List<SaleWithDetails>> filteredSalesWithDetailsPiutang;


    // Kelas untuk menampung parameter filter
    private static class FilterParams {
        String filterName;
        Long startDate;
        Long endDate;
        String name;


        boolean isFilterByCustomer;


        FilterParams() {}

        FilterParams(String filterName, Long startDate, Long endDate, String Name, boolean isFilterByCustomer) {
            this.filterName = filterName;
            this.startDate = startDate;
            this.endDate = endDate;
            this.name = name;
            this.isFilterByCustomer = isFilterByCustomer;

        }
    }

    public PiutangViewModel(@NonNull Application application) {
        super(application);
        repository = new SaleRepository(application);


        // Inisialisasi untuk filter piutang
        currentFilterParamsPiutang.setValue(new FilterParams("Semua Waktu", null, null, null, false));
        filteredSalesWithDetailsPiutang = Transformations.switchMap(currentFilterParamsPiutang, params -> {
            if (params == null) {
                return repository.getFilteredSalesForPaymentType2WithSearch(
                        0,
                        System.currentTimeMillis(),
                        "",
                        false
                );
            }

            long startDate, endDate;
            String transactionName = params.name != null ? params.name : "";
            boolean isFilterByCustomer=params.isFilterByCustomer;

            if (params.startDate != null && params.endDate != null) {
                startDate = params.startDate;
                endDate = params.endDate;
            } else if (params.filterName != null) {
                long[] dateRange = DateHelper.calculateDateRange(params.filterName);
                startDate = dateRange[0];
                endDate = dateRange[1];
            } else {
                startDate = 0;
                endDate = System.currentTimeMillis();
            }

            // Memanggil repository dengan filter payment_type = 2
            return repository.getFilteredSalesForPaymentType2WithSearch(startDate, endDate, transactionName, isFilterByCustomer);
        });
    }





    // Metode untuk memproses transaksi penjualan
    public void processSale(Sale sale, DetailSale detailSale, PromoDetail promoDetail, List<CartItem> cartItems, OnTransactionCompleteListener listener) {
        boolean isSuccess=false;

        repository.completeTransaction(sale, detailSale, promoDetail, cartItems, new OnTransactionCompleteListener() {
            @Override
            public void onSuccess(boolean status) {
                // Kembali ke main thread supaya UI bisa di-update
                if (listener != null) {
                    new Handler(Looper.getMainLooper()).post(() -> {
                        listener.onSuccess(status);
                    });
                }
            }

            @Override
            public void onFailure(boolean status) {
                if (listener != null) {
                    new Handler(Looper.getMainLooper()).post(() -> {
                        listener.onFailure(status);
                    });
                }
            }
        });

    }

    public void deleteSaleTransaction(long saleId, OnTransactionCompleteListener listener) {
        repository.deleteSaleTransactionAsync(saleId, new OnTransactionCompleteListener() {
            @Override
            public void onSuccess(boolean status) {
                // Kembali ke main thread supaya UI bisa di-update
                if (listener != null) {
                    new Handler(Looper.getMainLooper()).post(() -> {
                        listener.onSuccess(status);
                    });
                }
            }

            @Override
            public void onFailure(boolean status) {
                if (listener != null) {
                    new Handler(Looper.getMainLooper()).post(() -> {
                        listener.onFailure(status);
                    });
                }
            }
        });
    }

    // Operasi insert, update, delete melalui repository
    public void insert(Sale sale) {
        repository.insert(sale);
    }

    public void update(Sale sale) {
        repository.update(sale);
    }

    public void delete(Sale sale) {
        repository.delete(sale);
    }



    public SaleWithDetails getSaleWithDetailsById(long saleId) {
        return repository.getSaleWithDetailsByIdSync(saleId);
    }

    public LiveData<SaleWithDetails> getSaleWithDetailsByIdLive(long saleId) {
        return repository.getSaleWithDetailsByIdLive(saleId);
    }

    public void setFilter(String filterName) {
        FilterParams params = currentFilterParamsPiutang.getValue();
        if (params == null) {
            params = new FilterParams();
        }
        params.filterName = filterName;
        params.startDate = null;
        params.endDate = null;
        currentFilterParamsPiutang.setValue(params);
    }

    public void setIsFilterByCustomer(boolean isFilterByCustomer) {
        FilterParams params = currentFilterParamsPiutang.getValue();
        if (params == null) {
            params = new FilterParams();
        }
        params.isFilterByCustomer = isFilterByCustomer;

        currentFilterParamsPiutang.setValue(params);
    }

    public void setDateRangeFilter(long startDate, long endDate) {
        FilterParams params = currentFilterParamsPiutang.getValue();
        if (params == null) {
            params = new FilterParams();
        }
        params.filterName = null;
        params.startDate = startDate;
        params.endDate = endDate;
        currentFilterParamsPiutang.setValue(params);
    }

    public LiveData<List<SaleWithDetails>> getFilteredSalesWithDetailsPiutang() {
        return filteredSalesWithDetailsPiutang;
    }


    public void setTransactionNameFilter(String name) {
        FilterParams params = currentFilterParamsPiutang.getValue();
        if (params == null) {
            params = new FilterParams();
        }
        params.name = name;
        currentFilterParamsPiutang.setValue(params);
    }

    /**
     * Memperbarui sale_paid dan sale_paid_history dalam satu transaksi
     *
     * @param saleId        ID dari Sale yang ingin diperbarui
     * @param paymentAmount Jumlah pembayaran yang ingin ditambahkan
     * @param listener      Listener untuk menangani hasil transaksi
     */
    public void payPiutang(long saleId, double paymentAmount, OnTransactionCompleteListener listener) {
        repository.payPiutang(saleId, paymentAmount, listener);
    }

}
