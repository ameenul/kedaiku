package com.example.kedaiku.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.kedaiku.UI.penjualan_menu.CartItem;
import com.example.kedaiku.entites.Customer;

import java.util.HashMap;
import java.util.Map;

public class CartViewModel extends ViewModel {
    private MutableLiveData<Map<Long, CartItem>> cartItemsMap = new MutableLiveData<>(new HashMap<>());

    private MutableLiveData<Map<Long, CartItem>> oldCartItemsMap = new MutableLiveData<>(new HashMap<>());
    private MutableLiveData<Customer> currentCustomer = new MutableLiveData<>();
    private MutableLiveData<Long> currentDate = new MutableLiveData<>();


    public LiveData<Long> getCurrentDate() {
        return currentDate;
    }

    public void setCurrentDate(long currentDate) {
        this.currentDate.setValue(currentDate);
    }



    public MutableLiveData<Map<Long, CartItem>> getOldCartItemsMap() {
        return oldCartItemsMap;
    }

    public void setOldCartItemsMap(Map<Long, CartItem> items) {
        oldCartItemsMap.setValue(items);
    }

    public LiveData<Map<Long, CartItem>> getCartItemsMap() {
        return cartItemsMap;
    }

    public void setCartItemsMap(Map<Long, CartItem> items) {
        cartItemsMap.setValue(items);
    }

    public void addCartItem(CartItem item) {
        // Ambil Map yang ada
        Map<Long, CartItem> currentItems = cartItemsMap.getValue();
        if (currentItems == null) {
            currentItems = new HashMap<>();
        }

        // Tambahkan item ke dalam Map
        currentItems.put(item.getProductId(), item);

        // Perbarui LiveData dengan Map yang baru
        cartItemsMap.setValue(currentItems);
    }

    public void removeCartItem(Long productId) {
        Map<Long, CartItem> currentItems = cartItemsMap.getValue();
        if (currentItems != null) {
            currentItems.remove(productId);
            cartItemsMap.setValue(currentItems); // Perbarui LiveData
        }
    }

    public void removeAllCartItems() {
        // Ambil Map yang ada
        Map<Long, CartItem> currentItems = cartItemsMap.getValue();
        if (currentItems != null) {
            currentItems.clear(); // Menghapus semua item dari Map
            cartItemsMap.setValue(currentItems); // Perbarui LiveData
        }
    }

    public LiveData<Customer> getCurrentCustomer() {
        return currentCustomer;
    }

    public void setCurrentCustomer(Customer customer) {
        currentCustomer.setValue(customer);
    }
    // Metode untuk memperbarui kuantitas dan harga item di keranjang
    public void updateCartItemQuantityAndPrice(long productId, double newQuantity,
                                               double wholesalePrice, double specialPrice,
                                               String namaWholesale, String namaHargaSpesial,long spID,long wsID) {
        Map<Long, CartItem> currentCartItems = cartItemsMap.getValue();
        if (currentCartItems != null && currentCartItems.containsKey(productId)) {
            CartItem cartItem = currentCartItems.get(productId);
            cartItem.setQuantity(newQuantity);
            cartItem.setWholeSalePrice(wholesalePrice);
            cartItem.setSpecialPrice(specialPrice);
            cartItem.setNamaWholesale(namaWholesale);
            cartItem.setNamaHargaSpecial(namaHargaSpesial);
            cartItem.setIdWholesale(wsID);
            cartItem.setIdSpecialPrice(spID);
            double finalPrice= Math.min(cartItem.getPrice(),Math.min(cartItem.getSpecialPrice(),cartItem.getWholeSalePrice()));
            cartItem.setFinalPrice(finalPrice);

            cartItemsMap.setValue(currentCartItems); // Perbarui LiveData
        }
    }





}
