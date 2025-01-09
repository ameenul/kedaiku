package com.example.kedaiku.helper;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;

import com.example.kedaiku.UI.penjualan_menu.CartItem;
import com.example.kedaiku.entites.PromoDetail;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class FormatHelper {


    public static String formatCurrency(double value) {
        NumberFormat numberFormat = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
        return numberFormat.format(value);
    }


    public static String getDescDate(long date)
    {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy HH:mm:ss", Locale.getDefault());
        String descDate = dateFormat.format(date);
        return descDate;

    }


    public static String getDescDate(long date, String format)
    {
        SimpleDateFormat dateFormat = new SimpleDateFormat(format, Locale.getDefault());
        String descDate = dateFormat.format(date);
        return descDate;

    }
//
//    public static String parseCustomerNameFromDetailSale(String jsonDetail) {
//        try {
//            JSONObject root = new JSONObject(jsonDetail);
//        } catch (JSONException e) {
//            throw new RuntimeException(e);
//        }
//        ;
//
//
//    }

    public static List<CartItem> parseCartItemsFromDetailSale(String jsonDetail) {
        List<CartItem> cartItems = new ArrayList<>();

        try {
            // Buat objek JSON dari string
            JSONObject root = new JSONObject(jsonDetail);

            // Ambil array "items"
            JSONArray itemsArray = root.optJSONArray("items");
            if (itemsArray == null) {
                return cartItems; // jika "items" tidak ada, kembalikan list kosong
            }

            // Loop setiap item di array
            for (int i = 0; i < itemsArray.length(); i++) {
                JSONObject itemObj = itemsArray.getJSONObject(i);

                long productId     = itemObj.optLong("product_id", -1);
                String name        = itemObj.optString("name", "");
                double primaryPrice= itemObj.optDouble("primary_price", 0.0);  // HPP
                double sellPrice   = itemObj.optDouble("sell_price", 0.0);
                double normalPrice   = itemObj.optDouble("normal_price", 0.0);
                double qty         = itemObj.optDouble("qty", 0.0);
                String unit        = itemObj.optString("unit", "");

                // Buat CartItem sesuai konstruktor
                // Constructor CartItem:
                //   CartItem(long productId, String productName, double quantity,
                //            double productQty, double price, double HPP, String unit)
                //
                // productQty (stok asli produk) tidak terekam di JSON,
                // jadi kita isi 0 atau -1, tergantung kebutuhan.
                CartItem cartItem = new CartItem(
                        productId,           // productId
                        name,                // productName
                        qty,                 // quantity terjual
                        0,                   // productQty (stok di DB) tidak kita simpan di JSON ini
                        sellPrice,           // final price
                        primaryPrice,        // HPP
                        unit
                );

                cartItems.add(cartItem);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return cartItems;
    }


    public static List<CartItem> parseCartItemsFromDetailSale(String jsonDetail,String jsonPromo) {
        List<CartItem> cartItems = new ArrayList<>();

        try {
            // Buat objek JSON dari string
            JSONObject root = new JSONObject(jsonDetail);

            // Ambil array "items"
            JSONArray itemsArray = root.optJSONArray("items");
            if (itemsArray == null) {
                return cartItems; // jika "items" tidak ada, kembalikan list kosong
            }
            //
            JSONObject root2 = new JSONObject(jsonPromo);
            JSONArray promoArray = root2.optJSONArray("promo_detail");
            if (promoArray == null) {
                return cartItems; // jika "promo_detail" tidak ada, kembalikan list kosong
            }

            //


            // Loop setiap item di array
            for (int i = 0; i < itemsArray.length(); i++) {
                JSONObject itemObj = itemsArray.getJSONObject(i);

                long productId     = itemObj.optLong("product_id", -1);
                String name        = itemObj.optString("name", "");
                double primaryPrice= itemObj.optDouble("primary_price", 0.0);  // HPP
                double sellPrice   = itemObj.optDouble("sell_price", 0.0);
                double qty         = itemObj.optDouble("qty", 0.0);
                String unit        = itemObj.optString("unit", "");


                JSONObject promoObj = promoArray.getJSONObject(i);

                long promoId      = promoObj.optLong("promo_id", -1);
                String promoName  = promoObj.optString("promo_name", "");
                int promoType     = promoObj.optInt("promo_type", 0);
                double promoValue = promoObj.optDouble("promo_value", 0.0);


                // Buat CartItem sesuai konstruktor
                // Constructor CartItem:
                //   CartItem(long productId, String productName, double quantity,
                //            double productQty, double price, double HPP, String unit)
                //
                // productQty (stok asli produk) tidak terekam di JSON,
                // jadi kita isi 0 atau -1, tergantung kebutuhan.
                CartItem cartItem = new CartItem(
                        productId,           // productId
                        name,                // productName
                        qty,                 // quantity terjual
                        0,                   // productQty (stok di DB) tidak kita simpan di JSON ini
                        Double.MAX_VALUE,           // normal price
                        primaryPrice,        // HPP
                        unit
                );

                if(promoType==1)
                {


                    cartItem.setIdWholesale(promoId);
                    cartItem.setWholeSalePrice(promoValue);
                    cartItem.setNamaWholesale(promoName);

                    cartItem.setIdSpecialPrice(-1);
                    cartItem.setSpecialPrice(Double.MAX_VALUE);
                    cartItem.setNamaHargaSpecial("");
                }
                else if(promoType==2)
                {
                    cartItem.setIdSpecialPrice(promoId);
                    cartItem.setSpecialPrice(promoValue);
                    cartItem.setNamaHargaSpecial(promoName);

                    cartItem.setIdWholesale(-1);
                    cartItem.setWholeSalePrice(Double.MAX_VALUE);
                    cartItem.setNamaWholesale("");

                }
                else{
                    cartItem.setPrice(sellPrice);

                    cartItem.setIdSpecialPrice(-1);
                    cartItem.setSpecialPrice(Double.MAX_VALUE);

                    cartItem.setIdWholesale(-1);
                    cartItem.setWholeSalePrice(Double.MAX_VALUE);


                    cartItem.setNamaWholesale("");
                    cartItem.setNamaHargaSpecial("");


                }
                cartItem.setFinalPrice(sellPrice);


                cartItems.add(cartItem);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return cartItems;
    }


    public static <T> void observeOnce(LiveData<T> liveData, Observer<T> observer) {
        liveData.observeForever(new Observer<T>() {
            @Override
            public void onChanged(T t) {
                observer.onChanged(t);
                liveData.removeObserver(this);
            }
        });
    }


}
