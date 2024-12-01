package com.example.kedaiku.ifaceDao;


import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;
import androidx.room.Delete;

import com.example.kedaiku.entites.Customer;

import java.util.List;

@Dao
public interface CustomerDao {

    @Insert
    void insert(Customer customer);

    @Update
    void update(Customer customer);

    @Delete
    void delete(Customer customer);

    @Query("SELECT * FROM table_customer ORDER BY customer_name ASC")
    LiveData<List<Customer>> getAllCustomers();

    @Query("SELECT * FROM table_customer WHERE customer_name LIKE '%' || :searchQuery || '%' OR customer_phone LIKE '%' || :searchQuery || '%' ORDER BY customer_name ASC")
    LiveData<List<Customer>> searchCustomers(String searchQuery);

    @Query("SELECT * FROM table_customer WHERE customer_group_id = :groupId ORDER BY customer_name ASC")
    LiveData<List<Customer>> getCustomersByGroupId(long groupId);

    @Query("SELECT * FROM table_customer WHERE customer_group_id = 0 ORDER BY customer_name ASC")
    LiveData<List<Customer>> getCustomersWithoutGroup();

    @Query("SELECT * FROM table_customer WHERE id = :customerId LIMIT 1")
    LiveData<Customer> getCustomerById(long customerId);
    @Update
    void updateCustomers(List<Customer> customers);

    @Transaction
    default void deleteGroupAndUpdateCustomers(long groupId) {
        // Perbarui customer_group_id menjadi 0 untuk anggota grup
        List<Customer> customers = getCustomersByGroupIdSync(groupId);
        for (Customer customer : customers) {
            customer.setCustomer_group_id(0);
        }
        updateCustomers(customers);

        // Hapus grup
        deleteGroupById(groupId);
    }

    @Query("SELECT * FROM table_customer WHERE customer_group_id = :groupId")
    List<Customer> getCustomersByGroupIdSync(long groupId);

    @Query("DELETE FROM table_customer_group WHERE id = :groupId")
    void deleteGroupById(long groupId);


}
