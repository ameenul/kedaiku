package com.example.kedaiku.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.kedaiku.database.AppDatabase;
import com.example.kedaiku.entites.DetailSale;
import com.example.kedaiku.ifaceDao.DetailSaleDao;

import java.util.List;

public class DetailSaleViewModel extends AndroidViewModel {
    private DetailSaleDao detailSaleDao;
    private LiveData<List<DetailSale>> allDetailSales;

    public DetailSaleViewModel(@NonNull Application application) {
        super(application);
        AppDatabase db = AppDatabase.getDatabase(application);
        detailSaleDao = db.detailSaleDao();
        allDetailSales = detailSaleDao.getAllDetailSales();
    }

    public LiveData<List<DetailSale>> getAllDetailSales() {
        return allDetailSales;
    }

    public void insert(DetailSale detailSale) {
        AppDatabase.databaseWriteExecutor.execute(() -> detailSaleDao.insert(detailSale));
    }

    public void update(DetailSale detailSale) {
        AppDatabase.databaseWriteExecutor.execute(() -> detailSaleDao.update(detailSale));
    }

    public void delete(DetailSale detailSale) {
        AppDatabase.databaseWriteExecutor.execute(() -> detailSaleDao.delete(detailSale));
    }
}
