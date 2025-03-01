package com.example.kedaiku.UI.utang_menu;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.kedaiku.R;
import com.example.kedaiku.entites.DebtWithCreditor;
import com.example.kedaiku.entites.ParsingHistory;
import com.example.kedaiku.helper.DateHelper;
import com.example.kedaiku.helper.DateRangeCallback;
import com.example.kedaiku.helper.FormatHelper;
import com.example.kedaiku.viewmodel.DebtViewModel;

import java.util.ArrayList;
import java.util.List;

public class RiwayatPembayaranHutangActivity extends AppCompatActivity {

    private DebtViewModel DebtViewModel;
    private TextView textViewTransactionName, textViewCreditor, textViewHutang, textViewPaid, textViewUnpaid, textViewSelectedDate;
    private Spinner spinnerTimeFilter;
    private RecyclerView recyclerViewPaymentHistory;
    private RiwayatPembayaranHutangAdapter paymentHistoryAdapter;
    private long debtId;
    private DebtWithCreditor currentDebtCustomer;

    // Simpan daftar riwayat pembayaran penuh di sini.
    // Nanti saat filter berubah, kita akan mem-filter data ini.
    private List<ParsingHistory> allPaidHistories = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_riwayat_pembayaran_hutang);

        // Inisialisasi UI
        textViewTransactionName = findViewById(R.id.textViewTransactionName);
        textViewCreditor = findViewById(R.id.textViewCreditor);
        textViewHutang = findViewById(R.id.textViewHutang);
        textViewPaid = findViewById(R.id.textViewPaid);
        textViewUnpaid = findViewById(R.id.textViewUnpaid);
        spinnerTimeFilter = findViewById(R.id.spinnerTimeFilter);
        textViewSelectedDate = findViewById(R.id.textViewSelectedDates);
        recyclerViewPaymentHistory = findViewById(R.id.recyclerViewPaymentHistory);

        // Setup Spinner dengan array dari resources (filter_options)
        ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter.createFromResource(
                this,
                R.array.filter_options,
                android.R.layout.simple_spinner_item
        );
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTimeFilter.setAdapter(spinnerAdapter);

        // Setup RecyclerView
        recyclerViewPaymentHistory.setLayoutManager(new LinearLayoutManager(this));
        paymentHistoryAdapter = new RiwayatPembayaranHutangAdapter();
        recyclerViewPaymentHistory.setAdapter(paymentHistoryAdapter);

        // Ambil Id dari intent
        debtId = getIntent().getLongExtra("hutang_id", -1);
        if (debtId == -1) {
            Toast.makeText(this, "Hutang ID tidak valid", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Inisialisasi ViewModel
        DebtViewModel = new ViewModelProvider(this).get(DebtViewModel.class);


        DebtViewModel.getDebtWithCreditorByIdLive(debtId).observe(this, new Observer<DebtWithCreditor>() {
            @Override
            public void onChanged(DebtWithCreditor debtCustomer) {
                if (debtCustomer != null) {
                    currentDebtCustomer = debtCustomer;
                    // 1. Update summary (nama transaksi, creditor, dsb.)
                    updateSummaryUI(currentDebtCustomer);

                    // 2. Parsing data riwayat pembayaran penuh (tanpa filter)
                    String jsonPaidHistory = currentDebtCustomer.debt.getDebt_history_paid();
                    allPaidHistories = FormatHelper.parsePaidHistory(jsonPaidHistory);

                    // 3. Tampilkan data pertama kali (default "Semua Waktu")
                    updatePaymentHistory(allPaidHistories);
                }
            }
        });

        // Listener untuk spinnerTimeFilter untuk menerapkan filter berdasarkan rentang tanggal
        spinnerTimeFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (allPaidHistories == null) {
                    Toast.makeText(RiwayatPembayaranHutangActivity.this, "Data tidak tersedia", Toast.LENGTH_SHORT).show();
                    return;
                }
                applyFilter(position);

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Tidak melakukan apa-apa
            }
        });
    }

    /**
     * Update summary UI (nama transaksi, nama creditor, Hutang, terbayar, dll.)
     */
    private void updateSummaryUI(DebtWithCreditor debtWithCreditor) {
        textViewTransactionName.setText("Transaction: " + debtWithCreditor.debt.getDebt_note());
        textViewCreditor.setText("Creditor: " + debtWithCreditor.creditor.getCreditor_name());

        double debtQuantity = debtWithCreditor.debt.getDebt_quantity();
        double totalPaid = debtWithCreditor.debt.getDebt_paid();
        double totalUnpaid = debtQuantity - totalPaid;

        textViewHutang.setText("Jumlah Hutang: " + FormatHelper.formatCurrency(debtQuantity));
        textViewPaid.setText("Jumlah Terbayar: " + FormatHelper.formatCurrency(totalPaid));
        textViewUnpaid.setText("Jumlah Belum Terbayar: " + FormatHelper.formatCurrency(totalUnpaid));
    }

    /**
     * Update RecyclerView dengan daftar histori pembayaran (ParsingHistory)
     */
    private void updatePaymentHistory(List<ParsingHistory> newList) {
        paymentHistoryAdapter.setHistoryList(newList);
    }

    private void applyFilter(int spinnerPosition) {
        if (allPaidHistories == null || allPaidHistories.isEmpty()) return;


        String selectedFilter = (String) spinnerTimeFilter.getItemAtPosition(spinnerPosition);

        if (selectedFilter.equalsIgnoreCase("Pilih Tanggal")) {
            // Gunakan DateHelper
            DateHelper.pickCustomDateRange(this, new DateRangeCallback() {
                @Override
                public void onDateRangeSelected(long startMillis, long endMillis) {
                    // Filter data
                    List<ParsingHistory> filtered = FormatHelper.filterPaidHistoryByDateRange(
                            allPaidHistories,
                            startMillis,
                            endMillis
                    );
                    updatePaymentHistory(filtered);
                    spinnerTimeFilter.setSelection(0);
                    long[] dateRange =  new long[]{startMillis, endMillis};
                    String dateRangeText = "Tanggal Terpilih: " +DateHelper.getDescStartEndDateShort(dateRange);
                    textViewSelectedDate.setText(dateRangeText);

                }

                @Override
                public void onDateRangeCanceled() {
                    // Opsional: misalnya set spinner ke "Semua Waktu" atau kembalikan state sebelumnya
                    spinnerTimeFilter.setSelection(0); // Atau apapun
                }
            });
        }
        else if (!("Pilih Salah Satu :").equalsIgnoreCase(selectedFilter)){
            // Filter "Hari Ini", "Kemarin", dsb.
            long[] dateRange = DateHelper.calculateDateRange(selectedFilter);
            List<ParsingHistory> filtered = FormatHelper.filterPaidHistoryByDateRange(
                    allPaidHistories,
                    dateRange[0],
                    dateRange[1]
            );
            updatePaymentHistory(filtered);
            String dateRangeText = "Tanggal Terpilih: " +selectedFilter;
            textViewSelectedDate.setText(dateRangeText);
        }
    }
}
