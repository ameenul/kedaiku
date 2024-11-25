package com.example.kedaiku.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.example.kedaiku.ifaceDao.CustomerDao;
import com.example.kedaiku.database.AppDatabase;
import com.example.kedaiku.entites.Customer;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CustomerRepository {

    private CustomerDao customerDao;
    private LiveData<List<Customer>> allCustomers;
    private ExecutorService executorService;

    public CustomerRepository(Application application) {
        AppDatabase database = AppDatabase.getDatabase(application);
        customerDao = database.customerDao();
        allCustomers = customerDao.getAllCustomers();
        executorService = Executors.newSingleThreadExecutor();
    }

    public void insert(Customer customer) {
        executorService.execute(() -> customerDao.insert(customer));
    }

    public void update(Customer customer) {
        executorService.execute(() -> customerDao.update(customer));
    }

    public void delete(Customer customer) {
        executorService.execute(() -> customerDao.delete(customer));
    }

    public LiveData<List<Customer>> getAllCustomers() {
        return allCustomers;
    }

    public LiveData<List<Customer>> searchCustomers(String searchQuery) {
        return customerDao.searchCustomers(searchQuery);
    }

    public LiveData<List<Customer>> getCustomersByGroupId(int groupId) {
        return customerDao.getCustomersByGroupId(groupId);
    }

    public LiveData<List<Customer>> getCustomersWithoutGroup() {
        return customerDao.getCustomersWithoutGroup();
    }

    public LiveData<Customer> getCustomerById(int customerId) {
        return customerDao.getCustomerById(customerId);
    }

    public void deleteGroupAndUpdateCustomers(int groupId) {
        executorService.execute(() -> {
            customerDao.deleteGroupAndUpdateCustomers(groupId);
        });
    }

}
