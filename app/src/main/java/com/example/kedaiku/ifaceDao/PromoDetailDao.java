package com.example.kedaiku.ifaceDao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.kedaiku.entites.PromoDetail;

import java.util.List;

@Dao
public interface PromoDetailDao {
    @Insert
    long insert(PromoDetail promoDetail);

    @Update
    void update(PromoDetail promoDetail);

    @Delete
    void delete(PromoDetail promoDetail);

    @Query("SELECT * FROM table_promo_detail")
    LiveData<List<PromoDetail>> getAllPromoDetails();

    @Query("DELETE FROM table_promo_detail WHERE _id = :promoDetailId")
    void deletePromoDetailById(long promoDetailId);

}

