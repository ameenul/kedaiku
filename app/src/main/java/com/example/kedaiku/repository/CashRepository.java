package com.example.kedaiku.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.example.kedaiku.ifaceDao.CashDao;
import com.example.kedaiku.database.AppDatabase;
import com.example.kedaiku.entites.Cash;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CashRepository {

    private final CashDao cashDao;
    private final LiveData<List<Cash>> allCash;

    // Executor for background tasks
    private final ExecutorService executorService;

    public CashRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        cashDao = db.cashDao();
        allCash = cashDao.getAllCash();

        // Initialize executor service with a single-thread executor
        executorService = Executors.newSingleThreadExecutor();
    }

    public LiveData<List<Cash>> getAllCash() {
        return allCash;
    }

    public void insert(Cash cash) {
        executorService.execute(() -> cashDao.insert(cash));
    }

    public void update(Cash cash) {
        executorService.execute(() -> cashDao.update(cash));
    }

    public void delete(Cash cash) {
        executorService.execute(() -> cashDao.delete(cash.getId()));
    }

    public void updateCashWithTransaction(Cash cash, double amount, String description) {
        executorService.execute(() -> cashDao.updateCashWithTransaction(cash.getId(), amount, description));
    }

    public void transferCash(Cash sourceCash, Cash targetCash, double amount, String description) {
        executorService.execute(() -> cashDao.transferCash(sourceCash.getId(), targetCash.getId(), amount, description));
    }

    public LiveData<Cash> getCashById(long cashId) {
        return cashDao.getCashById(cashId);
    }



}
