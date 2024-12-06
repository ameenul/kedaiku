


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

public class ProductInventoryViewModel extends AndroidViewModel {

    private final ProductRepository repository;

    // MutableLiveData untuk parameter filter tanggal (List<ProductInventory>)
    private final MutableLiveData<FilterParam> filterParamLiveData = new MutableLiveData<>();
    // LiveData hasil filter produk berdasarkan tanggal
    private final LiveData<List<ProductInventory>> filteredProductInventory;

    public ProductInventoryViewModel(@NonNull Application application) {
        super(application);
        repository = new ProductRepository(application);


        filterParamLiveData.setValue(new ProductInventoryViewModel.FilterParam("Semua Waktu", "", null, null));
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
            return repository.getFilteredProductInventory(startDate, endDate, filterParam.name);
        });



    }




    // Metode untuk mengatur filter tanggal
    public void setFilter(String filterType,String name) {
        FilterParam params = filterParamLiveData.getValue();
        if (params == null) {
            params = new FilterParam(filterType,name, null, null);
        } else {
            params.setFilterType(filterType);
            params.setStartDate(null);
            params.setEndDate(null);
            params.setName(name);
        }
        filterParamLiveData.setValue(params);
    }

    // Metode untuk mengatur rentang tanggal khusus
    public void setCustomDateRange(long startDate, long endDate,String name) {
        FilterParam params = filterParamLiveData.getValue();
        if (params == null) {
            params = new FilterParam("Pilih Tanggal",name, startDate, endDate);
        } else {
            params.setFilterType("Pilih Tanggal");
            params.setStartDate(startDate);
            params.setEndDate(endDate);
            params.setName(name);
        }
        filterParamLiveData.setValue(params);
    }

    // Getter untuk hasil filter produk berdasarkan tanggal
    public LiveData<List<ProductInventory>> getFilteredProductInventory() {
        return filteredProductInventory;
    }






    // Kelas inner FilterParam
    public static class FilterParam {
        private String filterType;

        public FilterParam(String filterType, String name, Long startDate, Long endDate) {
            this.filterType = filterType;
            this.name = name;
            this.startDate = startDate;
            this.endDate = endDate;
        }

        public FilterParam(String filterType, Long startDate, Long endDate) {
            this.filterType = filterType;
            this.startDate = startDate;
            this.endDate = endDate;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        private String name;
        private Long startDate;
        private Long endDate;


        public FilterParam(String filterType) {
            this.filterType = filterType;
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


}
