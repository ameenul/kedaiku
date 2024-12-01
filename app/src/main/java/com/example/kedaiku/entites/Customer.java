package com.example.kedaiku.entites;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "table_customer")
public class Customer {
    @PrimaryKey(autoGenerate = true)
    private long id;

    @NonNull
    private String customer_name;

    @NonNull
    private String customer_address;


    private String customer_email;

    @NonNull
    private String customer_phone;

    private long customer_group_id; // Foreign key to Customer Group

    // Constructor, Getter, dan Setter


    public Customer(@NonNull String customer_name, @NonNull String customer_address, String customer_email, @NonNull String customer_phone, long customer_group_id) {

        this.setCustomer_name(customer_name);
        this.setCustomer_address(customer_address);
        this.setCustomer_email(customer_email);
        this.setCustomer_phone(customer_phone);
        this.setCustomer_group_id(customer_group_id);
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @NonNull
    public String getCustomer_name() {
        return customer_name;
    }

    public void setCustomer_name(@NonNull String customer_name) {
        this.customer_name = customer_name;
    }

    @NonNull
    public String getCustomer_address() {
        return customer_address;
    }

    public void setCustomer_address(@NonNull String customer_address) {
        this.customer_address = customer_address;
    }


    public String getCustomer_email() {
        return customer_email;
    }

    public void setCustomer_email(String customer_email) {
        this.customer_email = customer_email;
    }

    @NonNull
    public String getCustomer_phone() {
        return customer_phone;
    }

    public void setCustomer_phone(@NonNull String customer_phone) {
        this.customer_phone = customer_phone;
    }

    public long getCustomer_group_id() {
        return customer_group_id;
    }

    public void setCustomer_group_id(long customer_group_id) {
        this.customer_group_id = customer_group_id;
    }
}

