package com.example.kedaiku.entites;
import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "table_customer_group")
public class CustomerGroup {
    @PrimaryKey(autoGenerate = true)
    private int id;

    @NonNull
    private String name;

    // Constructor, Getter, dan Setter

    public CustomerGroup(@NonNull String name) {
               this.setName(name);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @NonNull
    public String getName() {
        return name;
    }

    public void setName(@NonNull String name) {
        this.name = name;
    }
}

