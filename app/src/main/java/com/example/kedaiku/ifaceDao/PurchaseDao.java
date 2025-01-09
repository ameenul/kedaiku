package com.example.kedaiku.ifaceDao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.kedaiku.entites.Purchase;
import com.example.kedaiku.entites.PurchaseWithProduct;

import java.util.List;

@Dao
public interface PurchaseDao {
    @Insert
    void insert(Purchase purchase);
    @Insert
    long insertPurchaseAndGetId(Purchase purchase);
    @Update
    void update(Purchase purchase);

    @Delete
    void delete(Purchase purchase);

    @Query("SELECT * FROM table_purchase")
    LiveData<List<Purchase>> getAllPurchases();

    @Query("SELECT * FROM table_purchase WHERE date BETWEEN :startDate AND :endDate")
    LiveData<List<Purchase>> getFilteredPurchases(long startDate, long endDate);

//    @Query("SELECT " +
//            "p._id, " +
//            "p.date, " +
//            "p.product_id, " +
//            "p.cash_id, " +
//            "p.purchase_detail, " +
//            "pr.product_name " +
//            "FROM table_purchase p " +
//            "JOIN table_product pr ON p.product_id = pr.id " +
//            "WHERE p.date BETWEEN :startDate AND :endDate")
//    LiveData<List<PurchaseWithProduct>> getFilteredPurchasesWithProductName(long startDate, long endDate);

    @Query("SELECT " +
            "p._id, " +
            "p.date, " +
            "p.product_id, " +
            "p.cash_id, " +
            "p.purchase_detail, " +
            "IFNULL(pr.product_name, 'Produk Terhapus') AS product_name " +
            "FROM table_purchase p " +
            "LEFT JOIN table_product pr ON p.product_id = pr.id " +
            "WHERE p.date BETWEEN :startDate AND :endDate")
    LiveData<List<PurchaseWithProduct>> getFilteredPurchasesWithProductName(long startDate, long endDate);


}

