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


    public static final Migration MIGRATION_2_3 = new Migration(2, 1) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
//            database.execSQL("CREATE TABLE IF NOT EXISTS `table_special_price_new` (" +
//                    "`_id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
//                    "`name` TEXT NOT NULL, " +
//                    "`product_id` INTEGER NOT NULL, " +
//                    "`group_id` INTEGER NOT NULL, " +
//                    "`percent` REAL NOT NULL, " + // Menggunakan 'percent' sebagai nama kolom
//                    "`status` INTEGER NOT NULL)");
//
//            // Salin data dari tabel lama ke tabel baru
//            database.execSQL("INSERT INTO table_special_price_new (`_id`, `name`, `product_id`, `group_id`, `percent`, `status`) " +
//                    "SELECT `_id`, `name`, `product_id`, `group_id`, `precent`, `status` FROM table_special_price");
//
//            // Hapus tabel lama
//            database.execSQL("DROP TABLE table_special_price");
//
//            // Ganti nama tabel baru menjadi nama tabel lama
//            database.execSQL("ALTER TABLE table_special_price_new RENAME TO table_special_price");

            // Buat tabel baru dengan tipe data yang diperbarui
            database.execSQL("CREATE TABLE new_table_wholesale (" +
                    "_id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "product_id INTEGER NOT NULL, " +
                    "name TEXT NOT NULL, " +
                    "price REAL NOT NULL, " +
                    "qty REAL NOT NULL, " + // Ubah dari INTEGER menjadi REAL
                    "status INTEGER NOT NULL)");

            // Salin data dari tabel lama ke tabel baru
            database.execSQL("INSERT INTO new_table_wholesale (product_id, name, price, qty, status) " +
                    "SELECT product_id, name, price, qty, status FROM table_wholesale");

            // Hapus tabel lama
            database.execSQL("DROP TABLE table_wholesale");

            // Ganti nama tabel baru menjadi nama tabel lama
            database.execSQL("ALTER TABLE new_table_wholesale RENAME TO table_wholesale");
        }
    };



    //public abstract CustomerGroupDao customerGroupDao();


    // Tambahkan DAO lainnya...

//   public static AppDatabase getDatabase(final Context context) {
//        if (INSTANCE == null) {
//            synchronized (AppDatabase.class) {
//                if (INSTANCE == null) {
//                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
//                                    AppDatabase.class, "app_database")
//                            .fallbackToDestructiveMigration()
//                            .build();
//                }
//            }
//        }
//        return INSTANCE;
//    }

    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, "app_database")
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
//                            .addMigrations(MIGRATION_2_3)
//                            .build();
//                }
//            }
//        }
//        return INSTANCE;
//    }

}
