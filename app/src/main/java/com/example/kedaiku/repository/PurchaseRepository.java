package com.example.kedaiku.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.example.kedaiku.entites.Purchase;
import com.example.kedaiku.entites.PurchaseWithProduct;
import com.example.kedaiku.ifaceDao.PurchaseDao;
import com.example.kedaiku.database.AppDatabase; // Pastikan Anda punya AppDatabase sebagai Singleton

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PurchaseRepository {
    private final PurchaseDao purchaseDao;
    private final ExecutorService executorService;

    public PurchaseRepository(Application application) {
        AppDatabase database = AppDatabase.getDatabase(application);
        purchaseDao = database.purchaseDao();
        executorService = Executors.newSingleThreadExecutor();
    }

    // Mengambil semua Purchase
    public LiveData<List<Purchase>> getAllPurchases() {
        return purchaseDao.getAllPurchases();
    }

    // Mengambil Purchase terfilter berdasarkan rentang tanggal
    public LiveData<List<Purchase>> getFilteredPurchases(long startDate, long endDate) {
        return purchaseDao.getFilteredPurchases(startDate, endDate);
    }

    // Mengambil Purchase terfilter dengan join product untuk mendapatkan product_name
    public LiveData<List<PurchaseWithProduct>> getFilteredPurchasesWithProductName(long startDate, long endDate) {
        return purchaseDao.getFilteredPurchasesWithProductName(startDate, endDate);
    }

    // Insert Purchase
    public void insert(Purchase purchase) {
        executorService.execute(() -> purchaseDao.insert(purchase));
    }

    // Insert Purchase dan dapatkan ID yang dihasilkan
    public void insertPurchaseAndGetId(Purchase purchase, OnInsertCompleteListener listener) {
        executorService.execute(() -> {
            long id = purchaseDao.insertPurchaseAndGetId(purchase);
            if (listener != null) {
                // Kembalikan ke main thread jika diperlukan (bisa gunakan Handler atau LiveData)
                listener.onInsertComplete(id);
            }
        });
    }

    // Update Purchase
    public void update(Purchase purchase) {
        executorService.execute(() -> purchaseDao.update(purchase));
    }

    // Delete Purchase
    public void delete(Purchase purchase) {
        executorService.execute(() -> purchaseDao.delete(purchase));
    }

    // Callback untuk insertPurchaseAndGetId agar bisa mengetahui ID setelah insert
    public interface OnInsertCompleteListener {
        void onInsertComplete(long id);
    }
}
