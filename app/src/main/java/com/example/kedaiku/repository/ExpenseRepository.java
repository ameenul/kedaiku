package com.example.kedaiku.repository;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;

import androidx.lifecycle.LiveData;

import com.example.kedaiku.database.AppDatabase;
import com.example.kedaiku.entites.Expense;
import com.example.kedaiku.entites.ExpenseWithCash;
import com.example.kedaiku.ifaceDao.CashDao;
import com.example.kedaiku.ifaceDao.ExpenseDao;


import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ExpenseRepository {

    private ExpenseDao expenseDao;
    private CashDao cashDao;
    private final ExecutorService executorService;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private AppDatabase db;

    public ExpenseRepository(Application application) {
        db = AppDatabase.getDatabase(application);
        expenseDao = db.expenseDao();
        cashDao = db.cashDao();  // Ambil CashDao dari database
        executorService = Executors.newSingleThreadExecutor();
    }

    public LiveData<List<ExpenseWithCash>> getAllExpensesWithCash() {
        return expenseDao.getAllExpensesWithCash();
    }

    // Filtering berdasarkan rentang tanggal dan expense_name (query di DAO)
    public LiveData<List<ExpenseWithCash>> getFilteredExpenseWithSearch(long startDate, long endDate, String expenseName) {
        return expenseDao.getFilteredExpenseWithSearch(startDate, endDate, expenseName);
    }

    // Insert expense: lakukan deduct saldo kas, lalu insert expense
    public void insertExpense( Expense expense, OnTransactionCompleteListener listener) {
        executorService.execute(() -> {
            try {
                db.runInTransaction(() -> {
                    // Deduct saldo kas: gunakan updateCashWithTransaction() dengan nilai negatif
                    long id = expenseDao.insertExpense(expense);
                    cashDao.updateCashWithTransaction(expense.getExpense_cash_id(), -expense.getExpense_amount(), "Pengeluaran ID : "+id);
                });
                mainHandler.post(() -> { if (listener != null) listener.onSuccess(true); });
            } catch (Exception e) {
                e.printStackTrace();
                mainHandler.post(() -> { if (listener != null) listener.onFailure(false); });
            }
        });
    }

    public void deleteExpenseTransaction( Expense expense,  OnTransactionCompleteListener listener) {
        executorService.execute(() -> {
            try {
                db.runInTransaction(() -> {
                    cashDao.updateCashWithTransaction(expense.getExpense_cash_id(), expense.getExpense_amount(), "Refund expense on delete id :"+expense.get_id());
                    expenseDao.deleteExpense(expense);
                });
                mainHandler.post(() -> { if (listener != null) listener.onSuccess(true); });
            } catch (Exception e) {
                e.printStackTrace();
                mainHandler.post(() -> { if (listener != null) listener.onFailure(false); });
            }
        });
    }

    // Transaksi untuk mengupdate expense: refund uang dari expense lama, deduct uang untuk expense baru, lalu update expense
    public void updateExpenseTransaction( Expense oldExpense,  Expense newExpense, OnTransactionCompleteListener listener) {
        executorService.execute(() -> {
            try {
                db.runInTransaction(() -> {
                    cashDao.updateCashWithTransaction(oldExpense.getExpense_cash_id(), oldExpense.getExpense_amount(), "Refund old expense on update id : "+oldExpense.get_id());
                    cashDao.updateCashWithTransaction(newExpense.getExpense_cash_id(), -newExpense.getExpense_amount(), "Deduct new expense on update id : "+newExpense.get_id());
                    expenseDao.updateExpense(newExpense);
                });
                mainHandler.post(() -> { if (listener != null) listener.onSuccess(true); });
            } catch (Exception e) {
                e.printStackTrace();
                mainHandler.post(() -> { if (listener != null) listener.onFailure(false); });
            }
        });
    }

    public LiveData<ExpenseWithCash> getExpenseWithCashByIdLive(long Id) {
        return expenseDao.getExpenseWithCashByIdLive(Id);
    }



//
//    public void updateExpense( Expense expense, OnTransactionCompleteListener listener) {
//        executorService.execute(() -> {
//            try {
//                expenseDao.updateExpense(expense);
//                mainHandler.post(() -> { if (listener != null) listener.onSuccess(true); });
//            } catch (Exception e) {
//                e.printStackTrace();
//                mainHandler.post(() -> { if (listener != null) listener.onFailure(false); });
//            }
//        });
//    }

//    public void deleteExpense(Expense expense,  OnTransactionCompleteListener listener) {
//        executorService.execute(() -> {
//            try {
//                expenseDao.deleteExpense(expense);
//                mainHandler.post(() -> { if (listener != null) listener.onSuccess(true); });
//            } catch (Exception e) {
//                e.printStackTrace();
//                mainHandler.post(() -> { if (listener != null) listener.onFailure(false); });
//            }
//        });
//    }

    // Transaksi untuk menghapus expense: refund saldo kas (kembalikan uang) lalu hapus expense

}
