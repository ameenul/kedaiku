package com.example.kedaiku.entites;

import androidx.room.Embedded;
import androidx.room.Relation;

public class SpecialPriceWithProduct {

    @Embedded
    private SpecialPrice specialPrice;

    @Relation(
            parentColumn = "product_id",
            entityColumn = "id"
    )
    private Product product;

    public SpecialPriceWithProduct(SpecialPrice specialPrice, Product product) {
        this.specialPrice = specialPrice;
        this.product = product;
    }

    public SpecialPrice getSpecialPrice() {
        return specialPrice;
    }

    public void setSpecialPrice(SpecialPrice specialPrice) {
        this.specialPrice = specialPrice;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public long get_id() {
        return specialPrice.get_id();
    }

    public String getName() {
        return specialPrice.getName();
    }

    public long getProduct_id() {
        return specialPrice.getProduct_id();
    }

    public long getGroup_id() {
        return specialPrice.getGroup_id();
    }

    public double getPrecent() {
        return specialPrice.getPrecent();
    }

    public int getStatus() {
        return specialPrice.getStatus();
    }

    public String getProduct_name() {
        return product.getProduct_name();
    }

    public String getProduct_description() {
        return product.getProduct_description();
    }

    public String getProduct_sku() {
        return product.getProduct_sku();
    }

    public double getProduct_price() {
        return product.getProduct_price();
    }

    public double getProduct_primary_price() {
        return product.getProduct_primary_price();
    }

    public String getProduct_unit() {
        return product.getProduct_unit();
    }

    public double getProduct_qty() {
        return product.getProduct_qty();
    }

    public String getBarcode() {
        return product.getBarcode();
    }

    public Product toProduct() {
        Product product = new Product(
                this.product.getProduct_name(),
                this.product.getProduct_description(),
                this.product.getProduct_sku(),
                this.product.getProduct_price(),
                this.product.getProduct_primary_price(),
                this.product.getProduct_unit(),
                this.product.getProduct_qty()
        );
        product.setId(this.product.getId());
        product.setBarcode(this.product.getBarcode());
        return product;
    }

    public SpecialPrice toSpecialPrice() {
        SpecialPrice specialPrice = new SpecialPrice(
                this.specialPrice.getName(),
                this.specialPrice.getProduct_id(),
                this.specialPrice.getGroup_id(),
                this.specialPrice.getPrecent(),
                this.specialPrice.getStatus()
        );
        specialPrice.set_id(this.specialPrice.get_id());
        return specialPrice;
    }
}
