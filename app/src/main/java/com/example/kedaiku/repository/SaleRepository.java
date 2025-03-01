package com.example.kedaiku.repository;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.lifecycle.LiveData;

import com.example.kedaiku.UI.penjualan_menu.CartItem;
import com.example.kedaiku.database.AppDatabase;
import com.example.kedaiku.entites.Cash;
import com.example.kedaiku.entites.DetailSale;
import com.example.kedaiku.entites.Inventory;
import com.example.kedaiku.entites.ParsingHistory;
import com.example.kedaiku.entites.PromoDetail;
import com.example.kedaiku.entites.Sale;
import com.example.kedaiku.entites.SaleWithDetails;
import com.example.kedaiku.helper.FormatHelper;
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
    AppDatabase db;


    private final ExecutorService executorService;

    public SaleRepository(Application application) {
        db = AppDatabase.getDatabase(application);
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
    public LiveData<List<SaleWithDetails>> getFilteredSalesForPaymentType2WithSearch(
            long startDate, long endDate, String searchString, boolean filterByCustomer) {
        return saleDao.getFilteredSalesForPaymentType2WithSearch(startDate, endDate, searchString, filterByCustomer);
    }


// =========================== Implementasi payPiutang ===========================

    /**
     * Metode untuk melakukan pembayaran piutang dengan mengupdate sale_paid_history dan sale_paid secara atomik
     *
     * @param saleId        ID dari Sale yang ingin diperbarui
     * @param paymentAmount Jumlah pembayaran yang ingin ditambahkan
     * @param listener      Listener untuk menangani hasil transaksi
     */
    public void payPiutang(long saleId, double paymentAmount, OnTransactionCompleteListener listener) {
        executorService.execute(() -> {
            boolean isSuccess = false;
            Handler mainHandler = new Handler(Looper.getMainLooper());
            try {


                db.runInTransaction(() -> {
                    // 1. Ambil SaleWithDetails berdasarkan saleId
                    SaleWithDetails saleWithDetails = saleDao.getSaleWithDetailsById(saleId);
                    if (saleWithDetails == null) {
                        throw new IllegalArgumentException("Sale not found for ID: " + saleId);
                    }

                    Sale sale = saleWithDetails.getSale();
                    DetailSale detailSale = saleWithDetails.getDetailSale();

                    if (sale == null || detailSale == null) {
                        throw new IllegalArgumentException("Sale atau DetailSale tidak ditemukan untuk ID: " + saleId);
                    }

                    // 2. Parsing sale_paid_history menjadi List<ParsingHistory>
                    List<ParsingHistory> paidHistories = FormatHelper.parsePaidHistory(detailSale.getSale_paid_history());

                    // 3. Tambahkan entri pembayaran baru
                    ParsingHistory newHistory = new ParsingHistory(System.currentTimeMillis(), paymentAmount);
                    paidHistories.add(newHistory);

                    // 4. Konversi kembali List<ParsingHistory> ke JSON
                    String updatedPaidHistoryJson = FormatHelper.convertPaidHistoriesToJson(paidHistories);

                    // 5. Hitung total sale_paid berdasarkan paidHistories
                    double newSalePaid = 0.0;
                    for (ParsingHistory history : paidHistories) {
                        newSalePaid += history.getPaid();
                    }

                    // 6. Update sale_paid di tabel_sale
                    sale.setSale_paid(newSalePaid);
                    saleDao.updateSale(sale);

                    // 7. Update sale_paid_history di tabel_detail_sale
                    detailSale.setSale_paid_history(updatedPaidHistoryJson);
                    detailSaleDao.update(detailSale);

                    // 8. Masukkan entri baru ke tabel kas
                    String cashDescription = "Pembayaran piutang untuk penjualan id: " + saleId;
                    cashDao.updateCashWithTransaction(sale.getSale_cash_id(), paymentAmount, cashDescription);

                });

                // Jika transaksi berhasil
                isSuccess = true;
                boolean finalIsSuccess = isSuccess;
                mainHandler.post(() -> listener.onSuccess(finalIsSuccess));
            } catch (Exception e) {
                Log.e("SaleRepository", "payPiutang Transaction failed: " + e.getMessage());
                e.printStackTrace();
                // Jika terjadi kesalahan, kembalikan false
               // listener.onFailure(false);
                mainHandler.post(() -> listener.onFailure(false));
            }
        });
    }


}
