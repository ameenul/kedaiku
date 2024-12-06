//package com.example.kedaiku.repository;
//
//
//import android.app.Application;
//
//import androidx.lifecycle.LiveData;
//
//import com.example.kedaiku.database.AppDatabase;
//import com.example.kedaiku.entites.Inventory;
//import com.example.kedaiku.entites.ProductInventory;
//import com.example.kedaiku.ifaceDao.InventoryDao;
//
//import java.util.List;
//import java.util.concurrent.ExecutorService;
//import java.util.concurrent.Executors;
//
//public class InventoryRepository {
//
//    private final InventoryDao inventoryDao;
//    private final LiveData<List<Inventory>> allInventories;
//    private final ExecutorService executorService;
//
//
//    public InventoryRepository(Application application) {
//        AppDatabase db = AppDatabase.getDatabase(application);
//        inventoryDao = db.inventoryDao();
//
//        allInventories = inventoryDao.getAllInventories();
//        executorService = Executors.newSingleThreadExecutor();
//    }
//
//    public LiveData<List<Inventory>> getAllInventories() {
//        return allInventories;
//    }
//
//    public void insert(Inventory inventory) {
//        executorService.execute(() -> inventoryDao.insert(inventory));
//    }
//
//    public void update(Inventory inventory) {
//        executorService.execute(() -> inventoryDao.update(inventory));
//    }
//
//    public void delete(Inventory inventory) {
//        executorService.execute(() -> inventoryDao.delete(inventory));
//    }
//
//    public LiveData<List<Inventory>> getInventoriesByStockProductId(long productId) {
//        return inventoryDao.getInventoriesByStockProductId(productId);
//    }
//
//    public LiveData<List<Inventory>> getInventoriesByDateRange(String startDate, String endDate) {
//        return inventoryDao.getInventoriesByDateRange(startDate, endDate);
//    }
//    public LiveData<List<Inventory>> getFilteredInventoryByStockProductId(long stockProductId, long startDate, long endDate) {
//        return inventoryDao.getFilteredInventoryByStockProductId(stockProductId, startDate, endDate);
//    }
//
//    public LiveData<List<ProductInventory>> getFilteredProductInventory(long startDate, long endDate) {
//        return inventoryDao.getFilteredProductInventory(startDate, endDate);
//    }
//
//
//
//
//}


package com.example.kedaiku.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.kedaiku.database.AppDatabase;
import com.example.kedaiku.entites.Inventory;
import com.example.kedaiku.entites.ProductInventory;
import com.example.kedaiku.ifaceDao.InventoryDao;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class InventoryRepository {

    private final InventoryDao inventoryDao;
    private final ExecutorService executorService;

    public InventoryRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        inventoryDao = db.inventoryDao();
        executorService = Executors.newFixedThreadPool(3);
    }


    // Menambahkan inventaris baru
    public void insert(Inventory inventory) {
        executorService.execute(() -> inventoryDao.insert(inventory));
    }

    // Memperbarui inventaris
    public void update(Inventory inventory) {
        executorService.execute(() -> inventoryDao.update(inventory));
    }

    // Menghapus inventaris
    public void delete(Inventory inventory) {
        executorService.execute(() -> inventoryDao.delete(inventory));
    }

    // Mendapatkan inventaris berdasarkan ID produk dan rentang tanggal
    public LiveData<List<Inventory>> getFilteredInventoryByStockProductId(long stockProductId, long startDate, long endDate) {
        return inventoryDao.getFilteredInventoryByStockProductId(stockProductId, startDate, endDate);
    }

//    // Mendapatkan semua inventaris
//    public LiveData<List<Inventory>> getAllInventories() {
//        return inventoryDao.getAllInventories();
//    }

//    // Mendapatkan inventaris berdasarkan produk
//    public LiveData<List<Inventory>> getInventoriesByStockProductId(long productId) {
//        return inventoryDao.getInventoriesByStockProductId(productId);
//    }

//    // Mendapatkan inventaris berdasarkan rentang tanggal
//    public LiveData<List<Inventory>> getInventoriesByDateRange(String startDate, String endDate) {
//        return inventoryDao.getInventoriesByDateRange(startDate, endDate);
//    }

//    public LiveData<List<Inventory>> getFilteredInventoryByDateRange(long startDate, long endDate) {
//        return inventoryDao.getFilteredInventoryByDateRange(startDate, endDate);
//    }








}
