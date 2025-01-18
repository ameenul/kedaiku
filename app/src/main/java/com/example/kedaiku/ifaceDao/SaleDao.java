package com.example.kedaiku.ifaceDao;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import androidx.room.Transaction;

import com.example.kedaiku.UI.penjualan_menu.CartItem;
import com.example.kedaiku.entites.CashFlow;
import com.example.kedaiku.entites.DetailSale;
import com.example.kedaiku.entites.Inventory;
import com.example.kedaiku.entites.Product;
import com.example.kedaiku.entites.PromoDetail;
import com.example.kedaiku.entites.Sale;
import com.example.kedaiku.entites.SaleWithDetails;
import com.example.kedaiku.helper.FormatHelper;

import java.util.List;

@Dao
public interface SaleDao {
    @Insert
    long insert(Sale sale);

    @Update
    void update(Sale sale);

    @Delete
    void delete(Sale sale);

    @Query("SELECT * FROM table_sale")
    LiveData<List<Sale>> getAllSales();

    @Transaction
    @Query("SELECT * FROM table_sale WHERE sale_date >= :startDate AND sale_date <= :endDate AND sale_transaction_name LIKE '%' || :transactionName || '%'")
    LiveData<List<SaleWithDetails>> getSalesWithDetailsFiltered(long startDate, long endDate, String transactionName);

    @Transaction
    @Query("""
    SELECT table_sale.* FROM table_sale
    INNER JOIN table_customer ON table_sale.sale_customer_id = table_customer.id
    WHERE sale_date >= :startDate 
      AND sale_date <= :endDate 
      AND sale_payment_type = 2
      AND (
          (:filterByCustomer = 0 AND sale_transaction_name LIKE '%' || :searchString || '%')
          OR 
          (:filterByCustomer = 1 AND customer_name LIKE '%' || :searchString || '%')
      )
""")
    LiveData<List<SaleWithDetails>> getFilteredSalesForPaymentType2WithSearch(
            long startDate,
            long endDate,
            String searchString,
            boolean filterByCustomer
    );


    @Transaction
    default boolean completeTransaction(Sale sale, DetailSale detailSales,
                                        PromoDetail promoDetail, List<CartItem> cartItems,
                                        DetailSaleDao detailSaleDao, PromoDetailDao promoDetailDao,
                                        ProductDao productDao, InventoryDao inventoryDao, CashDao cashDao) {
        try {
            // Set sale_detail_id in Sale and insert each detail sale
            long detailSaleId = detailSaleDao.insert(detailSales); //hps
            long promoDetailId = promoDetailDao.insert(promoDetail); //hps
            sale.setSale_detail_id(detailSaleId);
            sale.setPromo_id(promoDetailId);
            long saleId = insert(sale);

            // Loop through each product to update stock and insert inventory
            for (CartItem cartItem : cartItems) {
                long productId = cartItem.getProductId();
                double quantity = cartItem.getQuantity();
                Product p = productDao.getProductByIdSync(productId);
                double oldQty = p.getProduct_qty(); //2
                double newQty = oldQty - quantity; //1
                p.setProduct_qty(newQty);

                // Update Product Stock
                productDao.updateProductWithInventory(p, oldQty, "Penjualan dengan id : " + saleId, inventoryDao);
            }
            double cashIn =0;
            if(sale.getSale_total()<sale.getSale_paid())
            {
                cashIn = sale.getSale_total();
            }
            else {
                cashIn = sale.getSale_paid();

            }

            // Update cash transactions
            cashDao.updateCashWithTransaction(sale.getSale_cash_id(), -sale.getSale_ship(), "Ongkir Penjualan dengan id : " + saleId);
            cashDao.updateCashWithTransaction(sale.getSale_cash_id(), cashIn, "Pembayaran Penjualan dengan id : " + saleId);

            // Jika semua operasi berhasil, kembalikan true
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            // Jika terjadi kesalahan, kembalikan false
            return false;
        }
    }


    // Contoh query untuk mengambil SaleWithDetails berdasar saleId
    @Transaction
    @Query("SELECT * FROM table_sale WHERE id = :saleId LIMIT 1")
    SaleWithDetails getSaleWithDetailsById(long saleId);


    @Transaction
    @Query("SELECT * FROM table_sale WHERE id = :saleId LIMIT 1")
    LiveData<SaleWithDetails> getSaleWithDetailsByIdLive(long saleId);



    @Transaction
    default boolean deleteSaleTransaction(long saleId,
                                          ProductDao productDao,
                                          InventoryDao inventoryDao,
                                          CashDao cashDao,
                                          DetailSaleDao detailSaleDao,
                                          PromoDetailDao promoDetailDao) {
        try {
            // 1. Ambil SaleWithDetails
            SaleWithDetails saleWithDetails = getSaleWithDetailsById(saleId);
            if (saleWithDetails == null) {
                return false; // Sale tidak ditemukan
            }

            // 2. Dapatkan entitas yang diperlukan
            Sale sale = saleWithDetails.getSale();
            DetailSale detailSale = saleWithDetails.getDetailSale();
            PromoDetail promoDetail = saleWithDetails.getPromo();
            // ... customer, cash, dll., jika dibutuhkan.

            // 3. Revert perubahan kas (misalnya penjualan yang sudah dibayar dikembalikan)

            double cashOut =0;
            if(sale.getSale_total()<sale.getSale_paid())
            {
                cashOut = sale.getSale_total();
            }
            else {
                cashOut = sale.getSale_paid();

            }

            if(cashDao.getCashByIdSync(sale.getSale_cash_id())!=null){
                cashDao.updateCashWithTransaction(
                        sale.getSale_cash_id(),
                        -cashOut,
                        "Pembatalan penjualan (id: " + sale.getId() + ")"
                );
                cashDao.updateCashWithTransaction(
                        sale.getSale_cash_id(),
                        sale.getSale_ship(),
                        "Pengembalian ongkir (id: " + sale.getId() + ")"
                );

            }


            // 4. Kembalikan stok (ambil item dari detailSale.getSale_detail() atau sesuai struktur data Anda)
            //    Misalnya, jika detailSale.getSale_detail() berisi JSON item² penjualan:
            //    parseCartItemsFromJson() adalah metode kustom Anda sendiri.
            List<CartItem> cartItems = FormatHelper.parseCartItemsFromDetailSale(detailSale.getSale_detail());

            for (CartItem cartItem : cartItems) {
                long productId = cartItem.getProductId();
                double qty = cartItem.getQuantity();

                Product product = productDao.getProductByIdSync(productId);

                if(product!=null) {
                    double oldQty = product.getProduct_qty();
                    double newQty = oldQty + qty;

                    product.setProduct_qty(newQty);
                    productDao.updateProductWithInventory(
                            product,
                            oldQty,
                            "Pembatalan penjualan (id: " + sale.getId() + ")",
                            inventoryDao
                    );
                }
            }

            // 5. Hapus detailSale & promoDetail dari DB (bisa dengan @Delete atau query by ID)
            detailSaleDao.deleteDetailSaleById(detailSale.getId());
            promoDetailDao.deletePromoDetailById(promoDetail.getId());

            // 6. Terakhir, hapus Sale itu sendiri
            delete(sale);

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("error hapus",e.getMessage());
            return false;
        }
    }


    @Transaction
    default boolean updateSaleTransaction(Sale newSale,
                                          String newDetailJson,      // isi baru untuk detailSale
                                          String newPromoJson,       // isi baru untuk promoDetail
                                          List<CartItem> newCartItems,
                                          DetailSaleDao detailSaleDao,
                                          PromoDetailDao promoDetailDao,
                                          ProductDao productDao,
                                          InventoryDao inventoryDao,
                                          CashDao cashDao) {
        try {
            // 1. Ambil data lama
            long saleId = newSale.getId();
            SaleWithDetails oldData = getSaleWithDetailsById(saleId);
            if (oldData == null) return false;

            Sale oldSale = oldData.getSale();
            DetailSale oldDetailSale = oldData.getDetailSale();
            PromoDetail oldPromoDetail = oldData.getPromo();

            // 2. Revert efek penjualan lama (stok, kas)
            //    Kas:

            double cashOut =0;
            if(oldSale.getSale_total()<oldSale.getSale_paid())
            {
                cashOut = oldSale.getSale_total();
            }
            else {
                cashOut = oldSale.getSale_paid();

            }

            cashDao.updateCashWithTransaction(
                    oldSale.getSale_cash_id(),
                    -cashOut,
                    "Revert penjualan lama (ID: " + saleId + ")"
            );
            cashDao.updateCashWithTransaction(
                    oldSale.getSale_cash_id(),
                    oldSale.getSale_ship(),
                    "Revert ongkir lama (ID: " + saleId + ")"
            );
            //    Stok:
            List<CartItem> oldCartItems = FormatHelper.parseCartItemsFromDetailSale(oldDetailSale.getSale_detail());
            for (CartItem oldItem : oldCartItems) {
                Product product = productDao.getProductByIdSync(oldItem.getProductId());
                double oldQty = product.getProduct_qty();
                double revertedQty = oldQty + oldItem.getQuantity();
                product.setProduct_qty(revertedQty);

                productDao.updateProductWithInventory(
                        product,
                        oldQty,
                        "Revert stok penjualan lama (ID: " + saleId + ")",
                        inventoryDao
                );
            }

            // 3. Update DetailSale dan PromoDetail (tanpa delete)
            //    misal, isi kolom JSON barunya:
            oldDetailSale.setSale_detail(newDetailJson);
            oldPromoDetail.setDetail(newPromoJson);

            //    Panggil update:
            detailSaleDao.update(oldDetailSale);
            promoDetailDao.update(oldPromoDetail);

            // 4. Terapkan efek penjualan baru
            //    Stok:
            for (CartItem newItem : newCartItems) {
                Product product = productDao.getProductByIdSync(newItem.getProductId());
                double oldQty = product.getProduct_qty();
                double updatedQty = oldQty - newItem.getQuantity();
                product.setProduct_qty(updatedQty);

                productDao.updateProductWithInventory(
                        product,
                        oldQty,
                        "Update stok penjualan (ID: " + saleId + ")",
                        inventoryDao
                );
            }
            //    Kas:

            double cashIn =0;
            if(newSale.getSale_total()<newSale.getSale_paid())
            {
                cashIn = newSale.getSale_total();
            }
            else {
                cashIn = newSale.getSale_paid();

            }
            cashDao.updateCashWithTransaction(
                    newSale.getSale_cash_id(),
                    -newSale.getSale_ship(),
                    "Ongkir penjualan (Update) (ID: " + saleId + ")"
            );
            cashDao.updateCashWithTransaction(
                    newSale.getSale_cash_id(),
                    cashIn,
                    "Pembayaran penjualan (Update) (ID: " + saleId + ")"
            );

            // 5. Update Sale (baris utama)
            update(newSale);

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }




}
