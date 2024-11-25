package com.example.kedaiku.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.example.kedaiku.ifaceDao.CashDao;
import com.example.kedaiku.database.AppDatabase;
import com.example.kedaiku.entites.Cash;

import java.util.List;

public class CashRepository {

    private CashDao cashDao;
    private LiveData<List<Cash>> allCash;

    public CashRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        cashDao = db.cashDao();
        allCash = cashDao.getAllCash();
    }

    public LiveData<List<Cash>> getAllCash() {
        return allCash;
    }

    public void insert(Cash cash) {
        cashDao.insert(cash);
    }

    public void update(Cash cash) {
        cashDao.update(cash);
    }

    public void delete(Cash cash) {
        cashDao.delete(cash.getId());
    }

    public void updateCashWithTransaction(Cash cash, int amount, String description) {
        cashDao.updateCashWithTransaction(cash.getId(), amount, description);
    }

    public void transferCash(Cash sourceCash, Cash targetCash, int amount, String description) {
        cashDao.transferCash(sourceCash.getId(), targetCash.getId(), amount, description);
    }
}
