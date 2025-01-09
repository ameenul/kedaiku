package com.example.kedaiku.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.example.kedaiku.ifaceDao.SpecialPriceDao;
import com.example.kedaiku.database.AppDatabase;
import com.example.kedaiku.entites.SpecialPrice;
import com.example.kedaiku.entites.SpecialPriceWithProduct;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SpecialPriceRepository {

    private final SpecialPriceDao specialPriceDao;
    private final ExecutorService executorService;

    public SpecialPriceRepository(Application application) {
        AppDatabase database = AppDatabase.getDatabase(application);
        specialPriceDao = database.specialPriceDao();
        executorService = Executors.newSingleThreadExecutor();
    }

    // Insert
    public void insert(SpecialPrice specialPrice) {
        executorService.execute(() -> specialPriceDao.insert(specialPrice));
    }

    // Update
    public void update(SpecialPrice specialPrice) {
        executorService.execute(() -> specialPriceDao.update(specialPrice));
    }

    // Delete
    public void delete(SpecialPrice specialPrice) {
        executorService.execute(() -> specialPriceDao.delete(specialPrice));
    }

    // Get all special prices
    public LiveData<List<SpecialPrice>> getAllSpecialPrices() {
        return specialPriceDao.getAllSpecialPrices();
    }

    // Get special prices with product filtered by keyword
    public LiveData<List<SpecialPriceWithProduct>> getSpecialPriceWithProductLike(String searchKeyword) {
        return specialPriceDao.getSpecialPriceWithProductLike(searchKeyword);
    }

    public LiveData<SpecialPrice> getHighestSpecialPriceForProduct(long productId) {
        return specialPriceDao.getHighestSpecialPriceForProduct(productId);}


    public LiveData<SpecialPrice> getSpecialPriceByGroupIdAndProductId(long id, long productId) {
        return specialPriceDao.getSpecialPriceByGroupIdAndProductId(id, productId);
    }
}
