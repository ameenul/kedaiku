package com.example.kedaiku.UI.utang_menu;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.*;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.kedaiku.R;
import com.example.kedaiku.entites.Cash;
import com.example.kedaiku.entites.DebtWithCreditor;
import com.example.kedaiku.entites.SaleWithDetails;
import com.example.kedaiku.helper.CsvHelper;
import com.example.kedaiku.helper.DateHelper;
import com.example.kedaiku.helper.FormatHelper;
import com.example.kedaiku.repository.OnTransactionCompleteListener;
import com.example.kedaiku.viewmodel.CashViewModel;
import com.example.kedaiku.viewmodel.DebtViewModel;

import java.io.OutputStream;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DaftarHutangActivity extends AppCompatActivity {
    private Spinner spinnerSearchType, spinnerFilter;
    private EditText editTextSearch;
    private Button buttonExportCsv, buttonDaftarKreditur, buttonAddDebt;
    private TextView textViewSelectedDates, textViewTotalHutang, textViewTotalUnpaid;
    private RecyclerView recyclerViewPiutang;
    private HutangAdapter hutangAdapter;
    private DebtViewModel debtViewModel;
    private CashViewModel cashViewModel;
    private DateHelper dateHelper;
    private StringBuilder dateRange;
    private List<Cash> listCash;
    android.widget.ArrayAdapter<String> adapter;
    private String csvData;
    private ActivityResultLauncher<Intent> createFileLauncher;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_daftar_hutang);
        initUI();
        setupRecyclerView();
        setupViewModel();
        setupEvents();

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

    }

    private void initUI() {
        spinnerSearchType = findViewById(R.id.spinnerSearchType);
        spinnerFilter = findViewById(R.id.spinnerFilter);
        editTextSearch = findViewById(R.id.editTextSearch);
        buttonExportCsv = findViewById(R.id.buttonExportCsv);
        buttonDaftarKreditur = findViewById(R.id.buttonDaftarKreditur);
        buttonAddDebt = findViewById(R.id.buttonAddDebt);
        textViewSelectedDates = findViewById(R.id.textViewSelectedDates);
        textViewTotalHutang = findViewById(R.id.textViewTotaH);
        textViewTotalUnpaid = findViewById(R.id.textViewTotalUnpaid);
        recyclerViewPiutang = findViewById(R.id.recyclerViewPiutang);
        dateHelper = new DateHelper();
        dateRange = new StringBuilder();

    }

    private void setupRecyclerView() {
        recyclerViewPiutang.setLayoutManager(new LinearLayoutManager(this));
        hutangAdapter = new HutangAdapter();
        recyclerViewPiutang.setAdapter(hutangAdapter);

        hutangAdapter.setOnItemClickListener(new HutangAdapter.OnItemClickListener() {
            @Override
            public void onItemClicked(DebtWithCreditor debt) {
                Intent intent = new Intent(DaftarHutangActivity.this, RiwayatPembayaranHutangActivity.class);
                intent.putExtra("hutang_id", debt.debt.get_id());
                startActivity(intent);
            }

            @Override
            public void onHubungiCreditor(DebtWithCreditor debt) {
                Intent intent = new Intent(DaftarHutangActivity.this, CreditorDetailActivity.class);
                intent.putExtra("creditor_id", debt.creditor.get_id());
                startActivity(intent);

            }

            @Override
            public void onRiwayatPembayaran(DebtWithCreditor debt) {
                Intent intent = new Intent(DaftarHutangActivity.this, RiwayatPembayaranHutangActivity.class);
                intent.putExtra("hutang_id", debt.debt.get_id());
                startActivity(intent);

            }

            @Override
            public void onBayar(DebtWithCreditor debt) {
                showPaymentDialog(debt);
            }

            @Override
            public void onHapus(DebtWithCreditor debt) {
                AlertDialog.Builder builder = new AlertDialog.Builder(DaftarHutangActivity.this);
                builder.setMessage("Apakah Anda yakin ingin menghapus hutang ini?")
                        .setPositiveButton("Hapus", (dialog, which) -> {
                            // Panggil ViewModel untuk menghapus hutang
                            debtViewModel.deleteDebt(debt.debt);

                            // Tampilkan pesan sukses
                            Toast.makeText(DaftarHutangActivity.this, "Hutang berhasil dihapus", Toast.LENGTH_SHORT).show();
                        })
                        .setNegativeButton("Batal", (dialog, which) -> dialog.dismiss());

                // Tampilkan dialog
                builder.create().show();
            }

        });
    }

    private void setupViewModel() {
        debtViewModel = new ViewModelProvider(this).get(DebtViewModel.class);
        debtViewModel.getFilteredDebts().observe(this,new Observer<List<DebtWithCreditor>>() {
                    @Override
                    public void onChanged(List<DebtWithCreditor> debts) {
                        hutangAdapter.setDebtList(debts);
                        updateSummary(debts);
                    }
                }


        );
        
        cashViewModel = new ViewModelProvider(this).get(CashViewModel.class);

    }

    private void setupEvents() {
        buttonAddDebt.setOnClickListener(v -> {
            startActivity(new Intent(this, AddHutangActivity.class));
        });

        buttonDaftarKreditur.setOnClickListener(v -> {
            startActivity(new Intent(this, CreditorListActivity.class));
        });

        // Tambahkan TextWatcher untuk EditText Search
        editTextSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String searchQuery = s.toString();
                debtViewModel.setSearchQuery(searchQuery);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        spinnerSearchType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String filter = parent.getItemAtPosition(position).toString();
                debtViewModel.setIsFilterByCreditor(filter.equalsIgnoreCase("Nama Kreditur"));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        spinnerFilter.setOnItemSelectedListener(dateHelper.spinnerSelectedListener(this

                ,debtViewModel, dateRange, textViewSelectedDates, spinnerFilter));

        buttonExportCsv.setOnClickListener(v -> exportDataToCsv());

    }

    private void exportDataToCsv() {
        List<DebtWithCreditor> currentDebt = hutangAdapter.getDebtList();
        if (currentDebt == null || currentDebt.isEmpty()) {
            Toast.makeText(this, "Tidak ada data untuk diekspor", Toast.LENGTH_SHORT).show();
            return;
        }

        // Tentukan header dan baris data untuk CSV
        String[] headers = {"Transaksi ID", "Transaction Name", "Date", "Total", "Paid", "Creditor Name"};
        List<String[]> rows = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());

        for (DebtWithCreditor debt : currentDebt) {
            String saleId = String.valueOf(debt.debt.get_id());
            String transactionName = debt.debt.getDebt_note();
            String dateStr = sdf.format(new Date(debt.debt.getDebt_date()));
            String total = String.valueOf(debt.debt.getDebt_quantity());
            String paid = String.valueOf(debt.debt.getDebt_paid());
            String creditorName = debt.creditor.getCreditor_name();

            String[] row = { saleId, transactionName, dateStr, total, paid, creditorName };
            rows.add(row);
        }

        String judul = "";
        if(!dateHelper.lastFilter.equals("Pilih Tanggal")) {
            judul = ("List Hutang_search = " +editTextSearch.getText()+" "+ DateHelper.getDescStartEndDate(DateHelper.calculateDateRange(dateHelper.lastFilter)) + " \n");

        }
        else{
            judul = ("List Hutang_search = " +editTextSearch.getText()+" "+ dateRange.toString()+ " \n");

        }
        csvData = CsvHelper.convertToCsv(headers, rows,judul);

        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("text/csv");
        intent.putExtra(Intent.EXTRA_TITLE, "Hutang_export.csv");
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

    private void updateSummary(List<DebtWithCreditor> debts) {
        double totalHutang = 0.0;
        double totalUnpaid = 0.0;
        for (DebtWithCreditor d : debts) {
            totalHutang += d.debt.getDebt_quantity();
            double sisa = d.debt.getDebt_quantity() - d.debt.getDebt_paid();
            if (sisa > 0) {
                totalUnpaid += sisa;
            }
        }
        NumberFormat format = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
        textViewTotalHutang.setText("Total Hutang: " + format.format(totalHutang));
        textViewTotalUnpaid.setText("Total Belum Terbayar: " + format.format(totalUnpaid));
    }



    private void showPaymentDialog(DebtWithCreditor debt) {
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_pay_debt, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView);

        Spinner spinnerCash = dialogView.findViewById(R.id.spinnerKas);
        TextView textViewUnpaidAmount = dialogView.findViewById(R.id.textViewUnpaidAmount);
        TextView textViewPaymentError = dialogView.findViewById(R.id.textViewPaymentError);
        TextView textViewFormatRupiah = dialogView.findViewById(R.id.textViewAmountToPay);
        EditText editTextAmount = dialogView.findViewById(R.id.editTextAmountPaid);
        Button buttonConfirm = dialogView.findViewById(R.id.buttonPay);

        double unpaidAmount = debt.debt.getDebt_quantity() - debt.debt.getDebt_paid();
        textViewUnpaidAmount.setText("Belum Terbayar: " + FormatHelper.formatCurrency(unpaidAmount));

        AlertDialog dialog = builder.create();
        final long[] cashId = {0};

        Observer<List<Cash>> cashObserver = cashes -> {
            if (cashes != null) {
                listCash = cashes;
                String[] arrCashNames = new String[cashes.size()];
                for (int i = 0; i < cashes.size(); i++) {
                    arrCashNames[i] = cashes.get(i).getCash_name();
                }

                adapter = new ArrayAdapter<>(DaftarHutangActivity.this,
                        android.R.layout.simple_spinner_item, arrCashNames);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerCash.setAdapter(adapter);
            }
        };

        // Observe data hanya saat dialog terbuka
        cashViewModel.getAllCash().observe(this, cashObserver);

        // Hapus observer saat dialog ditutup
        dialog.setOnDismissListener(dialogInterface -> {
            cashViewModel.getAllCash().removeObserver(cashObserver);
        });

        spinnerCash.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (listCash != null && position < listCash.size()) {
                    cashId[0] = listCash.get(position).getId();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Tambahkan TextWatcher untuk format Rupiah
        editTextAmount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String jumlahText = editTextAmount.getText().toString();
                double jumlah = jumlahText.isEmpty() ? 0 : Double.parseDouble(jumlahText);
                textViewFormatRupiah.setText("Jumlah yang akan dibayar: " + FormatHelper.formatCurrency(jumlah));
                textViewPaymentError.setVisibility(View.INVISIBLE);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        buttonConfirm.setOnClickListener(v -> {
            String amountStr = editTextAmount.getText().toString().trim().replaceAll("[Rp,.]", "");

            if (!amountStr.isEmpty()) {
                double amount = Double.parseDouble(amountStr);
                if (amount > 0 && amount <= unpaidAmount) {
                    debtViewModel.payDebt(debt.debt.get_id(), amount, cashId[0], new OnTransactionCompleteListener() {
                        @Override
                        public void onSuccess(boolean status) {
                            dialog.dismiss();
                            Toast.makeText(DaftarHutangActivity.this, "Pembayaran berhasil", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onFailure(boolean status) {
                            Toast.makeText(DaftarHutangActivity.this, "Gagal melakukan pembayaran", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    textViewPaymentError.setVisibility(View.VISIBLE);
                    textViewPaymentError.setText("Jumlah pembayaran tidak valid atau melebihi yang belum terbayar.");
                }
            }
        });

        dialog.show();
    }

    // Tambahkan ini agar observer juga dihapus jika aktivitas dihancurkan


    @Override
    protected void onDestroy() {
        super.onDestroy();
        cashViewModel.getAllCash().removeObservers(this);
    }

}
