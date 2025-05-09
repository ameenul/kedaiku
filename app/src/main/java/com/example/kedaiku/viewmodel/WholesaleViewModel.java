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

    // MutableLiveData to hold the productId and quantity
    private final MutableLiveData<Long> productIdLiveData = new MutableLiveData<>();
    private final MutableLiveData<Double> quantityLiveData = new MutableLiveData<>();

    // Transformed LiveData for wholesale price based on productId and quantity
    private final LiveData<Wholesale> wholesalePrice;

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

        // Transform productId and quantity into wholesale price
        wholesalePrice = Transformations.switchMap(productIdLiveData, productId ->
                Transformations.switchMap(quantityLiveData, quantity ->
                        repository.getWholesalePriceForProduct(productId,  quantity)));
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

    public LiveData<Wholesale> getWholesalePriceForProduct(long productId, double quantity) {
        return repository.getWholesalePriceForProduct(productId, quantity);
    }

    // Set productId and quantity to get the wholesale price
    public void setProductIdAndQuantity(long productId, double quantity) {
        productIdLiveData.setValue(productId);
        quantityLiveData.setValue(quantity);
    }

    // Get the wholesale price for the current productId and quantity
    public LiveData<Wholesale> getWholesalePrice() {
        return wholesalePrice;
    }
}
