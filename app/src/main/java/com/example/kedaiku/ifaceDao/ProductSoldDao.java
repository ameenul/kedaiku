package com.example.kedaiku.ifaceDao;
import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.kedaiku.entites.ProductSold;

import java.util.List;

@Dao
public interface ProductSoldDao {
    @Insert
    void insert(ProductSold productSold);

    @Update
    void update(ProductSold productSold);

    @Delete
    void delete(ProductSold productSold);

    @Query("SELECT * FROM table_product_sold")
    LiveData<List<ProductSold>> getAllProductSold();
}
