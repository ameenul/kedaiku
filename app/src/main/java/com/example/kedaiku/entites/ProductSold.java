package com.example.kedaiku.entites;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;


@Entity(tableName = "table_product_sold")
public class ProductSold {
    @PrimaryKey(autoGenerate = true)
    private long id;



    private long product_id;

    @NonNull
    private String product_name;

    private double price;
    private double qty;

    @NonNull
    private String unit;

    // Constructor


    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }




    public ProductSold( long product_id, @NonNull String product_name, double price, double qty, @NonNull String unit) {

        this.product_id = product_id;
        this.product_name = product_name;
        this.price = price;
        this.qty = qty;
        this.unit = unit;
    }

    // Getters and Setters
    public long getProduct_id() {
        return product_id;
    }

    public void setProduct_id(long product_id) {
        this.product_id = product_id;
    }

    @NonNull
    public String getProduct_name() {
        return product_name;
    }

    public void setProduct_name(@NonNull String product_name) {
        this.product_name = product_name;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public double getQty() {
        return qty;
    }

    public void setQty(double qty) {
        this.qty = qty;
    }

    @NonNull
    public String getUnit() {
        return unit;
    }

    public void setUnit(@NonNull String unit) {
        this.unit = unit;
    }
}

