package com.example.kedaiku.database;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.example.kedaiku.entites.*;
import com.example.kedaiku.ifaceDao.*;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(entities = {Product.class, Cash.class, CashFlow.class, Customer.class, CustomerGroup.class,
        ProductSold.class, Inventory.class, Sale.class, DetailSale.class, SpecialPrice.class,
        Wholesale.class, PromoDetail.class, Purchase.class, Expense.class, Creditor.class, Debt.class},
        version = 2)
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


//    public static final Migration MIGRATION_1_2 = new Migration(1, 2) {
//        @Override
//        public void migrate(@NonNull SupportSQLiteDatabase database) {
//            // Perintah SQL untuk menambah kolom barcode:
//            database.execSQL("ALTER TABLE table_product ADD COLUMN barcode TEXT");
//            // Buat indeks unik untuk kolom barcode
//            database.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_table_product_barcode ON table_product(barcode)");
//        }
//    };



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

//    public static AppDatabase getDatabase(final Context context) {
//        if (INSTANCE == null) {
//            synchronized (AppDatabase.class) {
//                if (INSTANCE == null) {
//                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
//                                    AppDatabase.class, "app_database")
//                            // Hapus fallbackToDestructiveMigration() agar data tidak hilang
//                            .addMigrations(MIGRATION_1_2)
//                            .build();
//                }
//            }
//        }
//        return INSTANCE;
//    }

}
