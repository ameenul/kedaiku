package com.example.kedaiku.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.example.kedaiku.entites.Expense;
import com.example.kedaiku.entites.ExpenseWithCash;
import com.example.kedaiku.entites.SaleWithDetails;
import com.example.kedaiku.helper.DateHelper;
import com.example.kedaiku.repository.ExpenseRepository;
import com.example.kedaiku.repository.OnTransactionCompleteListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ExpenseViewModel extends AndroidViewModel implements DateRangeFilter {

    private final ExpenseRepository repository;
    // LiveData semua expense (dengan relasi cash)

    // MutableLiveData untuk parameter filter (rentang tanggal dan search filter)
    private final MutableLiveData<FilterParams> currentFilterParams = new MutableLiveData<>();
    // LiveData yang dikembalikan sebagai hasil filter, menggunakan query DAO jika parameter filter ada
    private LiveData<List<ExpenseWithCash>> filteredExpenses;

    // --- Filter khusus grafik ---
    private final MutableLiveData<ChartFilterParams> currentChartFilterParams = new MutableLiveData<>();
    // LiveData mentah untuk grafik (data penjualan sesuai rentang yang diinginkan)
    private final LiveData<List<ExpenseWithCash>> filteredExpenseForChart;


    public ExpenseViewModel(@NonNull Application application) {
        super(application);
        repository = new ExpenseRepository(application);
        currentFilterParams.setValue(new FilterParams("Semua Waktu", 0L, DateHelper.getEndOfDay(), ""));

        filteredExpenses = Transformations.switchMap(currentFilterParams, params -> {
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

            return repository.getFilteredExpenseWithSearch(startDate, endDate, search);
        });

        // Inisialisasi filter grafik. Misalnya, defaultnya "Bulan" dengan rentang waktu bulan berjalan.
        long monthStart = DateHelper.getStartOfMonth(); // misalnya getStartOfMonth() tanpa parameter mengembalikan awal bulan berjalan
        long monthEnd = DateHelper.getEndOfMonth();
        currentChartFilterParams.setValue(new ChartFilterParams("Bulan", monthStart, monthEnd));
        filteredExpenseForChart = Transformations.switchMap(currentChartFilterParams, params -> {
            long start, end;
            if ("Bulan".equalsIgnoreCase(params.option)) {
                // Jika opsi adalah "Bulan", gunakan rentang tanggal yang diberikan
                start = params.startDate != null ? params.startDate : DateHelper.getStartOfMonth();
                end = params.endDate != null ? params.endDate : DateHelper.getEndOfMonth();
            } else { // misal opsi "Tahun"
                start = params.startDate != null ? params.startDate : DateHelper.getStartOfYear();
                end = params.endDate != null ? params.endDate : DateHelper.getEndOfYear();
            }
            return repository.getFilteredExpenseWithSearch(start, end, "");
        });


    }

    public void setChartFilter(String option, Long startDate, Long endDate) {
        currentChartFilterParams.setValue(new ChartFilterParams(option, startDate, endDate));
    }
    public ChartFilterParams getCurrentChartFilterParams() {
        return currentChartFilterParams.getValue();
    }

    public LiveData<List<ExpenseWithCash>> getFilteredExpenseForChart() {
        return filteredExpenseForChart;
    }



        public LiveData<List<ChartDataPoint>> getChartData() {
            return Transformations.map(filteredExpenseForChart, expenseList -> {
                List<ChartDataPoint> chartDataPoints = new ArrayList<>();
                ChartFilterParams params = currentChartFilterParams.getValue();
                if (params != null && params.option != null && params.option.equalsIgnoreCase("Bulan")) {
                // Agregasi per hari
                Map<Integer, Double> dayTotals = new java.util.TreeMap<>();
                Calendar cal = Calendar.getInstance();
                // Gunakan params.startDate sebagai dasar; jika null, gunakan bulan berjalan
                long baseTimestamp = (params.startDate != null) ? params.startDate : DateHelper.getStartOfMonth();
                for (ExpenseWithCash expense : expenseList) {
                    cal.setTimeInMillis(expense.getExpense().getExpense_date());
                    int day = cal.get(Calendar.DAY_OF_MONTH);
                    double currentTotal = dayTotals.getOrDefault(day, 0.0);
                    dayTotals.put(day, currentTotal + expense.getExpense().getExpense_amount());
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
                for (ExpenseWithCash expense : expenseList) {
                    cal.setTimeInMillis(expense.getExpense().getExpense_date());
                    int month = cal.get(Calendar.MONTH) + 1; // Calendar.MONTH dimulai dari 0
                    double currentTotal = monthTotals.getOrDefault(month, 0.0);
                    monthTotals.put(month, currentTotal + expense.getExpense().getExpense_amount());
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


    public LiveData<List<ChartDataPoint>> getAllChartData() {
        return Transformations.map(filteredExpenseForChart, expenseList -> {
            List<ChartDataPoint> chartDataPoints = new ArrayList<>();
            ChartFilterParams params = currentChartFilterParams.getValue();
            if (params != null && params.option != null && params.option.equalsIgnoreCase("Bulan")) {
                // Agregasi per hari untuk seluruh hari dalam bulan, meskipun tidak ada penjualan
                Calendar cal = Calendar.getInstance();
                // Gunakan params.startDate sebagai dasar; jika null, gunakan awal bulan berjalan
                long baseTimestamp = (params.startDate != null) ? params.startDate : DateHelper.getStartOfMonth();
                cal.setTimeInMillis(baseTimestamp);
                int maxDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
                // Siapkan map untuk mengagregasi penjualan per hari
                java.util.Map<Integer, Double> dayTotals = new java.util.TreeMap<>();
                // Inisialisasi seluruh hari dengan nilai 0
                for (int day = 1; day <= maxDay; day++) {
                    dayTotals.put(day, 0.0);
                }
                // Tambahkan data dari expenseList
                for (ExpenseWithCash expense : expenseList) {
                    Calendar expCal = Calendar.getInstance();
                    expCal.setTimeInMillis(expense.getExpense().getExpense_date());
                    int day = expCal.get(Calendar.DAY_OF_MONTH);
                    double currentTotal = dayTotals.getOrDefault(day, 0.0);
                    dayTotals.put(day, currentTotal + expense.getExpense().getExpense_amount());
                }
                // Format tanggal menggunakan format "dd/MM/yyyy"
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                // Iterasi setiap hari dari 1 sampai maxDay
                for (int day = 1; day <= maxDay; day++) {
                    Calendar tempCal = (Calendar) cal.clone();
                    tempCal.set(Calendar.DAY_OF_MONTH, day);
                    String label = sdf.format(tempCal.getTime());
                    chartDataPoints.add(new ChartDataPoint(label, dayTotals.get(day)));
                }
            } else if (params != null && params.option != null && params.option.equalsIgnoreCase("Tahun")) {
                // Agregasi per bulan untuk seluruh bulan dalam tahun, meskipun tidak ada penjualan
                Calendar cal = Calendar.getInstance();
                // Gunakan params.startDate sebagai basis; jika null, gunakan awal tahun berjalan
                long baseTimestamp = (params.startDate != null) ? params.startDate : DateHelper.getStartOfYear();
                cal.setTimeInMillis(baseTimestamp);
                // Pastikan kita memiliki 12 bulan
                java.util.Map<Integer, Double> monthTotals = new java.util.TreeMap<>();
                for (int month = 1; month <= 12; month++) {
                    monthTotals.put(month, 0.0);
                }
                // Agregasi data dari expenseList
                for (ExpenseWithCash expense : expenseList) {
                    Calendar expCal = Calendar.getInstance();
                    expCal.setTimeInMillis(expense.getExpense().getExpense_date());
                    int month = expCal.get(Calendar.MONTH) + 1; // +1 karena Calendar.MONTH mulai dari 0
                    double currentTotal = monthTotals.getOrDefault(month, 0.0);
                    monthTotals.put(month, currentTotal + expense.getExpense().getExpense_amount());
                }
                // Format label bulan dengan format "MMM yyyy"
                SimpleDateFormat sdf = new SimpleDateFormat("MMM yyyy", Locale.getDefault());
                for (int month = 1; month <= 12; month++) {
                    Calendar tempCal = (Calendar) cal.clone();
                    tempCal.set(Calendar.MONTH, month - 1);
                    String label = sdf.format(tempCal.getTime());
                    chartDataPoints.add(new ChartDataPoint(label, monthTotals.get(month)));
                }
            }
            return chartDataPoints;
        });
    }



    private MutableLiveData<Long> currentDate = new MutableLiveData<>();


    public LiveData<Long> getCurrentDate() {
        return currentDate;
    }
    public LiveData<List<ExpenseWithCash>> getFilteredExpenses() {
        return filteredExpenses;
    }

    // Implementasi DateRangeFilter

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

    // Operasi CRUD dan transaksi

    public void insert(Expense expense, OnTransactionCompleteListener listener) {
        repository.insertExpense(expense, listener);
    }

    public void deleteExpenseTransaction(Expense expense, OnTransactionCompleteListener listener) {
        repository.deleteExpenseTransaction(expense, listener);
    }

    public void updateExpenseTransaction(Expense oldExpense, Expense newExpense, OnTransactionCompleteListener listener) {
        repository.updateExpenseTransaction(oldExpense, newExpense, listener);
    }

    public LiveData<ExpenseWithCash> getExpenseWithCashByIdLive(long Id) {
        return repository.getExpenseWithCashByIdLive(Id);
    }
    // Kelas pembantu untuk parameter filter
    public static class FilterParams {
        public String filterName;         // Misal: "Hari Ini", "Bulan Ini", "Pilih Tanggal", "Semua Waktu", dll.
        public Long startDate;            // Rentang tanggal mulai (millis)
        public Long endDate;              // Rentang tanggal akhir (millis)
        public String searchString;       // Query pencarian, bisa untuk note atau nama creditor

        public FilterParams() {}

        public FilterParams(String filterName, Long startDate, Long endDate, String searchString) {
            this.filterName = filterName;
            this.startDate = startDate;
            this.endDate = endDate;
            this.searchString = searchString;

        }
    }
}
