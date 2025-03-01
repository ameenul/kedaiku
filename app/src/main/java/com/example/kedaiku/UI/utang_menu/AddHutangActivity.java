package com.example.kedaiku.UI.utang_menu;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.example.kedaiku.R;
import com.example.kedaiku.entites.Cash;   // Misal Anda punya entitas Cash
import com.example.kedaiku.entites.Creditor;
import com.example.kedaiku.entites.Debt;   // Entitas Debt
import com.example.kedaiku.entites.Product;
import com.example.kedaiku.helper.FormatHelper;
import com.example.kedaiku.repository.OnTransactionCompleteListener;
import com.example.kedaiku.viewmodel.CashViewModel;  // Misal untuk spinner kas
import com.example.kedaiku.viewmodel.CreditorViewModel;
import com.example.kedaiku.viewmodel.DebtViewModel;  // ViewModel hutang

import java.util.List;

public class AddHutangActivity extends AppCompatActivity {

    private AutoCompleteTextView autoCompleteCreditor;
    private TextView textViewFormatRupiah;
    private EditText editTextJumlahHutang, editTextTransactionName;
    private Spinner spinnerKas;
    private CheckBox checkBoxTambahkanKas;
    private Button buttonSimpanHutang;

    private CashViewModel cashViewModel;      // Misal untuk load data kas
    private DebtViewModel debtViewModel;
    private CreditorViewModel creditorViewModel;// Untuk menyimpan hutang

    private long selectedCashId = 0;          // ID kas terpilih
    private List<Cash> listCash;              // Daftar kas
    private Creditor selectedCreditor;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_hutang);

        initUI();
        setupViewModel();
       setupSpinnerKas();
        setupAutoCompleteCreditor();
       setupEvents();
    }



    private void initUI() {
        autoCompleteCreditor    = findViewById(R.id.autoCompleteCreditor);
        editTextJumlahHutang    = findViewById(R.id.editTextJumlahHutang);
        spinnerKas              = findViewById(R.id.spinnerKas);
        checkBoxTambahkanKas    = findViewById(R.id.checkBoxTambahkanKas);
        editTextTransactionName = findViewById(R.id.editTextTransactionName);
        buttonSimpanHutang      = findViewById(R.id.buttonSimpanHutang);
        textViewFormatRupiah = findViewById(R.id.TextViewFormatRupiah);
        spinnerKas.setEnabled(checkBoxTambahkanKas.isChecked());
    }

    private void setupViewModel() {
        // Inisialisasi ViewModel
        debtViewModel = new ViewModelProvider(this).get(DebtViewModel.class);
        cashViewModel = new ViewModelProvider(this).get(CashViewModel.class);
        creditorViewModel = new ViewModelProvider(this).get(CreditorViewModel.class);


    }

    /**
     * Memuat daftar kas dari CashViewModel, lalu populasi ke spinner
     */
    private void setupSpinnerKas() {
        // Observasi data kas
        cashViewModel.getAllCash().observe(this, new Observer<List<Cash>>() {
            @Override
            public void onChanged(List<Cash> cashes) {
                if (cashes != null) {
                    listCash = cashes;
                    // Tampilkan ke spinner, misal pakai ArrayAdapter
                    // (Anda perlu custom adapter jika mau menampilkan nama kas dsb.)
                    // Sebagai contoh sederhana:
                    //   Buat array string berisi nama kas
                    String[] arrCashNames = new String[cashes.size()];
                    for (int i = 0; i < cashes.size(); i++) {
                        arrCashNames[i] = cashes.get(i).getCash_name();
                    }
                    // ArrayAdapter
                    //   android.R.layout.simple_spinner_item -> tampilan bawaan
                    //   android.R.layout.simple_spinner_dropdown_item -> dropdown
                    android.widget.ArrayAdapter<String> adapter =
                            new android.widget.ArrayAdapter<>(AddHutangActivity.this,
                                    android.R.layout.simple_spinner_item, arrCashNames);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinnerKas.setAdapter(adapter);
                }
            }
        });

        // Listener ketika user memilih kas
        spinnerKas.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                if (listCash != null && position < listCash.size()) {
                    selectedCashId = listCash.get(position).getId();
                }
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });
    }

    private void setupAutoCompleteCreditor() {

           creditorViewModel.getAllCreditors().observe(this, creditors -> {
                       if (creditors != null && !creditors.isEmpty()) {
                           // Buat array nama kreditur untuk adapter
                           String[] creditorNames = new String[creditors.size()];
                           for (int i = 0; i < creditors.size(); i++) {
                               creditorNames[i] = creditors.get(i).getCreditor_name();
                           }
                           // Buat adapter dengan layout bawaan untuk dropdown
                           ArrayAdapter<Creditor> adapter = new ArrayAdapter<>(AddHutangActivity.this,
                                   android.R.layout.simple_dropdown_item_1line, creditors);
                           autoCompleteCreditor.setAdapter(adapter);



                       }
                   });

        creditorViewModel.getSelectedCreditor().observe(this, creditor -> {
            if (creditor != null) {
                selectedCreditor = creditor;
                autoCompleteCreditor.setText(creditor.getCreditor_name());

            }
        });


        // Tangani klik item untuk mendapatkan data kreditor yang dipilih
        autoCompleteCreditor.setOnItemClickListener((parent, view, position, id) -> {
            // Ambil nama yang dipilih
            selectedCreditor =  (Creditor) parent.getAdapter().getItem(position);
            creditorViewModel.setSelectedCreditor(selectedCreditor);
        });
    }

    private void setupEvents() {
        // Tombol Simpan
        buttonSimpanHutang.setOnClickListener(v -> {
            simpanHutang();
        });

        setupCheckBoxTambahKas();
        setupEdiTextJumlahHutang();
    }

    private void setupCheckBoxTambahKas() {

        checkBoxTambahkanKas.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // Jika checkbox diceklis, aktifkan spinner
                spinnerKas.setEnabled(isChecked);

                // (Opsional) Ubah tampilan visual spinner
                if (isChecked) {
                    spinnerKas.setAlpha(1f);
                } else {
                    spinnerKas.setAlpha(0.5f); // Menandakan bahwa spinner nonaktif
                }
            }
        });


    }



    private void setupEdiTextJumlahHutang()
    {
        editTextJumlahHutang.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String jumlahText = editTextJumlahHutang.getText().toString();
                double jumlah = jumlahText.isEmpty() ? 0 : Double.parseDouble(jumlahText);
                textViewFormatRupiah.setText(FormatHelper.formatCurrency(jumlah));
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

    }

    private void simpanHutang() {
        // Ambil input user
        String creditorName = autoCompleteCreditor.getText().toString().trim();
        String jumlahHutang = editTextJumlahHutang.getText().toString().trim();
        String transactionName = editTextTransactionName.getText().toString().trim();
        boolean isTambahkanKas = checkBoxTambahkanKas.isChecked();

        // Validasi: pastikan nama kreditor diisi
        if (TextUtils.isEmpty(creditorName)) {
            autoCompleteCreditor.setError("Nama kreditor tidak boleh kosong");
            return;
        }
        // Validasi: pastikan kreditor terpilih dari AutoComplete (selectedCreditor tidak null)
        if (selectedCreditor == null) {
            autoCompleteCreditor.setError("Kreditor tidak terdaftar");
            return;
        }
        // Validasi: pastikan jumlah hutang diisi dan formatnya valid
        if (TextUtils.isEmpty(jumlahHutang)) {
            editTextJumlahHutang.setError("Jumlah hutang tidak boleh kosong");
            return;
        }
        double hutangValue;
        try {
            hutangValue = Double.parseDouble(jumlahHutang);
        } catch (NumberFormatException e) {
            editTextJumlahHutang.setError("Format angka tidak valid");
            return;
        }
        if (hutangValue <= 0) {
            editTextJumlahHutang.setError("Jumlah hutang harus > 0");
            return;
        }

        // Buat objek Debt; gunakan ID kreditor dari selectedCreditor
        Debt newDebt = new Debt(
                selectedCreditor.get_id(), // gunakan ID dari selectedCreditor
                hutangValue,               // debt_quantity
                0.0,                       // debt_paid (awal belum ada pembayaran)
                "",                        // debt_history_paid (kosong)
                transactionName,
                System.currentTimeMillis() // debt_date: hari ini (millis)
        );

        // Jika checkbox dicentang, update kas juga
        if (isTambahkanKas) {
            debtViewModel.addDebtAndUpdateCash(selectedCashId, hutangValue, newDebt, new OnTransactionCompleteListener() {
                @Override
                public void onSuccess(boolean status) {

                        Toast.makeText(AddHutangActivity.this, "Hutang berhasil disimpan dan kas diperbarui", Toast.LENGTH_SHORT).show();
                        finish();  // Tutup activity setelah transaksi sukses

                }

                @Override
                public void onFailure(boolean status) {

                        Toast.makeText(AddHutangActivity.this, "Gagal menyimpan hutang", Toast.LENGTH_LONG).show();

                }
            });
        } else {
            // Jika tidak, hanya insert hutang saja
            debtViewModel.insertDebt(newDebt);
            Toast.makeText(this, "Hutang berhasil disimpan", Toast.LENGTH_SHORT).show();
            finish();  // Tutup activity
        }
    }


}
