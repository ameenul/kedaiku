package com.example.kedaiku.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.example.kedaiku.UI.penjualan_menu.CartItem;
import com.example.kedaiku.database.AppDatabase;
import com.example.kedaiku.entites.DetailSale;
import com.example.kedaiku.entites.Inventory;
import com.example.kedaiku.entites.PromoDetail;
import com.example.kedaiku.entites.Sale;
import com.example.kedaiku.entites.SaleWithDetails;
import com.example.kedaiku.ifaceDao.CashDao;
import com.example.kedaiku.ifaceDao.DetailSaleDao;
import com.example.kedaiku.ifaceDao.InventoryDao;
import com.example.kedaiku.ifaceDao.ProductDao;
import com.example.kedaiku.ifaceDao.PromoDetailDao;
import com.example.kedaiku.ifaceDao.SaleDao;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SaleRepository {
    private final SaleDao saleDao;
    private final DetailSaleDao detailSaleDao;
    private final PromoDetailDao promoDetailDao;
    private final ProductDao productDao;
    private final CashDao cashDao;
    private final InventoryDao inventoryDao;


    private final ExecutorService executorService;

    public SaleRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        saleDao = db.saleDao();
        detailSaleDao = db.detailSaleDao();
        promoDetailDao = db.promoDetailDao();
        productDao = db.productDao();
        cashDao = db.cashDao();
        inventoryDao = db.inventoryDao();

        executorService = Executors.newSingleThreadExecutor();
    }

    public LiveData<List<SaleWithDetails>> getSalesWithDetailsFiltered(long startDate, long endDate, String transactionName) {
        return saleDao.getSalesWithDetailsFiltered(startDate, endDate, "%" + transactionName + "%");
    }

    public void insert(Sale sale) {
        executorService.execute(() -> saleDao.insert(sale));
    }

    public void update(Sale sale) {
        executorService.execute(() -> saleDao.update(sale));
    }

    public void delete(Sale sale) {
        executorService.execute(() -> saleDao.delete(sale));
    }

    public LiveData<List<Sale>> getAllSales() {
        return saleDao.getAllSales();
    }

    // Metode untuk menyelesaikan transaksi penjualan
    public void completeTransaction(Sale sale, DetailSale detailSale, PromoDetail promoDetail, List<CartItem> cartItems, OnTransactionCompleteListener listener) {
        executorService.execute(() -> {
            //saleDao.completeTransaction(sale, detailSale, promoDetail, cartItems, detailSaleDao, promoDetailDao, productDao, inventoryDao, cashDao);
            boolean isSuccess = false;
            try {
                isSuccess = saleDao.completeTransaction(sale, detailSale, promoDetail, cartItems, detailSaleDao, promoDetailDao, productDao, inventoryDao, cashDao);
                if (isSuccess) {
                    listener.onSuccess(isSuccess);
                } else {
                    listener.onFailure(isSuccess);
                }
            } catch (Exception e) {
                e.printStackTrace();
                listener.onFailure(false); // Jika terjadi kesalahan, panggil onFailure
            }

        });
    }


    public void deleteSaleTransactionAsync(long saleId, OnTransactionCompleteListener listener) {
        new Thread(() -> {
            try {
                boolean result = saleDao.deleteSaleTransaction(
                        saleId,
                        productDao,
                        inventoryDao,
                        cashDao,
                        detailSaleDao,
                        promoDetailDao
                );
                if (result) {
                    listener.onSuccess(true);
                } else {
                    listener.onFailure(false);
                }
            } catch (Exception e) {
                e.printStackTrace();
                listener.onFailure(false);
            }
        }).start();
    }

    // Deklarasi interface di dalam repository
    public interface OnTransactionCompleteListener {
        void onSuccess(boolean status);
        void onFailure(boolean status);
    }

    public void updateSaleTransactionAsync(Sale newSale,
                                           String newDetailSale,
                                           String newPromoDetail,
                                           List<CartItem> newCartItems,
                                           OnTransactionCompleteListener listener) {
        executorService.execute(() -> {
            boolean isSuccess = false;
            try {
                // Memanggil metode di SaleDao (harus Anda definisikan)
                isSuccess = saleDao.updateSaleTransaction(
                        newSale,
                        newDetailSale,
                        newPromoDetail,
                        newCartItems,
                        detailSaleDao,
                        promoDetailDao,
                        productDao,
                        inventoryDao,
                        cashDao
                );
                if (isSuccess) {
                    listener.onSuccess(true);
                } else {
                    listener.onFailure(false);
                }
            } catch (Exception e) {
                e.printStackTrace();
                listener.onFailure(false);
            }
        });
    }


    public SaleWithDetails getSaleWithDetailsByIdSync(long saleId) {
        return saleDao.getSaleWithDetailsById(saleId);
    }
    public LiveData<SaleWithDetails> getSaleWithDetailsByIdLive(long saleId) {
        return saleDao.getSaleWithDetailsByIdLive(saleId);
    }



}
