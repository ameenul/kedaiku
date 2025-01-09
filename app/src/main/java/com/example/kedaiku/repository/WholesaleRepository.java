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
    private final ExecutorService executorService;

    public WholesaleRepository(Application application) {
        AppDatabase database = AppDatabase.getDatabase(application);
        wholesaleDao = database.wholesaleDao();
        executorService = Executors.newFixedThreadPool(2);
    }

    public void insert(Wholesale wholesale) {
        executorService.execute(() -> wholesaleDao.insert(wholesale));
    }

    public void update(Wholesale wholesale) {
        executorService.execute(() -> wholesaleDao.update(wholesale));
    }

    public void delete(Wholesale wholesale) {
        executorService.execute(() -> wholesaleDao.delete(wholesale));
    }

    public LiveData<List<Wholesale>> getAllWholesales() {
        return wholesaleDao.getAllWholesales();
    }

    public LiveData<List<WholesaleWithProduct>> getWholesaleWithProductLike(String searchKeyword) {
        return wholesaleDao.getWholesaleWithProductLike(searchKeyword);
    }

    public LiveData<Wholesale> getWholesalePriceForProduct(long productId, double quantity) {
        return wholesaleDao.getWholesalePriceForProduct(productId, quantity);
    }



}
