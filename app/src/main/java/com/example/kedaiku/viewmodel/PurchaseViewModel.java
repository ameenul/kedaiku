package com.example.kedaiku.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.kedaiku.database.AppDatabase;
import com.example.kedaiku.entites.Purchase;
import com.example.kedaiku.ifaceDao.PurchaseDao;

import java.util.List;

public class PurchaseViewModel extends AndroidViewModel {
    private PurchaseDao purchaseDao;
    private LiveData<List<Purchase>> allPurchases;

    public PurchaseViewModel(@NonNull Application application) {
        super(application);
        AppDatabase db = AppDatabase.getDatabase(application);
        purchaseDao = db.purchaseDao();
        allPurchases = purchaseDao.getAllPurchases();
    }

    public LiveData<List<Purchase>> getAllPurchases() {
        return allPurchases;
    }

    public void insert(Purchase purchase) {
        AppDatabase.databaseWriteExecutor.execute(() -> purchaseDao.insert(purchase));
    }

    public void update(Purchase purchase) {
        AppDatabase.databaseWriteExecutor.execute(() -> purchaseDao.update(purchase));
    }

    public void delete(Purchase purchase) {
        AppDatabase.databaseWriteExecutor.execute(() -> purchaseDao.delete(purchase));
    }
}
