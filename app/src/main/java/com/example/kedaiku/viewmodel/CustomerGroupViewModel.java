package com.example.kedaiku.viewmodel;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.kedaiku.entites.CustomerGroup;
import com.example.kedaiku.repository.CustomerGroupRepository;

import java.util.List;

public class CustomerGroupViewModel extends AndroidViewModel {

    private CustomerGroupRepository repository;
    private LiveData<List<CustomerGroup>> allGroups;

    public CustomerGroupViewModel(Application application) {
        super(application);
        repository = new CustomerGroupRepository(application);
        allGroups = repository.getAllGroups();
    }

    public void insert(CustomerGroup group) {
        repository.insert(group);
    }

    public void update(CustomerGroup group) {
        repository.update(group);
    }

    public void delete(CustomerGroup group) {
        repository.delete(group);
    }

    public LiveData<List<CustomerGroup>> getAllGroups() {
        return allGroups;
    }

    public LiveData<CustomerGroup> getGroupById(int groupId) {
        return repository.getGroupById(groupId);
    }
}
