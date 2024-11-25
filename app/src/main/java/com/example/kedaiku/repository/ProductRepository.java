package com.example.kedaiku.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.example.kedaiku.database.AppDatabase;
import com.example.kedaiku.entites.Inventory;
import com.example.kedaiku.entites.Product;
import com.example.kedaiku.entites.ProductInventory;
import com.example.kedaiku.ifaceDao.InventoryDao;
import com.example.kedaiku.ifaceDao.ProductDao;

import java.util.List;

public class ProductRepository {

    private final ProductDao productDao;
    private final InventoryDao inventoryDao;
    private final LiveData<List<Product>> allProducts;

    public ProductRepository(Application application) {
        AppDatabase database = AppDatabase.getDatabase(application);
        productDao = database.productDao();
        inventoryDao = database.inventoryDao();
        allProducts = productDao.getAllProducts();
    }

    public LiveData<List<Product>> getAllProducts() {
        return allProducts;
    }

    public LiveData<List<Product>> searchProducts(String query) {
        return productDao.searchProducts(query);
    }

    public void insert(Product product) {
        AppDatabase.databaseWriteExecutor.execute(() -> productDao.insert(product));
    }

    public void updateProductWithInventory(Product product, double oldQty) {
        AppDatabase.databaseWriteExecutor.execute(() -> productDao.updateProductWithInventory(product, oldQty, inventoryDao));
    }

    public void updateProductWithInventory(Product product, double oldQty,String stock_note) {
        AppDatabase.databaseWriteExecutor.execute(() -> productDao.updateProductWithInventory(product, oldQty,stock_note ,inventoryDao));
    }

    public void delete(Product product) {
        AppDatabase.databaseWriteExecutor.execute(() -> productDao.delete(product));
    }

    public LiveData<Product> getProductById(long productId) {
        return productDao.getProductById(productId);
    }

    public void update(Product product) {
        AppDatabase.databaseWriteExecutor.execute(() -> productDao.update(product));
    }

    public void insertProductWithInventory(Product product, Inventory inventory) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            productDao.insertProductWithInventory(product, inventory, inventoryDao);
        });
    }



    public LiveData<List<ProductInventory>> getFilteredProductInventory(long startDate, long endDate) {
        return productDao.getFilteredProductInventory(startDate, endDate);
    }

}
