package com.example.kedaiku.entites;

import androidx.room.Embedded;
import androidx.room.Relation;

public class SaleWithDetails {

    @Embedded
    private Sale sale;

    @Relation(
            parentColumn = "sale_customer_id",
            entityColumn = "id"
    )
    private Customer customer;

    @Relation(
            parentColumn = "promo_id",
            entityColumn = "_id"
    )
    private PromoDetail promo;

    @Relation(
            parentColumn = "sale_cash_id",
            entityColumn = "id"
    )
    private Cash cash;

    @Relation(
            parentColumn = "sale_detail_id",
            entityColumn = "id"
    )
    private DetailSale detailSale;

    // Constructor
    public SaleWithDetails(Sale sale, Customer customer, PromoDetail promo, Cash cash, DetailSale detailSale) {
        this.sale = sale;
        this.customer = customer;
        this.promo = promo;
        this.cash = cash;
        this.detailSale = detailSale;
    }

    // Getters and Setters
    public Sale getSale() {
        return sale;
    }

    public void setSale(Sale sale) {
        this.sale = sale;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public PromoDetail getPromo() {
        return promo;
    }

    public void setPromo(PromoDetail promo) {
        this.promo = promo;
    }

    public Cash getCash() {
        return cash;
    }

    public void setCash(Cash cash) {
        this.cash = cash;
    }

    public DetailSale getDetailSale() {
        return detailSale;
    }

    public void setDetailSale(DetailSale detailSale) {
        this.detailSale = detailSale;
    }

    // Custom methods to retrieve specific fields from the related entities

    public long getSaleId() {
        return sale.getId();
    }

    public String getSaleTransactionName() {
        return sale.getSale_transaction_name();
    }

    public long getSaleDate() {
        return sale.getSale_date();
    }

    public double getSaleTotal() {
        return sale.getSale_total();
    }

    public double getSalePaid() {
        return sale.getSale_paid();
    }

    public String getCustomerName() {

        if(customer==null)
        {
            if(getSale().getSale_customer_id()==0)
            return "Umum";
            else {
                return "Pelanggan Terhapus";
            }
        }
        return customer.getCustomer_name();
    }

    public String getCustomerPhone() {
        return customer.getCustomer_phone();
    }

    public String getDetail() {
        return promo.getDetail();
    }


    public String getCashName() {
        return cash.getCash_name();
    }

    public double getCashAmount() {
        return cash.getCash_value();
    }

    public String getDetailSaleItemDetail() {
        return detailSale.getSale_detail();
    }


}
