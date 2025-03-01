package com.example.kedaiku.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.example.kedaiku.entites.Creditor;
import com.example.kedaiku.ifaceDao.CreditorDao;
import com.example.kedaiku.database.AppDatabase;
import com.example.kedaiku.entites.Creditor;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CreditorRepository {

    private CreditorDao creditorDao;

    private ExecutorService executorService;

    public CreditorRepository(Application application) {
        AppDatabase database = AppDatabase.getDatabase(application);
        creditorDao = database.creditorDao();

        executorService = Executors.newSingleThreadExecutor();
    }

    public void insert(Creditor creditor) {
        executorService.execute(() -> creditorDao.insert(creditor));
    }

    public void update(Creditor creditor) {
        executorService.execute(() -> creditorDao.update(creditor));
    }

    public void delete(Creditor creditor) {
        executorService.execute(() -> creditorDao.delete(creditor));
    }

    public LiveData<List<Creditor>> getAllCreditors() {
        return creditorDao.getAllCreditors();
    }

    public LiveData<List<Creditor>> searchCreditors(String searchQuery) {
        return creditorDao.searchCreditors(searchQuery);
    }

    public LiveData<Creditor> getCreditorById(long creditorId) {
        return creditorDao.getCreditorById(creditorId);
    }


}
