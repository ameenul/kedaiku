package com.example.kedaiku.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.kedaiku.database.AppDatabase;
import com.example.kedaiku.entites.Debt;
import com.example.kedaiku.ifaceDao.DebtDao;

import java.util.List;

public class DebtViewModel extends AndroidViewModel {
    private DebtDao debtDao;
    private LiveData<List<Debt>> allDebts;

    public DebtViewModel(@NonNull Application application) {
        super(application);
        AppDatabase db = AppDatabase.getDatabase(application);
        debtDao = db.debtDao();
        allDebts = debtDao.getAllDebts();
    }

    public LiveData<List<Debt>> getAllDebts() {
        return allDebts;
    }

    public void insert(Debt debt) {
        AppDatabase.databaseWriteExecutor.execute(() -> debtDao.insert(debt));
    }

    public void update(Debt debt) {
        AppDatabase.databaseWriteExecutor.execute(() -> debtDao.update(debt));
    }

    public void delete(Debt debt) {
        AppDatabase.databaseWriteExecutor.execute(() -> debtDao.delete(debt));
    }
}
