package com.example.kedaiku.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.kedaiku.database.AppDatabase;
import com.example.kedaiku.entites.Creditor;
import com.example.kedaiku.ifaceDao.CreditorDao;

import java.util.List;

public class CreditorViewModel extends AndroidViewModel {
    private CreditorDao creditorDao;
    private LiveData<List<Creditor>> allCreditors;

    public CreditorViewModel(@NonNull Application application) {
        super(application);
        AppDatabase db = AppDatabase.getDatabase(application);
        creditorDao = db.creditorDao();
        allCreditors = creditorDao.getAllCreditors();
    }

    public LiveData<List<Creditor>> getAllCreditors() {
        return allCreditors;
    }

    public void insert(Creditor creditor) {
        AppDatabase.databaseWriteExecutor.execute(() -> creditorDao.insert(creditor));
    }

    public void update(Creditor creditor) {
        AppDatabase.databaseWriteExecutor.execute(() -> creditorDao.update(creditor));
    }

    public void delete(Creditor creditor) {
        AppDatabase.databaseWriteExecutor.execute(() -> creditorDao.delete(creditor));
    }
}
