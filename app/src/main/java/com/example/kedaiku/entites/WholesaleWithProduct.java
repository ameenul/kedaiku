package com.example.kedaiku.entites;

public class WholesaleWithProduct {
    // Kolom dari Wholesale
    private long _id;
    private long product_id;
    private String name;
    private double price;
    private int qty;

    public WholesaleWithProduct(long _id, long product_id, String name, double price, int qty, int status, String product_name, double product_price, double product_primary_price) {
        this._id = _id;
        this.product_id = product_id;
        this.name = name;
        this.price = price;
        this.qty = qty;
        this.status = status;
        this.product_name = product_name;
        this.product_price = product_price;
        this.product_primary_price = product_primary_price;
    }

    public long get_id() {
        return _id;
    }

    public void set_id(long _id) {
        this._id = _id;
    }

    public long getProduct_id() {
        return product_id;
    }

    public void setProduct_id(long product_id) {
        this.product_id = product_id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public int getQty() {
        return qty;
    }

    public void setQty(int qty) {
        this.qty = qty;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getProduct_name() {
        return product_name;
    }

    public void setProduct_name(String product_name) {
        this.product_name = product_name;
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

    private int status;

    // Kolom dari Product
    private String product_name;
    private double product_price;
    private double product_primary_price;

    // Getter dan Setter untuk semua field
}
