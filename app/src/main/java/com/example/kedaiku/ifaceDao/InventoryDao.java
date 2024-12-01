package com.example.kedaiku.ifaceDao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.kedaiku.entites.Inventory;
import com.example.kedaiku.entites.ProductInventory;

import java.util.List;

@Dao
public interface InventoryDao {

    @Insert
    void insert(Inventory inventory);

    @Query("SELECT * FROM table_inventory WHERE stock_product_id = :productId ORDER BY stock_date DESC")
    LiveData<List<Inventory>> getInventoryForProduct(long productId);



    @Update
    void update(Inventory inventory);

    @Delete
    void delete(Inventory inventory);

    @Query("SELECT * FROM table_inventory ORDER BY stock_date DESC")
    LiveData<List<Inventory>> getAllInventories();

    @Query("SELECT * FROM table_inventory WHERE stock_product_id = :productId ORDER BY stock_date DESC")
    LiveData<List<Inventory>> getInventoriesByStockProductId(long productId);

    @Query("SELECT * FROM table_inventory WHERE stock_date BETWEEN :startDate AND :endDate ORDER BY stock_date DESC")
    LiveData<List<Inventory>> getInventoriesByDateRange(String startDate, String endDate);


    @Query("SELECT * FROM table_inventory WHERE stock_product_id = :stockProductId AND stock_date BETWEEN :startDate AND :endDate ORDER BY stock_date ASC")
    LiveData<List<Inventory>> getFilteredInventoryByStockProductId(long stockProductId, long startDate, long endDate);

    @Query("SELECT * FROM table_inventory WHERE stock_date BETWEEN :startDate AND :endDate ORDER BY stock_date ASC")
    LiveData<List<Inventory>> getFilteredInventoryByDateRange(long startDate, long endDate);


//    @Query("SELECT * FROM table_inventory WHERE stock_date BETWEEN :startDate AND :endDate ORDER BY stock_date ASC")
//    List<Inventory> getFilteredInventoryByDateRangeSync(long startDate, long endDate);
//    // Metode sinkron untuk mendapatkan inventaris berdasarkan stock product ID dan rentang tanggal
//    @Query("SELECT * FROM table_inventory WHERE stock_product_id = :stockProductId AND stock_date BETWEEN :startDate AND :endDate ORDER BY stock_date ASC")
//    List<Inventory> getFilteredInventoryByStockProductIdSync(long stockProductId, long startDate, long endDate);





//
//    @Query("SELECT p.id AS productId, " +
//            "p.product_name AS productName, " +
//            "SUM(i.stock_in) AS stockIn, " +
//            "SUM(i.stock_out) AS stockOut, " +
//            "(SELECT stock_balance FROM table_inventory WHERE stock_product_id = i.stock_product_id ORDER BY stock_date DESC LIMIT 1) AS stockBalance " +
//            "FROM table_inventory i " +
//            "JOIN table_product p ON i.stock_product_id = p.id " +
//            "WHERE i.stock_date BETWEEN :startDate AND :endDate " +
//            "GROUP BY i.stock_product_id, p.product_name, p.id")
//    LiveData<List<ProductInventory>> getFilteredProductInventory(long startDate, long endDate);



//    @Query("SELECT p.id AS productId, " +
//            "p.product_name AS productName, " +
//            "SUM(i.stock_in) AS stockIn, " +
//            "SUM(i.stock_out) AS stockOut, " +
//            "(SELECT stock_balance FROM table_inventory WHERE stock_product_id = i.stock_product_id ORDER BY stock_date DESC LIMIT 1) AS stockBalance " +
//            "FROM table_inventory i " +
//            "JOIN table_product p ON i.stock_product_id = p.id " +
//            "GROUP BY i.stock_product_id, p.id, p.product_name")
//    LiveData<List<ProductInventory>> getAllProductInventory();






}
