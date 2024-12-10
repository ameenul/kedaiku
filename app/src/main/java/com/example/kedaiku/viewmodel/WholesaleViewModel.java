package com.example.kedaiku.viewmodel;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.example.kedaiku.entites.Wholesale;
import com.example.kedaiku.entites.WholesaleWithProduct;
import com.example.kedaiku.repository.WholesaleRepository;

import java.util.List;

public class WholesaleViewModel extends AndroidViewModel {

    private final WholesaleRepository repository;

    // MutableLiveData to hold the search keyword
    private final MutableLiveData<String> searchKeyword = new MutableLiveData<>();

    // Transformed LiveData for search results
    private final LiveData<List<WholesaleWithProduct>> filteredWholesales;

    // LiveData for all wholesales
    private final LiveData<List<Wholesale>> allWholesales;

    public WholesaleViewModel(Application application) {
        super(application);
        repository = new WholesaleRepository(application);

        // Get all wholesales
        allWholesales = repository.getAllWholesales();
        searchKeyword.setValue("");

        // Transform searchKeyword into search results
        filteredWholesales = Transformations.switchMap(searchKeyword, keyword ->
                repository.getWholesaleWithProductLike(keyword));
    }

    // Insert
    public void insert(Wholesale wholesale) {
        repository.insert(wholesale);
    }

    // Update
    public void update(Wholesale wholesale) {
        repository.update(wholesale);
    }

    // Delete
    public void delete(Wholesale wholesale) {
        repository.delete(wholesale);
    }

    // Get all wholesales
    public LiveData<List<Wholesale>> getAllWholesales() {
        return allWholesales;
    }

    // Set the search keyword
    public void setSearchKeyword(String keyword) {
        searchKeyword.setValue(keyword);
    }

    // Get the filtered results
    public LiveData<List<WholesaleWithProduct>> getFilteredWholesales() {
        return filteredWholesales;
    }
}
