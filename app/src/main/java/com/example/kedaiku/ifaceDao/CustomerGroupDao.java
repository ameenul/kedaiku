package com.example.kedaiku.ifaceDao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import androidx.room.Delete;

import com.example.kedaiku.entites.CustomerGroup;

import java.util.List;

@Dao
public interface CustomerGroupDao {

    @Insert
    void insert(CustomerGroup group);

    @Update
    void update(CustomerGroup group);

    @Delete
    void delete(CustomerGroup group);

    @Query("SELECT * FROM table_customer_group ORDER BY name ASC")
    LiveData<List<CustomerGroup>> getAllGroups();

    @Query("SELECT * FROM table_customer_group WHERE id = :groupId")
    LiveData<CustomerGroup> getGroupById(int groupId);
}
