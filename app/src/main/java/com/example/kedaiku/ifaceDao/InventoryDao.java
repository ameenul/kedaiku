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

    @Update
    void update(Inventory inventory);

    @Delete
    void delete(Inventory inventory);

    @Query("SELECT * FROM table_inventory WHERE stock_product_id = :stockProductId AND stock_date BETWEEN :startDate AND :endDate ORDER BY stock_date ASC")
    LiveData<List<Inventory>> getFilteredInventoryByStockProductId(long stockProductId, long startDate, long endDate);




}
