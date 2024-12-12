package com.example.kedaiku.ifaceDao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import com.example.kedaiku.entites.SpecialPrice;
import com.example.kedaiku.entites.SpecialPriceWithProduct;

import java.util.List;

@Dao
public interface SpecialPriceDao {
    @Insert
    void insert(SpecialPrice specialPrice);

    @Update
    void update(SpecialPrice specialPrice);

    @Delete
    void delete(SpecialPrice specialPrice);

    @Query("SELECT * FROM table_special_price")
    LiveData<List<SpecialPrice>> getAllSpecialPrices();

    @Transaction
    @Query("SELECT * FROM table_special_price sp " +
            "JOIN table_product p ON sp.product_id = p.id " +
            "WHERE p.product_name LIKE '%' || :searchKeyword || '%'")
    LiveData<List<SpecialPriceWithProduct>> getSpecialPriceWithProductLike(String searchKeyword);


}

