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
import com.example.kedaiku.entites.CashFlow;
import com.example.kedaiku.entites.DetailSale;
import com.example.kedaiku.entites.Inventory;
import com.example.kedaiku.entites.PromoDetail;
import com.example.kedaiku.entites.Sale;
import com.example.kedaiku.entites.SaleWithDetails;
import com.example.kedaiku.helper.DateHelper;
import com.example.kedaiku.repository.SaleRepository;

import java.util.List;

public class SaleViewModel extends AndroidViewModel {

    private final SaleRepository repository;

    // Kelas untuk menampung parameter filter
    private static class FilterParams {
        String filterName;
        Long startDate;
        Long endDate;
        String transactionName;

        FilterParams() {}

        FilterParams(String filterName, Long startDate, Long endDate, String transactionName) {
            this.filterName = filterName;
            this.startDate = startDate;
            this.endDate = endDate;
            this.transactionName = transactionName;
        }
    }

    private final MutableLiveData<FilterParams> currentFilterParams = new MutableLiveData<>();
    private final LiveData<List<SaleWithDetails>> filteredSalesWithDetails;

    public SaleViewModel(@NonNull Application application) {
        super(application);
        repository = new SaleRepository(application);

        // Inisialisasi filter default ke "Semua Waktu"
        currentFilterParams.setValue(new FilterParams("Semua Waktu", null, null, ""));

        filteredSalesWithDetails = Transformations.switchMap(currentFilterParams, params -> {
            if (params == null) {
                return repository.getSalesWithDetailsFiltered(0, System.currentTimeMillis(), "");
            }

            long startDate, endDate;
            String transactionName = params.transactionName != null ? params.transactionName : "";

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

            return repository.getSalesWithDetailsFiltered(startDate, endDate, transactionName);
        });
    }

    // Callback di level ViewModel
    public interface OnTransactionCompleteListener {
        void onSuccess(boolean status);
        void onFailure(boolean status);
    }

    // Getter untuk hasil filter
    public LiveData<List<SaleWithDetails>> getFilteredSalesWithDetails() {
        return filteredSalesWithDetails;
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

    // Setter untuk filter berdasarkan nama transaksi
    public void setTransactionNameFilter(String transactionName) {
        FilterParams params = currentFilterParams.getValue();
        if (params == null) {
            params = new FilterParams();
        }
        params.transactionName = transactionName;
        currentFilterParams.setValue(params);
    }

    // Metode untuk memproses transaksi penjualan
    public void processSale(Sale sale, DetailSale detailSale, PromoDetail promoDetail, List<CartItem> cartItems,OnTransactionCompleteListener listener) {
        boolean isSuccess=false;

        repository.completeTransaction(sale, detailSale, promoDetail, cartItems, new SaleRepository.OnTransactionCompleteListener() {
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
        repository.deleteSaleTransactionAsync(saleId, new SaleRepository.OnTransactionCompleteListener() {
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


    public void updateSaleTransaction(Sale newSale,
                                      String newDetailSale,
                                      String newPromoDetail,
                                      List<CartItem> newCartItems,
                                      OnTransactionCompleteListener listener) {
        // Panggil repository di background thread
        repository.updateSaleTransactionAsync(newSale, newDetailSale, newPromoDetail, newCartItems,
                new SaleRepository.OnTransactionCompleteListener() {
                    @Override
                    public void onSuccess(boolean status) {
                        // Kembali ke main thread untuk update UI
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
                }
        );
    }


    public SaleWithDetails getSaleWithDetailsById(long saleId) {
        return repository.getSaleWithDetailsByIdSync(saleId);
    }

    public LiveData<SaleWithDetails> getSaleWithDetailsByIdLive(long saleId) {
        return repository.getSaleWithDetailsByIdLive(saleId);
    }




}
