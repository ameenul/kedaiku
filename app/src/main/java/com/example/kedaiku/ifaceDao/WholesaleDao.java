package com.example.kedaiku.ifaceDao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.kedaiku.entites.Wholesale;
import com.example.kedaiku.entites.WholesaleWithProduct;

import java.util.List;

@Dao
public interface WholesaleDao {
    @Insert
    void insert(Wholesale wholesale);

    @Update
    void update(Wholesale wholesale);

    @Delete
    void delete(Wholesale wholesale);

    @Query("SELECT * FROM table_wholesale")
    LiveData<List<Wholesale>> getAllWholesales();


    @Query("SELECT w.*, p.product_name, p.product_price, p.product_primary_price " +
            "FROM table_wholesale w " +
            "JOIN table_product p ON w.product_id = p.id " +
            "WHERE p.product_name LIKE '%' || :searchKeyword || '%'")
    LiveData<List<WholesaleWithProduct>> getWholesaleWithProductLike(String searchKeyword);


    @Query("SELECT * FROM table_wholesale WHERE product_id = :productId AND qty <= :quantity ORDER BY qty DESC LIMIT 1")
    LiveData<Wholesale> getWholesalePriceForProduct(long productId, double quantity);


}
