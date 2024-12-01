package com.example.kedaiku.viewmodel;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.kedaiku.entites.Cash;
import com.example.kedaiku.repository.CashRepository;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CashViewModel extends AndroidViewModel {

    private CashRepository cashRepository;
    private LiveData<List<Cash>> allCash;
    private ExecutorService executorService;

    public CashViewModel(Application application) {
        super(application);
        cashRepository = new CashRepository(application);
        allCash = cashRepository.getAllCash();
        executorService = Executors.newSingleThreadExecutor();
    }

    public LiveData<List<Cash>> getAllCash() {
        return allCash;
    }

    public void insert(Cash cash) {
        executorService.execute(() -> cashRepository.insert(cash));
    }

    public void update(Cash cash) {
        executorService.execute(() -> cashRepository.update(cash));
    }

    public void delete(Cash cash) {
        executorService.execute(() -> cashRepository.delete(cash));
    }

    public void updateCashWithTransaction(Cash cash, double amount, String description) {
        executorService.execute(() -> cashRepository.updateCashWithTransaction(cash, amount, description));
    }

    public void transferCash(Cash sourceCash, Cash targetCash, double amount, String description) {
        executorService.execute(() -> cashRepository.transferCash(sourceCash, targetCash, amount, description));
    }

    public LiveData<Cash> getCashById(long cashId) {
        return cashRepository.getCashById(cashId);
    }
}
