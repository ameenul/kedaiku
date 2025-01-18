package com.example.kedaiku.UI.piutang_menu;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.kedaiku.R;
import com.example.kedaiku.entites.SaleWithDetails;
import com.example.kedaiku.helper.CsvHelper;
import com.example.kedaiku.helper.DateHelper;
import com.example.kedaiku.helper.FormatHelper;
import com.example.kedaiku.viewmodel.PiutangViewModel;

import java.io.FileOutputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PiutangListActivity extends AppCompatActivity {

    private PiutangViewModel piutangViewModel;
    private PiutangAdapter adapter;
    private RecyclerView recyclerViewPiutang;
    private TextView textViewTotalUnpaid,textViewSelectedDate;
    private Spinner spinnerFilter,spinnerSearch;
    private TextView buttonExportCsv; // Menggunakan TextView sebagai tombol sederhana
    private EditText editTextSearch; // Tambahkan EditText Search
    private StringBuilder dateRange;
    private DateHelper dateHelper;

    private ActivityResultLauncher<Intent> createFileLauncher;
    private String csvData;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_piutang_list);
        dateRange = new StringBuilder();

        // Inisialisasi UI
        dateHelper = new DateHelper();
        
        recyclerViewPiutang = findViewById(R.id.recyclerViewPiutang);
        textViewTotalUnpaid = findViewById(R.id.textViewTotalUnpaid);
        textViewSelectedDate = findViewById(R.id.textViewSelectedDates);
        spinnerFilter = findViewById(R.id.spinnerFilter);
        spinnerSearch = findViewById(R.id.spinnerSearchType);
        buttonExportCsv = findViewById(R.id.buttonExportCsv);
        editTextSearch = findViewById(R.id.editTextSearch);

        // Setup RecyclerView dan Adapter
        recyclerViewPiutang.setLayoutManager(new LinearLayoutManager(this));
        adapter = new PiutangAdapter();
        recyclerViewPiutang.setAdapter(adapter);

        // Inisialisasi ViewModel
        piutangViewModel = new ViewModelProvider(this).get(PiutangViewModel.class);

        // Observasi LiveData khusus untuk piutang
        piutangViewModel.getFilteredSalesWithDetailsPiutang().observe(this, new Observer<List<SaleWithDetails>>() {
            @Override
            public void onChanged(List<SaleWithDetails> salesWithDetails) {
                double totalUnpaidSum = 0;
                for (SaleWithDetails sale : salesWithDetails) {
                    double unpaid = sale.getSaleTotal() - sale.getSalePaid();
                    if (unpaid > 0) {
                        totalUnpaidSum += unpaid;
                    }
                }
                textViewTotalUnpaid.setText("Total Belum Terbayar: " + FormatHelper.formatCurrency(totalUnpaidSum));
                adapter.setSalesList(salesWithDetails);
            }
        });

        // Konfigurasi spinnerFilter
        spinnerFilter.setOnItemSelectedListener(dateHelper.spinnerSelectedListener(PiutangListActivity.this

                ,piutangViewModel, dateRange, textViewSelectedDate, spinnerFilter));


        spinnerSearch.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String filter = parent.getItemAtPosition(position).toString();
                piutangViewModel.setIsFilterByCustomer(filter.equalsIgnoreCase("Nama Pelanggan"));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });


        // Tambahkan TextWatcher untuk EditText Search
        editTextSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String searchQuery = s.toString();
                piutangViewModel.setTransactionNameFilter(searchQuery);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });


        // Inisialisasi ActivityResultLauncher untuk membuat dokumen
        createFileLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        if (data != null) {
                            Uri uri = data.getData();
                            writeCsvDataToUri(uri);
                        }
                    }
                }
        );

        // Tombol Export CSV
        buttonExportCsv.setOnClickListener(v -> exportDataToCsv());
    }

    private void exportDataToCsv() {
        List<SaleWithDetails> currentSales = adapter.getSalesList();
        if (currentSales == null || currentSales.isEmpty()) {
            Toast.makeText(this, "Tidak ada data untuk diekspor", Toast.LENGTH_SHORT).show();
            return;
        }

        // Tentukan header dan baris data untuk CSV
        String[] headers = {"Sale ID", "Transaction Name", "Date", "Total", "Paid", "Payment Type", "Customer Name"};
        List<String[]> rows = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());

        for (SaleWithDetails sale : currentSales) {
            String saleId = String.valueOf(sale.getSaleId());
            String transactionName = sale.getSaleTransactionName();
            String dateStr = sdf.format(new Date(sale.getSaleDate()));
            String total = String.valueOf(sale.getSaleTotal());
            String paid = String.valueOf(sale.getSalePaid());
            String paymentType = sale.getSale().getSale_payment_type() == 1 ? "Cash" : "Piutang";
            String customerName = sale.getCustomerName();

            String[] row = { saleId, transactionName, dateStr, total, paid, paymentType, customerName };
            rows.add(row);
        }

        String judul = "";
        if(!dateHelper.lastFilter.equals("Pilih Tanggal")) {
            judul = ("List Piutang_search = " +editTextSearch.getText()+" "+ DateHelper.getDescStartEndDate(DateHelper.calculateDateRange(dateHelper.lastFilter)) + " \n");

        }
        else{
            judul = ("List Piutang_search = " +editTextSearch.getText()+" "+ dateRange.toString()+ " \n");

        }
        csvData = CsvHelper.convertToCsv(headers, rows,judul);

        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("text/csv");
        intent.putExtra(Intent.EXTRA_TITLE, "piutang_export.csv");
        createFileLauncher.launch(intent);
    }

    private void writeCsvDataToUri(Uri uri) {
        try {
            OutputStream outputStream = getContentResolver().openOutputStream(uri);
            outputStream.write(csvData.getBytes());
            outputStream.close();
            Toast.makeText(this, "Data berhasil disimpan", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Gagal menyimpan data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }




    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Lepas listener untuk spinnerFilter
        spinnerFilter.setOnItemSelectedListener(null);

        // Lepas listener untuk spinnerSearch
        spinnerSearch.setOnItemSelectedListener(null);
    }


}


