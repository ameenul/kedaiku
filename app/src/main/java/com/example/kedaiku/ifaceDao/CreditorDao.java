package com.example.kedaiku.ifaceDao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.kedaiku.entites.Creditor;


import java.util.List;

@Dao
public interface CreditorDao {
    @Insert
    void insert(Creditor creditor);

    @Update
    void update(Creditor creditor);

    @Delete
    void delete(Creditor creditor);

    @Query("SELECT * FROM table_creditor")
    LiveData<List<Creditor>> getAllCreditors();
    @Query("SELECT * FROM table_creditor WHERE creditor_name LIKE '%' || :searchQuery || '%' OR creditor_phone LIKE '%' || :searchQuery || '%' ORDER BY creditor_name ASC")
    LiveData<List<Creditor>> searchCreditors(String searchQuery);
    @Query("SELECT * FROM table_creditor WHERE _id = :creditorId LIMIT 1")
    LiveData<Creditor> getCreditorById(long creditorId);

    @Query("SELECT creditor_name FROM table_creditor WHERE _id = :creditorId")
    String getCreditorNameById(long creditorId);
}
