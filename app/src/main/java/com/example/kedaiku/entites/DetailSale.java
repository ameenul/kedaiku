package com.example.kedaiku.entites;
import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "table_detail_sale")
public class DetailSale {
    @PrimaryKey(autoGenerate = true)
    private int id;

    @NonNull
    private String sale_detail;

    @NonNull
    private String sale_paid_history;

    // Constructor
    public DetailSale(@NonNull String sale_detail, @NonNull String sale_paid_history) {
        this.sale_detail = sale_detail;
        this.sale_paid_history = sale_paid_history;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @NonNull
    public String getSale_detail() {
        return sale_detail;
    }

    public void setSale_detail(@NonNull String sale_detail) {
        this.sale_detail = sale_detail;
    }

    @NonNull
    public String getSale_paid_history() {
        return sale_paid_history;
    }

    public void setSale_paid_history(@NonNull String sale_paid_history) {
        this.sale_paid_history = sale_paid_history;
    }
}

