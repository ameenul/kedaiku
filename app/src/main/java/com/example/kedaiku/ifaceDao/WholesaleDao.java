package com.example.kedaiku.ifaceDao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.kedaiku.entites.Wholesale;

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
}
