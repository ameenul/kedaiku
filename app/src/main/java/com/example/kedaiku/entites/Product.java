package com.example.kedaiku.entites;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "table_product")
public class Product {
    @PrimaryKey(autoGenerate = true)
    private long id;

    @NonNull
    private String product_name;

    @NonNull
    private String product_description;

    @NonNull
    private String product_sku;

    private double product_price;
    private double product_primary_price;

    @NonNull
    private String product_unit;

    private double product_qty;

    // Constructor, Getter dan Setter




    public Product(@NonNull String product_name, @NonNull String product_description, @NonNull String product_sku, double product_price, double product_primary_price, @NonNull String product_unit, double product_qty) {
        this.product_name = product_name;
        this.product_description = product_description;
        this.product_sku = product_sku;
        this.product_price = product_price;
        this.product_primary_price = product_primary_price;
        this.product_unit = product_unit;
        this.product_qty = product_qty;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @NonNull
    public String getProduct_name() {
        return product_name;
    }

    public void setProduct_name(@NonNull String product_name) {
        this.product_name = product_name;
    }

    @NonNull
    public String getProduct_description() {
        return product_description;
    }

    public void setProduct_description(@NonNull String product_description) {
        this.product_description = product_description;
    }

    @NonNull
    public String getProduct_sku() {
        return product_sku;
    }

    public void setProduct_sku(@NonNull String product_sku) {
        this.product_sku = product_sku;
    }

    public double getProduct_price() {
        return product_price;
    }

    public void setProduct_price(double product_price) {
        this.product_price = product_price;
    }

    public double getProduct_primary_price() {
        return product_primary_price;
    }

    public void setProduct_primary_price(double product_primary_price) {
        this.product_primary_price = product_primary_price;
    }

    @NonNull
    public String getProduct_unit() {
        return product_unit;
    }

    public void setProduct_unit(@NonNull String product_unit) {
        this.product_unit = product_unit;
    }

    public double getProduct_qty() {
        return product_qty;
    }

    public void setProduct_qty(double product_qty) {
        this.product_qty = product_qty;
    }
}
