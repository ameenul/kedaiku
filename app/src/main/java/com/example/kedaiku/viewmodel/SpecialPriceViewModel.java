package com.example.kedaiku.viewmodel;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.example.kedaiku.entites.SpecialPrice;
import com.example.kedaiku.entites.SpecialPriceWithProduct;
import com.example.kedaiku.repository.SpecialPriceRepository;

import java.util.List;

public class SpecialPriceViewModel extends AndroidViewModel {

    private final SpecialPriceRepository repository;

    // MutableLiveData to hold the search keyword
    private final MutableLiveData<String> searchKeyword = new MutableLiveData<>();

    // Transformed LiveData for search results
    private final LiveData<List<SpecialPriceWithProduct>> filteredSpecialPrices;

    // LiveData for all special prices
    private final LiveData<List<SpecialPrice>> allSpecialPrices;

    public SpecialPriceViewModel(Application application) {
        super(application);
        repository = new SpecialPriceRepository(application);

        // Get all special prices
        allSpecialPrices = repository.getAllSpecialPrices();
        searchKeyword.setValue("");

        // Transform searchKeyword into search results
        filteredSpecialPrices = Transformations.switchMap(searchKeyword, keyword ->
                repository.getSpecialPriceWithProductLike(keyword));
    }

    // Insert
    public void insert(SpecialPrice specialPrice) {
        repository.insert(specialPrice);
    }

    // Update
    public void update(SpecialPrice specialPrice) {
        repository.update(specialPrice);
    }

    // Delete
    public void delete(SpecialPrice specialPrice) {
        repository.delete(specialPrice);
    }

    // Get all special prices
    public LiveData<List<SpecialPrice>> getAllSpecialPrices() {
        return allSpecialPrices;
    }

    // Set the search keyword
    public void setSearchKeyword(String keyword) {
        searchKeyword.setValue(keyword);
    }

    // Get the filtered results
    public LiveData<List<SpecialPriceWithProduct>> getFilteredSpecialPrices() {
        return filteredSpecialPrices;
    }


    public LiveData<SpecialPrice> getHighestSpecialPriceForProduct(long productId) {
        return repository.getHighestSpecialPriceForProduct(productId);
    }

    public LiveData<SpecialPrice> getSpecialPriceByGroupIdAndProductId(long groupId, long productId) {
        return repository.getSpecialPriceByGroupIdAndProductId(groupId, productId);
    }

}
