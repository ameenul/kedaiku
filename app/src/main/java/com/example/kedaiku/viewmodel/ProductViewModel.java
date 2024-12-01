//package com.example.kedaiku.viewmodel;
//
//import android.app.Application;
//import android.util.Pair;
//
//import androidx.annotation.NonNull;
//import androidx.lifecycle.AndroidViewModel;
//import androidx.lifecycle.LiveData;
//import androidx.lifecycle.MutableLiveData;
//import androidx.lifecycle.Transformations;
//
//import com.example.kedaiku.entites.Inventory;
//import com.example.kedaiku.entites.Product;
//import com.example.kedaiku.entites.ProductInventory;
//import com.example.kedaiku.entites.Purchase;
//import com.example.kedaiku.repository.ProductRepository;
//
//import java.util.Calendar;
//import java.util.List;
//
//public class ProductViewModel extends AndroidViewModel {
//
//    private final ProductRepository repository;
//    private final LiveData<List<Product>> allProducts;
//
//    // LiveData untuk FilterParam
//    private final MutableLiveData<FilterParam> filterParamLiveData = new MutableLiveData<>();
//
//    // LiveData hasil transformasi berdasarkan filter
//    private final LiveData<List<ProductInventory>> filteredProductInventory;
//
//    public ProductViewModel(@NonNull Application application) {
//        super(application);
//        repository = new ProductRepository(application);
//        allProducts = repository.getAllProducts();
//        filterParamLiveData.setValue(new FilterParam("semua waktu"));
//
//        // Inisialisasi LiveData hasil transformasi
//        filteredProductInventory = Transformations.switchMap(filterParamLiveData, filterParam -> {
//            String filterType = filterParam.getFilterType();
//            long startDate, endDate;
//
//            if ("Pilih Tanggal".equals(filterType) && filterParam.getStartDate() != null && filterParam.getEndDate() != null) {
//                startDate = filterParam.getStartDate();
//                endDate = filterParam.getEndDate();
//            } else {
//                Pair<Long, Long> dateRange = calculateDateRange(filterType);
//                startDate = dateRange.first;
//                endDate = dateRange.second;
//            }
//
//            return repository.getFilteredProductInventory(startDate, endDate);
//        });
//    }
//
//    // Metode untuk mengatur filter
//    public void setFilter(String filterType) {
//
//        FilterParam params = filterParamLiveData.getValue();
//        if (params == null) {
//            params = new FilterParam(filterType);
//        }
//        params.filterType = filterType;
//        filterParamLiveData.setValue(params);
//    }
//
//    // Metode untuk mengatur rentang tanggal khusus
//    public void setCustomDateRange(long startDate, long endDate) {
//        //filterParamLiveData.setValue(new FilterParam("Pilih Tanggal", startDate, endDate));
//
//        FilterParam params = filterParamLiveData.getValue();
//        if (params == null) {
//            params = new FilterParam("Pilih Tanggal", startDate, endDate);
//        }
//        params.filterType = "Pilih Tanggal";
//        params.startDate = startDate;
//        params.endDate = endDate;
//        filterParamLiveData.setValue(params);
//    }
//
//    // Getter untuk filteredProductInventory
//    public LiveData<List<ProductInventory>> getFilteredProductInventory() {
//        return filteredProductInventory;
//    }
//
//    // Getter untuk allProducts
//    public LiveData<List<Product>> getAllProducts() {
//        return allProducts;
//    }
//
//    // Metode lainnya (insert, update, delete, dll.)
//    public LiveData<List<Product>> searchProducts(String query) {
//        return repository.searchProducts(query);
//    }
//
//    public void insert(Product product) {
//        repository.insert(product);
//    }
//
//    public void updateProductWithInventory(Product product, double oldQty) {
//        repository.updateProductWithInventory(product, oldQty);
//    }
//
//    public void updateProductWithInventory(Product product, double oldQty, String stock_note) {
//        repository.updateProductWithInventory(product, oldQty, stock_note);
//    }
//
//    public void delete(Product product) {
//        repository.delete(product);
//    }
//
//    public LiveData<Product> getProductById(long productId) {
//        return repository.getProductById(productId);
//    }
//
//    public void update(Product product) {
//        repository.update(product);
//    }
//
//    public void insertProductWithInventory(Product product, Inventory inventory) {
//        repository.insertProductWithInventory(product, inventory);
//    }
//
//    // Metode untuk menghitung rentang tanggal berdasarkan filter
//    private Pair<Long, Long> calculateDateRange(String filterType) {
//        long startDate, endDate;
//
//        switch (filterType) {
//            case "Hari Ini":
//                startDate = getStartOfDay();
//                endDate = getEndOfDay();
//                break;
//            case "Kemarin":
//                startDate = getStartOfYesterday();
//                endDate = getEndOfYesterday();
//                break;
//            case "Bulan Ini":
//                startDate = getStartOfMonth();
//                endDate = getEndOfDay();
//                break;
//            case "Bulan Lalu":
//                startDate = getStartOfLastMonth();
//                endDate = getEndOfLastMonth();
//                break;
//            default: // "Semua Waktu"
//                startDate = 0;
//                endDate = getEndOfDay();
//        }
//
//        return new Pair<>(startDate, endDate);
//    }
//
//    // Metode untuk mendapatkan waktu awal dan akhir hari, bulan, dll.
//    private long getStartOfDay() {
//        Calendar calendar = Calendar.getInstance();
//        calendar.set(Calendar.HOUR_OF_DAY, 0);
//        calendar.set(Calendar.MINUTE, 0);
//        calendar.set(Calendar.SECOND, 0);
//        calendar.set(Calendar.MILLISECOND, 0);
//        return calendar.getTimeInMillis();
//    }
//
//    private long getEndOfDay() {
//        Calendar calendar = Calendar.getInstance();
//        calendar.set(Calendar.HOUR_OF_DAY, 23);
//        calendar.set(Calendar.MINUTE, 59);
//        calendar.set(Calendar.SECOND, 59);
//        calendar.set(Calendar.MILLISECOND, 999);
//        return calendar.getTimeInMillis();
//    }
//
//    private long getStartOfYesterday() {
//        Calendar calendar = Calendar.getInstance();
//        calendar.add(Calendar.DATE, -1);
//        calendar.set(Calendar.HOUR_OF_DAY, 0);
//        calendar.set(Calendar.MINUTE, 0);
//        calendar.set(Calendar.SECOND, 0);
//        calendar.set(Calendar.MILLISECOND, 0);
//        return calendar.getTimeInMillis();
//    }
//
//    private long getEndOfYesterday() {
//        Calendar calendar = Calendar.getInstance();
//        calendar.add(Calendar.DATE, -1);
//        calendar.set(Calendar.HOUR_OF_DAY, 23);
//        calendar.set(Calendar.MINUTE, 59);
//        calendar.set(Calendar.SECOND, 59);
//        calendar.set(Calendar.MILLISECOND, 999);
//        return calendar.getTimeInMillis();
//    }
//
//    private long getStartOfMonth() {
//        Calendar calendar = Calendar.getInstance();
//        calendar.set(Calendar.DAY_OF_MONTH, 1);
//        calendar.set(Calendar.HOUR_OF_DAY, 0);
//        calendar.set(Calendar.MINUTE, 0);
//        calendar.set(Calendar.SECOND, 0);
//        calendar.set(Calendar.MILLISECOND, 0);
//        return calendar.getTimeInMillis();
//    }
//
//    private long getEndOfLastMonth() {
//        Calendar calendar = Calendar.getInstance();
//        calendar.set(Calendar.DAY_OF_MONTH, 1);
//        calendar.add(Calendar.MILLISECOND, -1);
//        return calendar.getTimeInMillis();
//    }
//
//    private long getStartOfLastMonth() {
//        Calendar calendar = Calendar.getInstance();
//        calendar.add(Calendar.MONTH, -1);
//        calendar.set(Calendar.DAY_OF_MONTH, 1);
//        calendar.set(Calendar.HOUR_OF_DAY, 0);
//        calendar.set(Calendar.MINUTE, 0);
//        calendar.set(Calendar.SECOND, 0);
//        calendar.set(Calendar.MILLISECOND, 0);
//        return calendar.getTimeInMillis();
//    }
//
//    // Kelas inner statis FilterParam
//    public static class FilterParam {
//        private String filterType;
//        private Long startDate;
//        private Long endDate;
//
//        public FilterParam(String filterType) {
//            this.filterType = filterType;
//        }
//
//        public FilterParam(String filterType, Long startDate, Long endDate) {
//            this.filterType = filterType;
//            this.startDate = startDate;
//            this.endDate = endDate;
//        }
//
//        // Getters dan Setters
//        public String getFilterType() {
//            return filterType;
//        }
//
//        public void setFilterType(String filterType) {
//            this.filterType = filterType;
//        }
//
//        public Long getStartDate() {
//            return startDate;
//        }
//
//        public void setStartDate(Long startDate) {
//            this.startDate = startDate;
//        }
//
//        public Long getEndDate() {
//            return endDate;
//        }
//
//        public void setEndDate(Long endDate) {
//            this.endDate = endDate;
//        }
//    }
//
//
//    public void processPurchase(
//            Purchase purchase,
//            double selectedPrice,
//            Inventory inventory,
//            Product product,
//            long cashId,
//            double purchaseAmount,
//            String cashFlowDescription,
//            ProductRepository.OnTransactionCompleteListener listener
//    ) {
//        repository.processPurchase(
//                purchase,
//                selectedPrice,
//                inventory,
//                product,
//                cashId,
//                purchaseAmount,
//                cashFlowDescription,
//                listener
//        );
//    }
//
//    public void deletePurchaseTransaction(Purchase purchase, ProductRepository.OnTransactionCompleteListener listener) {
//        repository.deletePurchaseTransaction(purchase, listener);
//    }
//
//
//}


package com.example.kedaiku.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.example.kedaiku.entites.Inventory;
import com.example.kedaiku.entites.Product;
import com.example.kedaiku.entites.ProductInventory;
import com.example.kedaiku.entites.Purchase;
import com.example.kedaiku.helper.DateHelper;
import com.example.kedaiku.repository.ProductRepository;

import java.util.List;

public class ProductViewModel extends AndroidViewModel {

    private final ProductRepository repository;
    private final LiveData<List<Product>> allProducts;

    // MutableLiveData untuk query pencarian produk (List<Product>)
    private final MutableLiveData<String> searchQueryLiveData = new MutableLiveData<>();
    // LiveData hasil pencarian produk
    private final LiveData<List<Product>> searchResults;

    // MutableLiveData untuk parameter filter tanggal (List<ProductInventory>)
    private final MutableLiveData<FilterParam> filterParamLiveData = new MutableLiveData<>();
    // LiveData hasil filter produk berdasarkan tanggal
    private final LiveData<List<ProductInventory>> filteredProductInventory;

    public ProductViewModel(@NonNull Application application) {
        super(application);
        repository = new ProductRepository(application);
        allProducts = repository.getAllProducts();

        // Inisialisasi nilai awal
        searchQueryLiveData.setValue("");
        filterParamLiveData.setValue(new FilterParam("Semua Waktu", null, null));

        // Inisialisasi searchResults menggunakan Transformations.switchMap
        searchResults = Transformations.switchMap(searchQueryLiveData, query -> {
            if (query == null || query.isEmpty()) {
                // Jika query kosong, kembalikan semua produk
                return allProducts;
            } else {
                // Jika ada query, kembalikan hasil pencarian
                return repository.searchProducts(query);
            }
        });

        // Inisialisasi filteredProductInventory menggunakan Transformations.switchMap
        filteredProductInventory = Transformations.switchMap(filterParamLiveData, filterParam -> {
            String filterType = filterParam.getFilterType();
            long startDate, endDate;

            if ("Pilih Tanggal".equals(filterType) && filterParam.getStartDate() != null && filterParam.getEndDate() != null) {
                // Jika filter khusus rentang tanggal
                startDate = filterParam.getStartDate();
                endDate = filterParam.getEndDate();
            } else {
                // Gunakan DateHelper untuk menghitung rentang tanggal berdasarkan filterType
                long[] dateRange = DateHelper.calculateDateRange(filterType);
                startDate = dateRange[0];
                endDate = dateRange[1];
            }

            // Kembalikan data produk yang difilter berdasarkan tanggal
            return repository.getFilteredProductInventory(startDate, endDate);
        });
    }

    // Metode untuk mengatur query pencarian
    public void setSearchQuery(String query) {
        searchQueryLiveData.setValue(query.trim());
    }

    // Getter untuk hasil pencarian produk
    public LiveData<List<Product>> getSearchResults() {
        return searchResults;
    }

    // Metode untuk mengatur filter tanggal
    public void setFilter(String filterType) {
        FilterParam params = filterParamLiveData.getValue();
        if (params == null) {
            params = new FilterParam(filterType, null, null);
        } else {
            params.setFilterType(filterType);
            params.setStartDate(null);
            params.setEndDate(null);
        }
        filterParamLiveData.setValue(params);
    }

    // Metode untuk mengatur rentang tanggal khusus
    public void setCustomDateRange(long startDate, long endDate) {
        FilterParam params = filterParamLiveData.getValue();
        if (params == null) {
            params = new FilterParam("Pilih Tanggal", startDate, endDate);
        } else {
            params.setFilterType("Pilih Tanggal");
            params.setStartDate(startDate);
            params.setEndDate(endDate);
        }
        filterParamLiveData.setValue(params);
    }

    // Getter untuk hasil filter produk berdasarkan tanggal
    public LiveData<List<ProductInventory>> getFilteredProductInventory() {
        return filteredProductInventory;
    }

    // Getter untuk semua produk
    public LiveData<List<Product>> getAllProducts() {
        return allProducts;
    }

    // Metode lainnya (insert, update, delete, dll.)
    public void insert(Product product) {
        repository.insert(product);
    }

    public void updateProductWithInventory(Product product, double oldQty) {
        repository.updateProductWithInventory(product, oldQty);
    }

    public void updateProductWithInventory(Product product, double oldQty, String stockNote) {
        repository.updateProductWithInventory(product, oldQty, stockNote);
    }

    public void delete(Product product) {
        repository.delete(product);
    }

    public LiveData<Product> getProductById(long productId) {
        return repository.getProductById(productId);
    }

    public void update(Product product) {
        repository.update(product);
    }

    public void insertProductWithInventory(Product product, Inventory inventory) {
        repository.insertProductWithInventory(product, inventory);
    }

    // Kelas inner FilterParam
    public static class FilterParam {
        private String filterType;
        private Long startDate;
        private Long endDate;

        public FilterParam(String filterType) {
            this.filterType = filterType;
        }

        public FilterParam(String filterType, Long startDate, Long endDate) {
            this.filterType = filterType;
            this.startDate = startDate;
            this.endDate = endDate;
        }

        // Getters dan Setters
        public String getFilterType() {
            return filterType;
        }

        public void setFilterType(String filterType) {
            this.filterType = filterType;
        }

        public Long getStartDate() {
            return startDate;
        }

        public void setStartDate(Long startDate) {
            this.startDate = startDate;
        }

        public Long getEndDate() {
            return endDate;
        }

        public void setEndDate(Long endDate) {
            this.endDate = endDate;
        }
    }

    // Metode untuk proses pembelian
    public void processPurchase(
            Purchase purchase,
            double selectedPrice,
            Inventory inventory,
            Product product,
            long cashId,
            double purchaseAmount,
            String cashFlowDescription,
            ProductRepository.OnTransactionCompleteListener listener
    ) {
        repository.processPurchase(
                purchase,
                selectedPrice,
                inventory,
                product,
                cashId,
                purchaseAmount,
                cashFlowDescription,
                listener
        );
    }

    public void deletePurchaseTransaction(Purchase purchase, ProductRepository.OnTransactionCompleteListener listener) {
        repository.deletePurchaseTransaction(purchase, listener);
    }
}
