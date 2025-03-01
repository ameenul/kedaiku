package com.example.kedaiku.helper;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;

import com.example.kedaiku.UI.penjualan_menu.CartItem;
import com.example.kedaiku.entites.ParsingHistory;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class FormatHelper {


    public static String formatCurrency(double value) {
        NumberFormat numberFormat = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
        return numberFormat.format(value);
    }


    public static String getDescDate(long date)
    {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        String descDate = dateFormat.format(date);
        return descDate;

    }

    /**
     * Mengonversi string tanggal dengan format "dd/MM/yyyy" menjadi objek Date.
     *
     * @param dateStr String tanggal, misalnya "03/01/2025"
     * @return Objek Date jika parsing berhasil, atau null jika terjadi error.
     */
    public static Date parseDate(String dateStr) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        try {
            return sdf.parse(dateStr);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }



    /**
     * Mengonversi string tanggal dengan format "dd/MM/yyyy" menjadi timestamp (milidetik).
     *
     * @param dateStr String tanggal, misalnya "03/01/2025"
     * @return Timestamp dalam milidetik jika parsing berhasil, atau -1 jika terjadi error.
     */
    public static long parseTimestamp(String dateStr) {
        Date date = parseDate(dateStr);
        return (date != null) ? date.getTime() : -1;
    }


    public static String getDescDate(long date, String format)
    {
        SimpleDateFormat dateFormat = new SimpleDateFormat(format, Locale.getDefault());
        String descDate = dateFormat.format(date);
        return descDate;

    }



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
                        normalPrice,           //normal price
                        primaryPrice,        // HPP
                        unit
                );
                cartItem.setFinalPrice(sellPrice); // final price

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
                double normalPrice = itemObj.optDouble("normal_price", 0.0);
                double sellPrice   = itemObj.optDouble("sell_price", 0.0);
                double qty         = itemObj.optDouble("qty", 0.0);
                String unit        = itemObj.optString("unit", "");
                String priceType        = itemObj.optString("price_type", "");


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
                        normalPrice,           // normal price
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
                cartItem.setPriceType(priceType);


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

    /**
     * Mengonversi daftar ParsingHistory menjadi string JSON
     *
     * @param paidHistories Daftar riwayat pembayaran
     * @return String JSON yang berisi paid_history
     */
    public static String convertPaidHistoriesToJson(List<ParsingHistory> paidHistories) {
        JSONObject root = new JSONObject();
        JSONArray paidHistoryArray = new JSONArray();

        try {
            for (ParsingHistory history : paidHistories) {
                JSONObject historyObj = new JSONObject();
                historyObj.put("date", history.getDate());
                historyObj.put("paid", history.getPaid());
                paidHistoryArray.put(historyObj);
            }
            root.put("paid_history", paidHistoryArray);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return root.toString();
    }

    /**
     * Metode untuk mem-parsing string JSON sale_paid_history menjadi daftar ParsingHistory
     *
     * @param jsonPaidHistory String JSON yang berisi paid_history
     * @return List<ParsingHistory> daftar riwayat pembayaran
     */
    public static List<ParsingHistory> parsePaidHistory(String jsonPaidHistory) {
        if (jsonPaidHistory == null || jsonPaidHistory.isEmpty()) {
            return new ArrayList<>();
        }

        List<ParsingHistory> parsingHistories = new ArrayList<>();

        try {
            // Membuat objek JSON dari string
            JSONObject root = new JSONObject(jsonPaidHistory);

            // Mengambil array "paid_history"
            JSONArray paidHistoryArray = root.optJSONArray("paid_history");
            if (paidHistoryArray == null) {
                return parsingHistories; // Jika "paid_history" tidak ada, kembalikan list kosong
            }

            // Loop setiap objek di array
            for (int i = 0; i < paidHistoryArray.length(); i++) {
                JSONObject historyObj = paidHistoryArray.getJSONObject(i);

                long date = historyObj.optLong("date", 0);
                double paid = historyObj.optDouble("paid", 0.0);

                ParsingHistory parsingHistory = new ParsingHistory(date, paid);
                parsingHistories.add(parsingHistory);
            }

        } catch (JSONException e) {
            e.printStackTrace();
            // Anda bisa menambahkan penanganan error lebih lanjut jika diperlukan
        }

        return parsingHistories;
    }

    // Metode untuk memfilter daftar ParsingHistory berdasarkan rentang tanggal
    public static List<ParsingHistory> filterPaidHistoryByDateRange(List<ParsingHistory> historyList, long startDate, long endDate) {
        List<ParsingHistory> filtered = new ArrayList<>();
        for (ParsingHistory history : historyList) {
            if (history.getDate() >= startDate && history.getDate() <= endDate) {
                filtered.add(history);
            }
        }
        return filtered;
    }


}
