package com.example.kedaiku.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.kedaiku.database.AppDatabase;
import com.example.kedaiku.entites.SpecialPrice;
import com.example.kedaiku.ifaceDao.SpecialPriceDao;

import java.util.List;

public class SpecialPriceViewModel extends AndroidViewModel {
    private SpecialPriceDao specialPriceDao;
    private LiveData<List<SpecialPrice>> allSpecialPrices;

    public SpecialPriceViewModel(@NonNull Application application) {
        super(application);
        AppDatabase db = AppDatabase.getDatabase(application);
        specialPriceDao = db.specialPriceDao();
        allSpecialPrices = specialPriceDao.getAllSpecialPrices();
    }

    public LiveData<List<SpecialPrice>> getAllSpecialPrices() {
        return allSpecialPrices;
    }

    public void insert(SpecialPrice specialPrice) {
        AppDatabase.databaseWriteExecutor.execute(() -> specialPriceDao.insert(specialPrice));
    }

    public void update(SpecialPrice specialPrice) {
        AppDatabase.databaseWriteExecutor.execute(() -> specialPriceDao.update(specialPrice));
    }

    public void delete(SpecialPrice specialPrice) {
        AppDatabase.databaseWriteExecutor.execute(() -> specialPriceDao.delete(specialPrice));
    }
}
