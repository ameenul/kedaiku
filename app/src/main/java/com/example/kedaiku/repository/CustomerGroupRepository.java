package com.example.kedaiku.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.example.kedaiku.ifaceDao.CustomerGroupDao;
import com.example.kedaiku.database.AppDatabase;
import com.example.kedaiku.entites.CustomerGroup;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CustomerGroupRepository {

    private CustomerGroupDao groupDao;
    private LiveData<List<CustomerGroup>> allGroups;
    private ExecutorService executorService;

    public CustomerGroupRepository(Application application) {
        AppDatabase database = AppDatabase.getDatabase(application);
        groupDao = database.customerGroupDao();
        allGroups = groupDao.getAllGroups();
        executorService = Executors.newSingleThreadExecutor();
    }

    public void insert(CustomerGroup group) {
        executorService.execute(() -> groupDao.insert(group));
    }

    public void update(CustomerGroup group) {
        executorService.execute(() -> groupDao.update(group));
    }

    public void delete(CustomerGroup group) {
        executorService.execute(() -> groupDao.delete(group));
    }

    public LiveData<List<CustomerGroup>> getAllGroups() {
        return allGroups;
    }

    public LiveData<CustomerGroup> getGroupById(int groupId) {
        return groupDao.getGroupById(groupId);
    }
}
