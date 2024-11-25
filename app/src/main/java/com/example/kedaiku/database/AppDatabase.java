package com.example.kedaiku.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.kedaiku.entites.*;
import com.example.kedaiku.ifaceDao.*;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(entities = {Product.class, Cash.class, CashFlow.class, Customer.class, CustomerGroup.class,
        ProductSold.class, Inventory.class, Sale.class, DetailSale.class, SpecialPrice.class,
        Wholesale.class, PromoDetail.class, Purchase.class, Expense.class, Creditor.class, Debt.class},
        version = 1)
public abstract class AppDatabase extends RoomDatabase {
    private static volatile AppDatabase INSTANCE;
    private static final int NUMBER_OF_THREADS = 4;
    public static final ExecutorService databaseWriteExecutor =
            Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    public abstract CashDao cashDao();
    public abstract CashFlowDao cashFlowDao();
    public abstract CreditorDao creditorDao();
    public abstract CustomerDao customerDao();
    public abstract CustomerGroupDao customerGroupDao();
    public abstract DebtDao debtDao();
    public abstract DetailSaleDao detailSaleDao();
    public abstract ExpenseDao expenseDao();
    public abstract InventoryDao inventoryDao();
    public abstract ProductSoldDao productSoldDao();
    public abstract ProductDao productDao();
    public abstract PromoDetailDao promoDetailDao();
    public abstract PurchaseDao purchaseDao();
    public abstract SaleDao saleDao();
    public abstract SpecialPriceDao specialPriceDao();
    public abstract WholesaleDao wholesaleDao();

    //public abstract CustomerGroupDao customerGroupDao();


    // Tambahkan DAO lainnya...

   public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, "app_database")
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
