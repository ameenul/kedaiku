


package com.example.kedaiku.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.example.kedaiku.entites.Inventory;
import com.example.kedaiku.entites.Product;
import com.example.kedaiku.entites.ProductInventory;
import com.example.kedaiku.entites.Purchase;
import com.example.kedaiku.helper.DateHelper;
import com.example.kedaiku.repository.ProductRepository;

import java.util.List;

public class ProductViewModel extends AndroidViewModel {

    private final ProductRepository repository;
    private final LiveData<List<Product>> allProducts ;

    // MutableLiveData untuk query pencarian produk (List<Product>)
    private final MutableLiveData<String> searchQueryLiveData = new MutableLiveData<>();
    // LiveData hasil pencarian produk
    private final LiveData<List<Product>> searchResults ;



    public ProductViewModel(@NonNull Application application) {
        super(application);
        repository = new ProductRepository(application);


           allProducts = repository.getAllProducts();

           // Inisialisasi nilai awal
           searchQueryLiveData.setValue("");

           // Inisialisasi searchResults menggunakan Transformations.switchMap
           searchResults = Transformations.switchMap(searchQueryLiveData, query -> {
               if (query == null || query.isEmpty()) {
                   // Jika query kosong, kembalikan semua produk
                   return allProducts;
               } else {
                   // Jika ada query, kembalikan hasil pencarian
                   return repository.searchProducts(query);
               }
           });
    }

    // Metode untuk mengatur query pencarian
    public void setSearchQuery(String query) {
        searchQueryLiveData.setValue(query.trim());
    }

    // Getter untuk hasil pencarian produk
    public LiveData<List<Product>> getSearchResults() {
        return searchResults;
    }


    // Getter untuk semua produk
    public LiveData<List<Product>> getAllProducts() {
        return allProducts;
    }

    // Metode lainnya (insert, update, delete, dll.)
    public void insert(Product product) {
        repository.insert(product);
    }

    public void updateProductWithInventory(Product product, double oldQty) {
        repository.updateProductWithInventory(product, oldQty);
    }

    public void updateProductWithInventory(Product product, double oldQty, String stockNote) {
        repository.updateProductWithInventory(product, oldQty, stockNote);
    }

    public void delete(Product product) {
        repository.delete(product);
    }

    public LiveData<Product> getProductById(long productId) {
        return repository.getProductById(productId);
    }

    public void update(Product product) {
        repository.update(product);
    }

    public void insertProductWithInventory(Product product, Inventory inventory) {
        repository.insertProductWithInventory(product, inventory);
    }

    // Kelas inner FilterParam

    // Metode untuk proses pembelian
    public void processPurchase(
            Purchase purchase,
            double selectedPrice,
            Inventory inventory,
            Product product,
            long cashId,
            double purchaseAmount,
            String cashFlowDescription,
            ProductRepository.OnTransactionCompleteListener listener
    ) {
        repository.processPurchase(
                purchase,
                selectedPrice,
                inventory,
                product,
                cashId,
                purchaseAmount,
                cashFlowDescription,
                listener
        );
    }

    public void deletePurchaseTransaction(Purchase purchase, ProductRepository.OnTransactionCompleteListener listener) {
        repository.deletePurchaseTransaction(purchase, listener);
    }
}
