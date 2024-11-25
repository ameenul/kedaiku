package com.example.kedaiku.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.kedaiku.database.AppDatabase;
import com.example.kedaiku.entites.ProductSold;
import com.example.kedaiku.ifaceDao.ProductSoldDao;

import java.util.List;

public class ProductSoldViewModel extends AndroidViewModel {
    private ProductSoldDao productSoldDao;
    private LiveData<List<ProductSold>> allProductSold;

    public ProductSoldViewModel(@NonNull Application application) {
        super(application);
        AppDatabase db = AppDatabase.getDatabase(application);
        productSoldDao = db.productSoldDao();
        allProductSold = productSoldDao.getAllProductSold();
    }

    public LiveData<List<ProductSold>> getAllProductSold() {
        return allProductSold;
    }

    public void insert(ProductSold productSold) {
        AppDatabase.databaseWriteExecutor.execute(() -> productSoldDao.insert(productSold));
    }

    public void update(ProductSold productSold) {
        AppDatabase.databaseWriteExecutor.execute(() -> productSoldDao.update(productSold));
    }

    public void delete(ProductSold productSold) {
        AppDatabase.databaseWriteExecutor.execute(() -> productSoldDao.delete(productSold));
    }
}
