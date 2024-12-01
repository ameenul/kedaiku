package com.example.kedaiku.repository;

import android.app.Application;
import android.os.Looper;

import androidx.lifecycle.LiveData;

import com.example.kedaiku.database.AppDatabase;
import com.example.kedaiku.entites.Inventory;
import com.example.kedaiku.entites.Product;
import com.example.kedaiku.entites.ProductInventory;
import com.example.kedaiku.entites.Purchase;
import com.example.kedaiku.ifaceDao.CashDao;
import com.example.kedaiku.ifaceDao.InventoryDao;
import com.example.kedaiku.ifaceDao.ProductDao;
import com.example.kedaiku.ifaceDao.PurchaseDao;

import java.util.List;

public class ProductRepository {

    private final ProductDao productDao;
    private final InventoryDao inventoryDao;
    private final LiveData<List<Product>> allProducts;
    private final CashDao cashDao;
    private final PurchaseDao purchaseDao;

    public ProductRepository(Application application) {
        AppDatabase database = AppDatabase.getDatabase(application);
        productDao = database.productDao();
        inventoryDao = database.inventoryDao();
        allProducts = productDao.getAllProducts();
        cashDao = database.cashDao();
        purchaseDao = database.purchaseDao();
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

    public void processPurchase(
            Purchase purchase,
            double selectedPrice,
            Inventory inventory,
            Product product,
            long cashId,
            double purchaseAmount,
            String cashFlowDescription,
            OnTransactionCompleteListener listener
    ) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            try {
                productDao.processPurchase(
                        purchase,
                        selectedPrice,
                        inventory,
                        product,
                        cashId,
                        purchaseAmount,
                        cashFlowDescription,
                        inventoryDao,
                        cashDao,
                        purchaseDao
                );
                if (listener != null) {
                    new android.os.Handler(Looper.getMainLooper()).post(() -> listener.onSuccess("Pembelian berhasil."));
                }
            } catch (IllegalStateException e) {
                if (listener != null) {
                    new android.os.Handler(Looper.getMainLooper()).post(() -> listener.onFailure(e.getMessage()));
                }
            }
        });
    }


    public interface OnTransactionCompleteListener {
        void onSuccess(String message);

        void onFailure(String errorMessage);
    }

    public void deletePurchaseTransaction(Purchase purchase, OnTransactionCompleteListener listener) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            try {
                productDao.deletePurchaseTransaction(
                        purchase,
                        cashDao,
                        inventoryDao,
                        productDao,
                        purchaseDao
                );
                new android.os.Handler(Looper.getMainLooper()).post(() -> listener.onSuccess("Pembelian berhasil dibatalkan"));
            } catch (Exception e) {
                if (listener != null) {
                    new android.os.Handler(Looper.getMainLooper()).post(() -> listener.onFailure(e.getMessage()));
                }
            }
        });
    }


}
