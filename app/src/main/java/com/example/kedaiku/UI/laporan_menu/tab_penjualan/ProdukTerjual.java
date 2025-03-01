package com.example.kedaiku.UI.laporan_menu.tab_penjualan;

public class ProdukTerjual {
    private String productName;
    private double productPrice;
    private double quantitySold;

    public ProdukTerjual(String productName, double productPrice, double quantitySold) {
        this.productName = productName;
        this.productPrice = productPrice;
        this.quantitySold = quantitySold;
    }

    public String getProductName() {
        return productName;
    }

    public double getProductPrice() {
        return productPrice;
    }

    public double getQuantitySold() {
        return quantitySold;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public void setProductPrice(double productPrice) {
        this.productPrice = productPrice;
    }

    public void setQuantitySold(int quantitySold) {
        this.quantitySold = quantitySold;
    }
}
