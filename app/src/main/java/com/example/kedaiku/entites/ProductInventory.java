package com.example.kedaiku.entites;

public class ProductInventory {
    private long productId; // Tambahkan ini
    private String productName;
    private double stockIn;
    private double stockOut;
    private double stockBalance;

    // Constructor diperbarui untuk menerima productId
    public ProductInventory(long productId, String productName, double stockIn, double stockOut, double stockBalance) {
        this.productId = productId;
        this.productName = productName;
        this.stockIn = stockIn;
        this.stockOut = stockOut;
        this.stockBalance = stockBalance;
    }

    // Getter dan Setter untuk productId
    public long getProductId() {
        return productId;
    }

    public void setProductId(long productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public double getStockIn() {
        return stockIn;
    }

    public double getStockOut() {
        return stockOut;
    }

    public double getStockBalance() {
        return stockBalance;
    }
}
