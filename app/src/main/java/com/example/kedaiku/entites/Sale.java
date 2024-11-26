package com.example.kedaiku.entites;
import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;


@Entity(tableName = "table_sale")
public class Sale {
    @PrimaryKey(autoGenerate = true)
    private long id;


    private long sale_date;
    private long promo_id;
    private long sale_detail_id;

    @NonNull
    private String sale_transaction_name;

    private int sale_customer_id;

    private int sale_payment_type;
    private double sale_total;
    private int sale_cash_id;
    private double sale_hpp;
    private double sale_discount;
    private double sale_ship;

    private double sale_paid;

    // Constructor


    public Sale(  long sale_date, @NonNull String sale_transaction_name, int sale_customer_id,
                  long sale_detail_id, int sale_payment_type, double sale_total, int sale_cash_id, double sale_hpp, double sale_discount, double sale_ship, long promo_id, double sale_paid) {

        this.sale_date = sale_date;
        this.sale_transaction_name = sale_transaction_name;
        this.sale_customer_id = sale_customer_id;
        this.sale_detail_id = sale_detail_id;
        this.sale_payment_type = sale_payment_type;
        this.sale_total = sale_total;
        this.sale_cash_id = sale_cash_id;
        this.sale_hpp = sale_hpp;
        this.sale_discount = sale_discount;
        this.sale_ship = sale_ship;
        this.promo_id = promo_id;
        this.sale_paid = sale_paid;
    }

    // Getters and Setters
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }


    public long getSale_date() {
        return sale_date;
    }

    public void setSale_date( long sale_date) {
        this.sale_date = sale_date;
    }

    @NonNull
    public String getSale_transaction_name() {
        return sale_transaction_name;
    }

    public void setSale_transaction_name(@NonNull String sale_transaction_name) {
        this.sale_transaction_name = sale_transaction_name;
    }

    public int getSale_customer_id() {
        return sale_customer_id;
    }

    public void setSale_customer_id(int sale_customer_id) {
        this.sale_customer_id = sale_customer_id;
    }

    public long getSale_detail_id() {
        return sale_detail_id;
    }

    public void setSale_detail_id(long sale_detail_id) {
        this.sale_detail_id = sale_detail_id;
    }

    public int getSale_payment_type() {
        return sale_payment_type;
    }

    public void setSale_payment_type(int sale_payment_type) {
        this.sale_payment_type = sale_payment_type;
    }

    public double getSale_total() {
        return sale_total;
    }

    public void setSale_total(double sale_total) {
        this.sale_total = sale_total;
    }

    public int getSale_cash_id() {
        return sale_cash_id;
    }

    public void setSale_cash_id(int sale_cash_id) {
        this.sale_cash_id = sale_cash_id;
    }

    public double getSale_hpp() {
        return sale_hpp;
    }

    public void setSale_hpp(double sale_hpp) {
        this.sale_hpp = sale_hpp;
    }

    public double getSale_discount() {
        return sale_discount;
    }

    public void setSale_discount(double sale_discount) {
        this.sale_discount = sale_discount;
    }

    public double getSale_ship() {
        return sale_ship;
    }

    public void setSale_ship(double sale_ship) {
        this.sale_ship = sale_ship;
    }

    public long getPromo_id() {
        return promo_id;
    }

    public void setPromo_id(long promo_id) {
        this.promo_id = promo_id;
    }

    public double getSale_paid() {
        return sale_paid;
    }

    public void setSale_paid(double sale_paid) {
        this.sale_paid = sale_paid;
    }
}

