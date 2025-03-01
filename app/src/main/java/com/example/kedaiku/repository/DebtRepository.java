package com.example.kedaiku.repository;

import android.app.Application;
import android.os.Handler;
import android.util.Log;

import androidx.lifecycle.LiveData;

import com.example.kedaiku.database.AppDatabase;
import com.example.kedaiku.entites.Debt;
import com.example.kedaiku.entites.DebtWithCreditor;
import com.example.kedaiku.entites.ParsingHistory;
import com.example.kedaiku.entites.SaleWithDetails;
import com.example.kedaiku.helper.FormatHelper;
import com.example.kedaiku.ifaceDao.CashDao;
import com.example.kedaiku.ifaceDao.CreditorDao;
import com.example.kedaiku.ifaceDao.DebtDao;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DebtRepository {
    private final DebtDao debtDao;
    private final CashDao cashDao;
    private final CreditorDao creditorDao;
    private final AppDatabase db;
    private final ExecutorService executorService;
    private final Handler mainHandler = new Handler();

    public DebtRepository(Application application) {
        db = AppDatabase.getDatabase(application);
        debtDao = db.debtDao();
        cashDao = db.cashDao();
        creditorDao = db.creditorDao();

                // Pastikan di AppDatabase sudah ada method cashDao()
        executorService = Executors.newSingleThreadExecutor();
    }

    public LiveData<List<Debt>> getAllDebts() {
        return debtDao.getAllDebts();
    }

    public void insert(Debt debt) {
        executorService.execute(() -> debtDao.insert(debt));
    }

    public void update(Debt debt) {
        executorService.execute(() -> debtDao.update(debt));
    }

    public void delete(Debt debt) {
        executorService.execute(() -> debtDao.delete(debt));
    }

    public LiveData<DebtWithCreditor> getDebtWithCreditorByIdLive(long saleId) {
        return debtDao.getDebtWithCreditorByIdLive(saleId);
    }

    // Metode untuk mengambil nama kreditor berdasarkan creditor_id
    public String fetchCreditorName(long creditorId) {
        return creditorDao.getCreditorNameById(creditorId);
    }



    /**
     * Metode untuk melakukan transaksi yang memasukkan Debt baru dan memperbarui kas (uang masuk)
     * secara atomik menggunakan runInTransaction().
     *
     * @param newDebt      Objek Debt yang akan dimasukkan.
     * @param cashId       ID kas yang akan diperbarui.
     * @param amount       Jumlah uang masuk yang akan ditambahkan ke kas.
     * @param listener     Listener untuk menangani hasil transaksi.
     */
    public void addDebtAndUpdateCash(Debt newDebt, long cashId, double amount, OnTransactionCompleteListener listener) {
        executorService.execute(() -> {
            try {
                // Menjalankan seluruh operasi dalam satu transaksi.
                db.runInTransaction(() -> {
                    // 1. Update kas: Tambahkan uang masuk ke kas dengan keterangan
                    cashDao.updateCashWithTransaction(cashId, amount, "Hutang Masuk dengan ID " + newDebt.get_id());
                    // 2. Insert debt baru ke tabel hutang
                    debtDao.insert(newDebt);
                });
                // Jika transaksi berhasil, panggil listener.onSuccess()
                mainHandler.post(() -> {
                    listener.onSuccess(true);
                });
            } catch (Exception e) {
                e.printStackTrace();
                // Jika terjadi kesalahan, panggil listener.onFailure()
                mainHandler.post(() -> {
                    listener.onFailure(false);
                });
            }
        });
    }

    /**
     * Mengembalikan LiveData list Debt berdasarkan filter waktu dan pencarian.
     *
     * @param startDate         Tanggal mulai (millis)
     * @param endDate           Tanggal akhir (millis)
     * @param searchString      String pencarian (bisa berdasarkan debt_note atau creditor_name)
     * @param filterByCreditor  Jika true, pencarian dilakukan berdasarkan nama creditor;
     *                          jika false, berdasarkan debt_note.
     * @return LiveData list Debt yang sudah difilter
     */
    public LiveData<List<DebtWithCreditor>> getFilteredDebtsWithSearch(long startDate, long endDate, String searchString, boolean filterByCreditor) {
        return debtDao.getFilteredDebtsWithSearch(startDate, endDate, searchString, filterByCreditor);
    }

    /**
     * Transaksi pembayaran hutang:
     * - Mengambil data Debt berdasarkan debtId.
     * - Meng-parse field debt_history_paid (JSON) menjadi List<ParsingHistory>.
     * - Menambahkan entri pembayaran baru.
     * - Menghitung total pembayaran baru dan meng-update debt_paid.
     * - Update record Debt dan mencatat transaksi ke kas.
     *
     * @param debtId        ID dari Debt yang akan dibayar
     * @param cashId        ID kas yang akan diupdate
     * @param paymentAmount Jumlah pembayaran yang ditambahkan
     * @param listener      Listener untuk menangani hasil transaksi
     */
    public void payDebt(long debtId, long cashId, double paymentAmount, OnTransactionCompleteListener listener) {
        executorService.execute(() -> {

            try {
                db.runInTransaction(() -> {
                    // 1. Ambil record Debt secara sinkron
                    Debt debt = debtDao.getDebtByIdSync(debtId);
                    if (debt == null) {
                        throw new IllegalArgumentException("Debt tidak ditemukan untuk ID: " + debtId);
                    }

                    // 2. Parse debt_history_paid menjadi List<ParsingHistory>
                    List<ParsingHistory> paidHistories = FormatHelper.parsePaidHistory(debt.getDebt_history_paid());

                    // 3. Tambahkan entri pembayaran baru
                    ParsingHistory newHistory = new ParsingHistory(System.currentTimeMillis(), paymentAmount);
                    paidHistories.add(newHistory);

                    // 4. Konversi kembali list ke JSON
                    String updatedPaidHistoryJson = FormatHelper.convertPaidHistoriesToJson(paidHistories);

                    // 5. Hitung total pembayaran baru
                    double newDebtPaid = 0.0;
                    for (ParsingHistory history : paidHistories) {
                        newDebtPaid += history.getPaid();
                    }

                    // 6. Update Debt: set debt_paid dan debt_history_paid
                    debt.setDebt_paid(newDebtPaid);
                    debt.setDebt_history_paid(updatedPaidHistoryJson);
                    debtDao.update(debt);

                    // 7. Update kas: catat transaksi masuk ke kas
                    cashDao.updateCashWithTransaction(cashId, -paymentAmount, "Pembayaran Hutang untuk Debt ID: " + debtId);
                });

                mainHandler.post(() -> {
                    listener.onSuccess(true);
                });
            } catch (IllegalArgumentException e) {
                Log.e("DebtRepository", "Transaksi bayar hutang gagal karena argumen tidak valid: " + e.getMessage());
                e.printStackTrace();
                mainHandler.post(() -> {
                    listener.onFailure(false);
                });
            } catch (Exception e) {
                Log.e("DebtRepository", "Transaksi bayar hutang gagal: " + Log.getStackTraceString(e));
                e.printStackTrace();
                mainHandler.post(() -> {
                    listener.onFailure(false);
                });
            }
        });
    }

}
