package com.example.kedaiku.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.example.kedaiku.entites.Customer;
import com.example.kedaiku.repository.CustomerRepository;

import java.util.List;

public class CustomerViewModel extends AndroidViewModel {

    private CustomerRepository repository;
    private LiveData<List<Customer>> allCustomers;

    private MutableLiveData<String> searchQuery = new MutableLiveData<>();

    private LiveData<List<Customer>> customers;

    public CustomerViewModel(@NonNull Application application) {
        super(application);
        repository = new CustomerRepository(application);
        allCustomers = repository.getAllCustomers();

        customers = Transformations.switchMap(searchQuery, query -> {
            if (query == null || query.trim().isEmpty()) {
                return allCustomers;
            } else {
                return repository.searchCustomers(query);
            }
        });
    }

    public LiveData<List<Customer>> getCustomers() {
        return customers;
    }

    public void setSearchQuery(String query) {
        searchQuery.setValue(query);
    }

    public void insert(Customer customer) {
        repository.insert(customer);
    }

    public void update(Customer customer) {
        repository.update(customer);
    }

    public void delete(Customer customer) {
        repository.delete(customer);
    }

    public LiveData<Customer> getCustomerById(int customerId) {
        return repository.getCustomerById(customerId);
    }

    public LiveData<List<Customer>> getCustomersWithoutGroup() {
        return repository.getCustomersWithoutGroup();
    }

    public LiveData<List<Customer>> getCustomersByGroupId(int groupId) {
        return repository.getCustomersByGroupId(groupId);
    }

    public void deleteGroupAndUpdateCustomers(int groupId) {
        repository.deleteGroupAndUpdateCustomers(groupId);
    }
}
