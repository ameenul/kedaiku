package com.example.kedaiku.UI.piutang_menu;

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
import com.example.kedaiku.entites.ParsingHistory;
import com.example.kedaiku.entites.SaleWithDetails;
import com.example.kedaiku.helper.DateHelper;
import com.example.kedaiku.helper.DateRangeCallback;
import com.example.kedaiku.helper.FormatHelper;
import com.example.kedaiku.viewmodel.PiutangViewModel;

import java.util.ArrayList;
import java.util.List;

public class RiwayatPembayaranActivity extends AppCompatActivity {

    private PiutangViewModel piutangViewModel;
    private TextView textViewTransactionName, textViewCustomer, textViewPiutang, textViewPaid, textViewUnpaid, textViewSelectedDate;
    private Spinner spinnerTimeFilter;
    private RecyclerView recyclerViewPaymentHistory;
    private RiwayatPembayaranAdapter paymentHistoryAdapter;
    private long saleId;
    private SaleWithDetails currentSaleDetails;

    // Simpan daftar riwayat pembayaran penuh di sini.
    // Nanti saat filter berubah, kita akan mem-filter data ini.
    private List<ParsingHistory> allPaidHistories = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_riwayat_pembayaran);

        // Inisialisasi UI
        textViewTransactionName = findViewById(R.id.textViewTransactionName);
        textViewCustomer = findViewById(R.id.textViewCustomer);
        textViewPiutang = findViewById(R.id.textViewPiutang);
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
        paymentHistoryAdapter = new RiwayatPembayaranAdapter();
        recyclerViewPaymentHistory.setAdapter(paymentHistoryAdapter);

        // Ambil saleId dari intent
        saleId = getIntent().getLongExtra("sale_id", -1);
        if (saleId == -1) {
            Toast.makeText(this, "Sale ID tidak valid", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Inisialisasi ViewModel
        piutangViewModel = new ViewModelProvider(this).get(PiutangViewModel.class);

        // Observasi data SaleWithDetails berdasarkan saleId
        piutangViewModel.getSaleWithDetailsByIdLive(saleId).observe(this, new Observer<SaleWithDetails>() {
            @Override
            public void onChanged(SaleWithDetails saleWithDetails) {
                if (saleWithDetails != null) {
                    currentSaleDetails = saleWithDetails;
                    // 1. Update summary (nama transaksi, customer, dsb.)
                    updateSummaryUI(currentSaleDetails);

                    // 2. Parsing data riwayat pembayaran penuh (tanpa filter)
                    String jsonPaidHistory = currentSaleDetails.getDetailSale().getSale_paid_history();
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
                    Toast.makeText(RiwayatPembayaranActivity.this, "Data tidak tersedia", Toast.LENGTH_SHORT).show();
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
     * Update summary UI (nama transaksi, nama customer, piutang, terbayar, dll.)
     */
    private void updateSummaryUI(SaleWithDetails saleWithDetails) {
        textViewTransactionName.setText("Transaction: " + saleWithDetails.getSaleTransactionName());
        textViewCustomer.setText("Customer: " + saleWithDetails.getCustomerName());

        double totalSales = saleWithDetails.getSaleTotal();
        double totalPaid = saleWithDetails.getSalePaid();
        double totalUnpaid = totalSales - totalPaid;

        textViewPiutang.setText("Jumlah Piutang: " + FormatHelper.formatCurrency(totalSales));
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
