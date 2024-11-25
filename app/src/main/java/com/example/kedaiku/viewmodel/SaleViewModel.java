package com.example.kedaiku.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.kedaiku.database.AppDatabase;
import com.example.kedaiku.entites.Sale;
import com.example.kedaiku.ifaceDao.SaleDao;

import java.util.List;

public class SaleViewModel extends AndroidViewModel {
    private SaleDao saleDao;
    private LiveData<List<Sale>> allSales;

    public SaleViewModel(@NonNull Application application) {
        super(application);
        AppDatabase db = AppDatabase.getDatabase(application);
        saleDao = db.saleDao();
        allSales = saleDao.getAllSales();
    }

    public LiveData<List<Sale>> getAllSales() {
        return allSales;
    }

    public void insert(Sale sale) {
        AppDatabase.databaseWriteExecutor.execute(() -> saleDao.insert(sale));
    }

    public void update(Sale sale) {
        AppDatabase.databaseWriteExecutor.execute(() -> saleDao.update(sale));
    }

    public void delete(Sale sale) {
        AppDatabase.databaseWriteExecutor.execute(() -> saleDao.delete(sale));
    }
}

