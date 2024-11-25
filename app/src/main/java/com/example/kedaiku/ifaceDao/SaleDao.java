package com.example.kedaiku.ifaceDao;
import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.kedaiku.entites.Sale;

import java.util.List;

@Dao
public interface SaleDao {
    @Insert
    void insert(Sale sale);

    @Update
    void update(Sale sale);

    @Delete
    void delete(Sale sale);

    @Query("SELECT * FROM table_sale")
    LiveData<List<Sale>> getAllSales();
}

