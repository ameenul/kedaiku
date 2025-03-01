// DebtWithCreditor.java
package com.example.kedaiku.entites;

import androidx.room.Embedded;
import androidx.room.Relation;

import com.example.kedaiku.entites.Debt;

public class DebtWithCreditor {
    @Embedded
    public Debt debt;

    @Relation(
            parentColumn = "creditor_id",
            entityColumn = "_id"
    )
    public Creditor creditor;
}
