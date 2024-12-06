package com.example.kedaiku.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.example.kedaiku.database.AppDatabase;
import com.example.kedaiku.entites.Wholesale;
import com.example.kedaiku.entites.WholesaleWithProduct;
import com.example.kedaiku.ifaceDao.WholesaleDao;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class WholesaleRepository {

    private final WholesaleDao wholesaleDao;
    private final LiveData<List<Wholesale>> allWholesales;
    private final ExecutorService executorService;

    public WholesaleRepository(Application application) {
        AppDatabase database = AppDatabase.getDatabase(application);
        wholesaleDao = database.wholesaleDao();
        allWholesales = wholesaleDao.getAllWholesales();
        executorService = Executors.newSingleThreadExecutor();
    }

    // Insert
    public void insert(Wholesale wholesale) {
        executorService.execute(() -> wholesaleDao.insert(wholesale));
    }

    // Update
    public void update(Wholesale wholesale) {
        executorService.execute(() -> wholesaleDao.update(wholesale));
    }

    // Delete
    public void delete(Wholesale wholesale) {
        executorService.execute(() -> wholesaleDao.delete(wholesale));
    }

    // Get all wholesales
    public LiveData<List<Wholesale>> getAllWholesales() {
        return allWholesales;
    }

    // Get wholesales with product using LIKE search
    public LiveData<List<WholesaleWithProduct>> getWholesaleWithProductLike(String searchKeyword) {
        return new LiveData<List<WholesaleWithProduct>>() {
            {
                executorService.execute(() -> postValue(wholesaleDao.getWholesaleWithProductLike(searchKeyword)));
            }
        };
    }
}
