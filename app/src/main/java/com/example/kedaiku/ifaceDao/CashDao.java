package com.example.kedaiku.ifaceDao;



import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import com.example.kedaiku.entites.Cash;
import com.example.kedaiku.entites.CashFlow;

import java.util.List;

@Dao
public interface CashDao {

    @Insert
    void insert(Cash cash);

    @Update
    void update(Cash cash);

    @Query("DELETE FROM table_cash WHERE id = :cashId")
    void delete(long cashId);

    @Query("SELECT * FROM table_cash ORDER BY cash_name ASC")
    LiveData<List<Cash>> getAllCash();

    @Query("UPDATE table_cash SET cash_value = cash_value + :amount WHERE id = :cashId")
    void updateCashValue(long cashId, double amount);

    @Insert
    void insertCashFlow(CashFlow cashFlow);

    // Metode baru untuk mengambil objek Cash berdasarkan ID
    @Query("SELECT * FROM table_cash WHERE id = :cashId")
    LiveData<Cash> getCashById(long cashId);

    @Query("SELECT * FROM table_cash WHERE id = :cashId LIMIT 1")
    Cash getCashByIdSync(long cashId);


    @Transaction
    default void updateCashWithTransaction(long cashId, double amount, String description) {
        updateCashValue(cashId, amount);
//        Date dt = new Date();
//        SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-yyyy-hh:mm");

        CashFlow cashFlow = new CashFlow(cashId,System.currentTimeMillis() , description,amount );
        insertCashFlow(cashFlow);
    }

    @Transaction
    default void transferCash(long sourceCashId, long targetCashId, double amount, String description) {
//        Date dt = new Date();
//        SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-yyyy-hh:mm");
        // Ambil objek Cash untuk sumber dan tujuan
        Cash sourceCash = getCashByIdSync(sourceCashId);
        Cash targetCash = getCashByIdSync(targetCashId);

        // Ambil nama kas
        String sourceCashName = sourceCash.getCash_name();
        String targetCashName = targetCash.getCash_name();


        // Kurangi saldo cash sumber
        updateCashValue(sourceCashId, -amount);
        // Tambah saldo cash tujuan
        updateCashValue(targetCashId, amount);

        String transferDescription = "Transfer dari " + sourceCashName + " ke " + targetCashName + ". " + description;

        // Insert ke cash_flow sumber
        CashFlow sourceCashFlow = new CashFlow(sourceCashId,System.currentTimeMillis(), transferDescription, -amount);
        insertCashFlow(sourceCashFlow);

        // Insert ke cash_flow tujuan
        CashFlow targetCashFlow = new CashFlow(targetCashId, System.currentTimeMillis(), transferDescription, amount);
        insertCashFlow(targetCashFlow);
    }
}
