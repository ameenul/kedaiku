package com.example.kedaiku.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.kedaiku.database.AppDatabase;
import com.example.kedaiku.entites.Wholesale;
import com.example.kedaiku.ifaceDao.WholesaleDao;

import java.util.List;

public class WholesaleViewModel extends AndroidViewModel {
    private WholesaleDao wholesaleDao;
    private LiveData<List<Wholesale>> allWholesales;

    public WholesaleViewModel(@NonNull Application application) {
        super(application);
        AppDatabase db = AppDatabase.getDatabase(application);
        wholesaleDao = db.wholesaleDao();
        allWholesales = wholesaleDao.getAllWholesales();
    }

    public LiveData<List<Wholesale>> getAllWholesales() {
        return allWholesales;
    }

    public void insert(Wholesale wholesale) {
        AppDatabase.databaseWriteExecutor.execute(() -> wholesaleDao.insert(wholesale));
    }

    public void update(Wholesale wholesale) {
        AppDatabase.databaseWriteExecutor.execute(() -> wholesaleDao.update(wholesale));
    }

    public void delete(Wholesale wholesale) {
        AppDatabase.databaseWriteExecutor.execute(() -> wholesaleDao.delete(wholesale));
    }
}
