package com.example.kedaiku.UI.penjualan_menu;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import android.view.View;
import android.widget.TextView;

import com.example.kedaiku.R;
import com.example.kedaiku.entites.Customer;
import com.example.kedaiku.entites.Product;
import com.example.kedaiku.entites.SpecialPrice;
import com.example.kedaiku.entites.Wholesale;


public class CartItem implements Cloneable {
    private long productId;
    private String productName;
    private double quantity;

    public String getPriceType() {
        return priceType;
    }

    public void setPriceType(String priceType) {
        this.priceType = priceType;
    }

    private String priceType;



    private double productQuantity;

    public void setPrice(double price) {
        this.price = price;
    }

    private double price;
    private double wholeSalePrice;
    private double specialPrice;
    private  String namaWholesale;
    private  long idWholesale;

    private  String namaHargaSpecial;
    private  long idSpecialPrice;

    private  String unit;
    private double finalPrice;


    public long getIdWholesale() {
        return idWholesale;
    }

    public void setIdWholesale(long idWholesale) {
        this.idWholesale = idWholesale;
    }

    public long getIdSpecialPrice() {
        return idSpecialPrice;
    }

    public void setIdSpecialPrice(long idSpecialPrice) {
        this.idSpecialPrice = idSpecialPrice;
    }

    public double getProductQuantity() {
        return productQuantity;
    }

    public void setProductQuantity(double productQuantity) {
        this.productQuantity = productQuantity;
    }
    public double getHpp() {
        return hpp;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public void setHpp(double hpp) {
        this.hpp = hpp;
    }

    private double hpp;

    public double getFinalPrice() {
        return finalPrice;
    }

    public void setFinalPrice(double finalPrice) {
        this.finalPrice = finalPrice;
    }

    public String getNamaHargaSpecial() {
        return namaHargaSpecial;
    }

    public void setNamaHargaSpecial(String namaHargaSpecial) {
        this.namaHargaSpecial = namaHargaSpecial;
    }

    public String getNamaWholesale() {
        return namaWholesale;
    }

    public void setNamaWholesale(String namaWholesale) {
        this.namaWholesale = namaWholesale;
    }




    public double getSpecialPrice() {
        return specialPrice;
    }

    public void setSpecialPrice(double specialPrice) {
        this.specialPrice = specialPrice;
    }

    public double getWholeSalePrice() {
        return wholeSalePrice;
    }

    public void setWholeSalePrice(double wholeSalePrice) {
        this.wholeSalePrice = wholeSalePrice;
    }

    public View getItemView() {
        return itemView;
    }

    public void setItemView(View itemView) {
        this.itemView = itemView;
    }

    private View itemView; // Tambahkan referensi ke itemView

    public CartItem(long productId, String productName, double quantity,double productQuantity, double price, double hpp,String unit) { // Tambahkan itemView ke konstruktor
        this.productId = productId;
        this.productName = productName;
        this.quantity = quantity;
        this.price = price;
        this.productQuantity = productQuantity;
        this.hpp = hpp;
        this.unit=unit;

    }

    // Getter dan setter
    public long getProductId() {
        return productId;
    }

    public String getProductName() {
        return productName;
    }

    public double getQuantity() {
        return quantity;
    }

    public void setQuantity(double quantity) {
        this.quantity = quantity;
       // updateItemView(); // Perbarui tampilan saat kuantitas diubah
    }

    public double getPrice() {
        return price;
    }

    public void updatePrice(double newPrice) {
        this.price = newPrice;
       // updateItemView(); // Perbarui tampilan saat harga diubah
    }

    @Override
    public CartItem clone() {
        try {
            CartItem clone = (CartItem) super.clone();
            // TODO: copy mutable state here, so the clone can't change the internals of the original
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }


//
//    private void updateItemView() {
//        if (itemView != null) {
//            TextView itemDetails = itemView.findViewById(R.id.textViewItemDetails);
//            TextView itemTotal = itemView.findViewById(R.id.textViewItemTotal);
//            itemDetails.setText(productName + "\n" + quantity + " x " + price);
//            itemTotal.setText(String.valueOf(quantity * price));
//        }
//    }



}
