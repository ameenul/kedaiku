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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class SaleViewModel extends AndroidViewModel implements DateRangeFilter {

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


    // --- Filter khusus grafik ---
    private final MutableLiveData<ChartFilterParams> currentChartFilterParams = new MutableLiveData<>();
    // LiveData mentah untuk grafik (data penjualan sesuai rentang yang diinginkan)
    private final LiveData<List<SaleWithDetails>> filteredSalesForChart;

    public SaleViewModel(@NonNull Application application) {
        super(application);
        repository = new SaleRepository(application);

        // Inisialisasi filter default ke "Semua Waktu"
        currentFilterParams.setValue(new FilterParams("Semua Waktu", null, null, ""));

        filteredSalesWithDetails = Transformations.switchMap(currentFilterParams, params -> {
            if (params == null) {
                return repository.getSalesWithDetailsFiltered(0, DateHelper.getEndOfDay(), "");
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
                endDate = DateHelper.getEndOfDay();
            }

            return repository.getSalesWithDetailsFiltered(startDate, endDate, transactionName);
        });


        // Inisialisasi filter grafik. Misalnya, defaultnya "Bulan" dengan rentang waktu bulan berjalan.
        long monthStart = DateHelper.getStartOfMonth(); // misalnya getStartOfMonth() tanpa parameter mengembalikan awal bulan berjalan
        long monthEnd = DateHelper.getEndOfMonth();
        currentChartFilterParams.setValue(new ChartFilterParams("Bulan", monthStart, monthEnd));
        filteredSalesForChart = Transformations.switchMap(currentChartFilterParams, params -> {
            long start, end;
            if ("Bulan".equalsIgnoreCase(params.option)) {
                // Jika opsi adalah "Bulan", gunakan rentang tanggal yang diberikan
                start = params.startDate != null ? params.startDate : DateHelper.getStartOfMonth();
                end = params.endDate != null ? params.endDate : DateHelper.getEndOfMonth();
            } else { // misal opsi "Tahun"
                start = params.startDate != null ? params.startDate : DateHelper.getStartOfYear();
                end = params.endDate != null ? params.endDate : DateHelper.getEndOfYear();
            }
            return repository.getSalesWithDetailsFiltered(start, end, "");
        });



    }


    // Metode setter untuk filter grafik
    public void setChartFilter(String option, Long startDate, Long endDate) {
        currentChartFilterParams.setValue(new ChartFilterParams(option, startDate, endDate));
    }
    public ChartFilterParams getCurrentChartFilterParams() {
        return currentChartFilterParams.getValue();
    }

    public LiveData<List<SaleWithDetails>> getFilteredSalesForChart() {
        return filteredSalesForChart;
    }



    public LiveData<List<ChartDataPoint>> getChartData() {
        return Transformations.map(filteredSalesForChart, salesList -> {
            List<ChartDataPoint> chartDataPoints = new ArrayList<>();
            ChartFilterParams params = currentChartFilterParams.getValue();
            if (params != null && params.option != null && params.option.equalsIgnoreCase("Bulan")) {
                // Agregasi per hari
                Map<Integer, Double> dayTotals = new java.util.TreeMap<>();
                Calendar cal = Calendar.getInstance();
                // Gunakan params.startDate sebagai dasar; jika null, gunakan bulan berjalan
                long baseTimestamp = (params.startDate != null) ? params.startDate : DateHelper.getStartOfMonth();
                for (SaleWithDetails sale : salesList) {
                    cal.setTimeInMillis(sale.getSaleDate());
                    int day = cal.get(Calendar.DAY_OF_MONTH);
                    double currentTotal = dayTotals.getOrDefault(day, 0.0);
                    dayTotals.put(day, currentTotal + sale.getSaleTotal());
                }
                // Format tanggal lengkap: "dd/MM/yyyy"
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                Calendar baseCal = Calendar.getInstance();
                baseCal.setTimeInMillis(baseTimestamp);
                for (Map.Entry<Integer, Double> entry : dayTotals.entrySet()) {
                    // Hitung tanggal berdasarkan hari offset
                    Calendar tempCal = (Calendar) baseCal.clone();
                    tempCal.add(Calendar.DAY_OF_MONTH, entry.getKey() - 1);
                    String label = sdf.format(tempCal.getTime());
                    chartDataPoints.add(new ChartDataPoint(label, entry.getValue()));
                }
            } else if (params != null && params.option != null && params.option.equalsIgnoreCase("Tahun")) {
                // Agregasi per bulan
                Map<Integer, Double> monthTotals = new java.util.TreeMap<>();
                Calendar cal = Calendar.getInstance();
                // Gunakan params.startDate sebagai basis untuk tahun; jika null, gunakan tahun berjalan
                long baseTimestamp = (params.startDate != null) ? params.startDate : DateHelper.getStartOfYear();
                for (SaleWithDetails sale : salesList) {
                    cal.setTimeInMillis(sale.getSaleDate());
                    int month = cal.get(Calendar.MONTH) + 1; // Calendar.MONTH dimulai dari 0
                    double currentTotal = monthTotals.getOrDefault(month, 0.0);
                    monthTotals.put(month, currentTotal + sale.getSaleTotal());
                }
                // Format label: "MMM yyyy"
                SimpleDateFormat sdf = new SimpleDateFormat("MMM yyyy", Locale.getDefault());
                Calendar baseCal = Calendar.getInstance();
                baseCal.setTimeInMillis(baseTimestamp);
                for (Map.Entry<Integer, Double> entry : monthTotals.entrySet()) {
                    // Atur kalender ke bulan yang sesuai
                    Calendar tempCal = (Calendar) baseCal.clone();
                    tempCal.set(Calendar.MONTH, entry.getKey() - 1);
                    String label = sdf.format(tempCal.getTime());
                    chartDataPoints.add(new ChartDataPoint(label, entry.getValue()));
                }
            }
            return chartDataPoints;
        });
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


    public void updateSaleTransaction(Sale newSale,
                                      String newDetailSale,
                                      String newPromoDetail,
                                      List<CartItem> newCartItems,
                                      OnTransactionCompleteListener listener) {
        // Panggil repository di background thread
        repository.updateSaleTransactionAsync(newSale, newDetailSale, newPromoDetail, newCartItems,
                 new OnTransactionCompleteListener() {
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
