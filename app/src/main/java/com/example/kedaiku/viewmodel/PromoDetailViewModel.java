package com.example.kedaiku.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.kedaiku.database.AppDatabase;
import com.example.kedaiku.entites.PromoDetail;
import com.example.kedaiku.ifaceDao.PromoDetailDao;

import java.util.List;

public class PromoDetailViewModel extends AndroidViewModel {
    private PromoDetailDao promoDetailDao;
    private LiveData<List<PromoDetail>> allPromoDetails;

    public PromoDetailViewModel(@NonNull Application application) {
        super(application);
        AppDatabase db = AppDatabase.getDatabase(application);
        promoDetailDao = db.promoDetailDao();
        allPromoDetails = promoDetailDao.getAllPromoDetails();
    }

    public LiveData<List<PromoDetail>> getAllPromoDetails() {
        return allPromoDetails;
    }

    public void insert(PromoDetail promoDetail) {
        AppDatabase.databaseWriteExecutor.execute(() -> promoDetailDao.insert(promoDetail));
    }

    public void update(PromoDetail promoDetail) {
        AppDatabase.databaseWriteExecutor.execute(() -> promoDetailDao.update(promoDetail));
    }

    public void delete(PromoDetail promoDetail) {
        AppDatabase.databaseWriteExecutor.execute(() -> promoDetailDao.delete(promoDetail));
    }
}
