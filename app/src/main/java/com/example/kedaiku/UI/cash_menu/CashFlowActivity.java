package com.example.kedaiku.UI.cash_menu;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.kedaiku.R;
import com.example.kedaiku.entites.CashFlow;
import com.example.kedaiku.helper.DateHelper;
import com.example.kedaiku.viewmodel.CashFlowViewModel;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.OutputStream;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class CashFlowActivity extends AppCompatActivity {

    private CashFlowViewModel cashFlowViewModel;
    private RecyclerView recyclerView;
    private CashFlowAdapter adapter;
    private long cashId;
    private String cashName;
    private TextView textViewEmptyMessage, textViewSelectedDateRange;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
    private long [] DateInMillis ;
    private TextView textViewSaldo;
    private Button buttonExportCsv;
    private String csvData;
    private ActivityResultLauncher<Intent> createFileLauncher;
    double saldo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cash_flow);

        initViews();
        textViewSaldo = findViewById(R.id.textViewSaldo);
        buttonExportCsv = findViewById(R.id.buttonExportCsv);

// Tampilkan saldo yang diterima dari Intent

        saldo = getIntent().getDoubleExtra("cash_saldo", 0);
        String formattedSaldo = NumberFormat.getCurrencyInstance(new Locale("id", "ID")).format(saldo);
        textViewSaldo.setText("Saldo: " + formattedSaldo);

// Listener untuk tombol Export CSV
        buttonExportCsv.setOnClickListener(v -> exportDataToCsv());


        cashId = getIntent().getLongExtra("cash_id", -1);
        cashName = getIntent().getStringExtra("cash_name");

        if (cashId == -1) {
            Toast.makeText(this, "Invalid cash ID", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setTitle("Cash Flow - " + cashName);

        cashFlowViewModel = new ViewModelProvider(this).get(CashFlowViewModel.class);

        // Observasi data dari ViewModel
        observeCashFlowData();

        // Muat data awal
        cashFlowViewModel.setCashId(cashId);
        cashFlowViewModel.setFilter("Semua Waktu");


        createFileLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                Uri uri = result.getData().getData();
                if (uri != null) {
                    writeCsvDataToUri(uri);
                }
            }
        });


        DateInMillis = DateHelper.calculateDateRange("Semua Waktu");

    }

    private void initViews() {
        recyclerView = findViewById(R.id.recyclerViewCashFlow);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new CashFlowAdapter(this);
        recyclerView.setAdapter(adapter);

        textViewEmptyMessage = findViewById(R.id.textViewEmptyMessage);
        textViewSelectedDateRange = findViewById(R.id.textViewSelectedDateRange);
    }

    private void observeCashFlowData() {
        cashFlowViewModel.getFilteredCashFlows().observe(this, cashFlows -> {
            updateUI(cashFlows);
        });
    }

    private void updateUI(List<CashFlow> cashFlows) {
        if (cashFlows == null || cashFlows.isEmpty()) {
            textViewEmptyMessage.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            textViewEmptyMessage.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
            adapter.setCashFlowList(cashFlows);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_cash_flow, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if(item.getItemId() != R.id.action_filter_custom){
            DateInMillis = DateHelper.calculateDateRange(item.getTitle().toString());
            //Menampilkan rentang tanggal yang dipilih
            String startDateStr = dateFormat.format(DateInMillis[0]);
            String endDateStr = dateFormat.format(DateInMillis[1]);
            textViewSelectedDateRange.setText("Periode: " + startDateStr + " - " + endDateStr);
            textViewSelectedDateRange.setVisibility(View.VISIBLE);
        }

        if (item.getItemId() == R.id.action_filter_today) {
            cashFlowViewModel.setFilter("Hari Ini");

            return true;
        } else if (item.getItemId() == R.id.action_filter_yesterday) {
            cashFlowViewModel.setFilter("Kemarin");

            return true;
        } else if (item.getItemId() == R.id.action_filter_this_month) {
            cashFlowViewModel.setFilter("Bulan Ini");

            return true;
        } else if (item.getItemId() == R.id.action_filter_last_month) {
            cashFlowViewModel.setFilter("Bulan Lalu");

            return true;
        } else if (item.getItemId() == R.id.action_filter_all_time) {
            cashFlowViewModel.setFilter("Semua Waktu");

            return true;
        } else if (item.getItemId() == R.id.action_filter_custom) {
            showDateRangeDialog();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }



    }

    private void showDateRangeDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Pilih Rentang Tanggal");

        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_date_range_picker, null);
        builder.setView(dialogView);

        Button buttonStartDate = dialogView.findViewById(R.id.buttonStartDate);
        Button buttonEndDate = dialogView.findViewById(R.id.buttonEndDate);

        final Calendar startCalendar = Calendar.getInstance();
        final Calendar endCalendar = Calendar.getInstance();

        // Listener untuk tanggal awal
        buttonStartDate.setOnClickListener(v -> {
            DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
                startCalendar.set(year, month, dayOfMonth);
                buttonStartDate.setText(dateFormat.format(startCalendar.getTime()));
            }, startCalendar.get(Calendar.YEAR), startCalendar.get(Calendar.MONTH), startCalendar.get(Calendar.DAY_OF_MONTH));
            datePickerDialog.show();
        });

        // Listener untuk tanggal akhir
        buttonEndDate.setOnClickListener(v -> {
            DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
                endCalendar.set(year, month, dayOfMonth);
                buttonEndDate.setText(dateFormat.format(endCalendar.getTime()));
            }, endCalendar.get(Calendar.YEAR), endCalendar.get(Calendar.MONTH), endCalendar.get(Calendar.DAY_OF_MONTH));
            datePickerDialog.show();
        });

        builder.setPositiveButton("Filter", (dialog, which) -> {
            if (startCalendar.getTimeInMillis() <= endCalendar.getTimeInMillis()) {
                DateInMillis =  DateHelper.fixmmssRange(startCalendar,endCalendar);

                cashFlowViewModel.setDateRangeFilter(startCalendar.getTimeInMillis(), endCalendar.getTimeInMillis());
                //Menampilkan rentang tanggal yang dipilih
                String startDateStr = dateFormat.format(DateInMillis[0]);
                String endDateStr = dateFormat.format(DateInMillis[1]);
                textViewSelectedDateRange.setText("Periode: " + startDateStr + " - " + endDateStr);
                textViewSelectedDateRange.setVisibility(View.VISIBLE);


            } else {
                Toast.makeText(this, "Tanggal akhir harus setelah atau sama dengan tanggal awal", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Batal", null);

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void exportDataToCsv() {
        // Dapatkan data dari adapter
        List<CashFlow> dataToExport = adapter.getCashFlowList();

        if (dataToExport == null || dataToExport.isEmpty()) {
            Toast.makeText(this, "Tidak ada data untuk diekspor", Toast.LENGTH_SHORT).show();
            return;
        }

        // Siapkan data CSV
        StringBuilder data = new StringBuilder();

        // Header file dengan nama dan rentang waktu (jika ada)
        if (DateInMillis != null && DateInMillis.length == 2) {
            data.append("CashFlow_" + cashName + " " + DateHelper.getDescStartEndDate(DateInMillis) + "\n");
        } else {
            data.append("CashFlow_" + cashName + "\n");
        }
        String formattedSaldo = NumberFormat.getCurrencyInstance(new Locale("id", "ID")).format(saldo);
        data.append("Saldo : " +formattedSaldo + "\n");
        data.append("\n");

        // Header kolom
        data.append("ID,Tanggal,Deskripsi,Jumlah\n");

        // Format data cash flow
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy HH:mm:ss", Locale.getDefault());
        for (CashFlow item : dataToExport) {
            String date = dateFormat.format(item.getCash_date()); // Format tanggal
            data.append(item.getId()).append(",");
            data.append(date).append(",");
            data.append(item.getCash_description().replace(",", " ")).append(","); // Hindari koma dalam deskripsi
            data.append(item.getCash_value()).append("\n");
        }

        // Simpan data CSV ke variabel
        csvData = data.toString();

        // Buat intent untuk memilih lokasi penyimpanan file
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("text/csv");
        intent.putExtra(Intent.EXTRA_TITLE, "CashFlow_" + cashName + ".csv");
        createFileLauncher.launch(intent);
    }



    private void writeCsvDataToUri(Uri uri) {
        try {
            OutputStream outputStream = getContentResolver().openOutputStream(uri);
            outputStream.write(csvData.getBytes());
            outputStream.close();
            Toast.makeText(this, "Data berhasil diekspor", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Gagal menyimpan data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }



}
