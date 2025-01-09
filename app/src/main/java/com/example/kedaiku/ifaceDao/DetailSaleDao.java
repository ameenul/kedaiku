package com.example.kedaiku.ifaceDao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.kedaiku.entites.DetailSale;

import java.util.List;

@Dao
public interface DetailSaleDao {
    @Insert
    long insert(DetailSale detailSale);

    @Update
    void update(DetailSale detailSale);

    @Delete
    void delete(DetailSale detailSale);

    @Query("SELECT * FROM table_detail_sale")
    LiveData<List<DetailSale>> getAllDetailSales();

    @Query("DELETE FROM table_detail_sale WHERE id = :detailSaleId")
    void deleteDetailSaleById(long detailSaleId);



}
