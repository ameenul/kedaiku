package com.example.kedaiku.ifaceDao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.kedaiku.entites.SpecialPrice;

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
}

