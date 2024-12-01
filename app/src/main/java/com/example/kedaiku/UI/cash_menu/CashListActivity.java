package com.example.kedaiku.UI.cash_menu;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.kedaiku.R;

import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.widget.TextView;

import androidx.annotation.Nullable;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.example.kedaiku.entites.Cash;
import com.example.kedaiku.viewmodel.CashViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class CashListActivity extends AppCompatActivity implements CashListAdapter.OnCashItemClickListener {

    private RecyclerView recyclerView;
    private TextView totalTextView;
    private FloatingActionButton fabAddCash;
    private CashViewModel cashViewModel;
    private CashListAdapter adapter;
    private List<Cash> cashList;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cash_list);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        totalTextView = findViewById(R.id.totalTextView);
        fabAddCash = findViewById(R.id.fab_add_cash);

        cashViewModel = new ViewModelProvider(this).get(CashViewModel.class);

        adapter = new CashListAdapter(this, this);
        recyclerView.setAdapter(adapter);

        cashViewModel.getAllCash().observe(this, new Observer<List<Cash>>() {
            @Override
            public void onChanged(List<Cash> cashes) {
                cashList = cashes;
                adapter.setCashList(cashes);

                double totalCash = 0;
                for (Cash cash : cashes) {
                    totalCash += cash.getCash_value();
                }
                totalTextView.setText("Total Kas: " + formatCurrency(totalCash));
            }
        });

        fabAddCash.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showAddCashDialog();
            }
        });
    }

    private String formatCurrency(double value) {
        NumberFormat numberFormat = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
        return numberFormat.format(value);
    }

    // 1. Menampilkan Dialog "Add Cash" di Tengah Layar
    private void showAddCashDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Tambah Kas");
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_cash, null);
        builder.setView(dialogView);

        EditText editTextCashName = dialogView.findViewById(R.id.editTextCashName);
        EditText editTextCashValue = dialogView.findViewById(R.id.editTextCashValue);
        TextView textViewFormattedCashValue = dialogView.findViewById(R.id.textViewFormattedCashValue);

        editTextCashValue.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }

            @Override
            public void afterTextChanged(Editable s) {
                String input = s.toString().replaceAll("[^\\d]", "");
                if (!input.isEmpty()) {
                    try {
                        long parsed = Long.parseLong(input);
                        String formatted = NumberFormat.getCurrencyInstance(new Locale("id", "ID")).format(parsed);
                        textViewFormattedCashValue.setText(formatted);
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                        textViewFormattedCashValue.setText("Rp 0");
                    }
                } else {
                    textViewFormattedCashValue.setText("Rp 0");
                }
            }
        });


        builder.setPositiveButton("Tambah", null);
        builder.setNegativeButton("Batal", null);

        AlertDialog dialog = builder.create();
        dialog.setOnShowListener(dialogInterface -> {
            Button buttonAdd = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            buttonAdd.setOnClickListener(v -> {
                String cashName = editTextCashName.getText().toString().trim();
                String cashValueStr = editTextCashValue.getText().toString().replaceAll("[Rp.,\\s]", "");

                if (cashName.isEmpty()) {
                    editTextCashName.setError("Nama kas tidak boleh kosong");
                    return;
                }

                if (cashValueStr.isEmpty()) {
                    editTextCashValue.setError("Jumlah kas tidak boleh kosong");
                    return;
                }

                int cashValue = Integer.parseInt(cashValueStr);
                Cash newCash = new Cash(cashName, cashValue);
                cashViewModel.insert(newCash);
                dialog.dismiss();
            });
        });

        dialog.show();
    }

    // 2. Masuk ke CashFlowActivity saat Item Cash Diklik
    @Override
    public void onCashItemClick(Cash cash) {
        Intent intent = new Intent(this, CashFlowActivity.class);
        intent.putExtra("cash_id", cash.getId());
        intent.putExtra("cash_name", cash.getCash_name()); // Tambahkan nama kas
        intent.putExtra("cash_saldo", cash.getCash_value());

        startActivity(intent);

    }

    // 3. Menangani Menu More Actions
    @Override
    public void onIncreaseCashClick(Cash cash) {
        showModifyCashDialog(cash, true);
    }

    @Override
    public void onDecreaseCashClick(Cash cash) {
        showModifyCashDialog(cash, false);
    }

    @Override
    public void onTransferCashClick(Cash cash) {
        showTransferCashDialog(cash);
    }

    @Override
    public void onRenameCashClick(Cash cash) {
        showRenameCashDialog(cash);
    }

    @Override
    public void onViewDetailClick(Cash cash) {
        onCashItemClick(cash);
    }

    @Override
    public void onDeleteCashClick(Cash cash) {
        new AlertDialog.Builder(this)
                .setTitle("Hapus Kas")
                .setMessage("Apakah Anda yakin ingin menghapus kas ini?")
                .setPositiveButton("Ya", (dialog, which) -> cashViewModel.delete(cash))
                .setNegativeButton("Tidak", null)
                .show();
    }

    // Dialog Tambah/Kurangi Kas
    private void showModifyCashDialog(Cash cash, boolean isIncrease) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(isIncrease ? "Tambah Kas" : "Kurangi Kas");
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_modify_cash, null);
        builder.setView(dialogView);

        EditText editTextAmount = dialogView.findViewById(R.id.editTextAmount);
        TextView textViewFormattedAmount = dialogView.findViewById(R.id.textViewFormattedAmount);
        EditText editTextDescription = dialogView.findViewById(R.id.editTextDescription);

        editTextAmount.addTextChangedListener(new TextWatcher() {
            private String current = "";
            @Override
            public void beforeTextChanged(CharSequence s, int start, int before, int count) { }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }
            @Override
            public void afterTextChanged(Editable s) {
                String input = s.toString().replaceAll("[^\\d]", "");
                if (!input.isEmpty()) {
                    try {
                        long parsed = Long.parseLong(input);
                        String formatted = NumberFormat.getCurrencyInstance(new Locale("id", "ID")).format(parsed);
                        textViewFormattedAmount.setText(formatted);
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                        textViewFormattedAmount.setText("Rp 0");
                    }
                } else {
                    textViewFormattedAmount.setText("Rp 0");
                }

            }
        });

        builder.setPositiveButton("Simpan", null);
        builder.setNegativeButton("Batal", null);

        AlertDialog dialog = builder.create();
        dialog.setOnShowListener(dialogInterface -> {
            Button buttonSave = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            buttonSave.setOnClickListener(v -> {
                String amountStr = editTextAmount.getText().toString().replaceAll("[Rp.,\\s]", "");
                String description = editTextDescription.getText().toString().trim();

                if (amountStr.isEmpty()) {
                    editTextAmount.setError("Jumlah tidak boleh kosong");
                    return;
                }

                if (description.isEmpty()) {
                    editTextDescription.setError("Deskripsi tidak boleh kosong");
                    return;
                }

                double amount = Integer.parseInt(amountStr);
                if (!isIncrease) {
                    amount = -amount;
                }

                // Update cash_value dan insert ke cash_flow dalam satu transaksi
                cashViewModel.updateCashWithTransaction(cash, amount, description);
                dialog.dismiss();
            });
        });

        dialog.show();
    }

    // Dialog Ubah Nama Kas
    private void showRenameCashDialog(Cash cash) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Ubah Nama Kas");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setText(cash.getCash_name());
        builder.setView(input);

        builder.setPositiveButton("Simpan", (dialog, which) -> {
            String newName = input.getText().toString().trim();
            if (!newName.isEmpty()) {
                cash.setCash_name(newName);
                cashViewModel.update(cash);
            }
        });

        builder.setNegativeButton("Batal", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    // Dialog Transfer Kas
    private void showTransferCashDialog(Cash sourceCash) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Transfer Kas");
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_transfer_cash, null);
        builder.setView(dialogView);

        Spinner spinnerTargetCash = dialogView.findViewById(R.id.spinnerTargetCash);
        EditText editTextAmount = dialogView.findViewById(R.id.editTextAmount);
        TextView textViewFormattedAmount = dialogView.findViewById(R.id.textViewFormattedAmount);
        EditText editTextDescription = dialogView.findViewById(R.id.editTextDescription);

        // Filter cashList untuk menghapus sourceCash
        List<Cash> targetCashList = new ArrayList<>(cashList);
        targetCashList.remove(sourceCash);

        ArrayAdapter<Cash> spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, targetCashList);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTargetCash.setAdapter(spinnerAdapter);

        editTextAmount.addTextChangedListener(new TextWatcher() {
            private String current = "";
            @Override
            public void beforeTextChanged(CharSequence s, int start, int before, int count) { }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }
            @Override
            public void afterTextChanged(Editable s) {
                String input = s.toString().replaceAll("[^\\d]", "");
                if (!input.isEmpty()) {
                    try {
                        long parsed = Long.parseLong(input);
                        String formatted = NumberFormat.getCurrencyInstance(new Locale("id", "ID")).format(parsed);
                        textViewFormattedAmount.setText(formatted);
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                        textViewFormattedAmount.setText("Rp 0");
                    }
                } else {
                    textViewFormattedAmount.setText("Rp 0");
                }



            }
        });

        builder.setPositiveButton("Transfer", null);
        builder.setNegativeButton("Batal", null);

        AlertDialog dialog = builder.create();
        dialog.setOnShowListener(dialogInterface -> {
            Button buttonTransfer = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            buttonTransfer.setOnClickListener(v -> {
                String amountStr = editTextAmount.getText().toString().replaceAll("[Rp.,\\s]", "");
                String description = editTextDescription.getText().toString().trim();
                Cash targetCash = (Cash) spinnerTargetCash.getSelectedItem();

                if (amountStr.isEmpty()) {
                    editTextAmount.setError("Jumlah tidak boleh kosong");
                    return;
                }

                if (description.isEmpty()) {
                    editTextDescription.setError("Deskripsi tidak boleh kosong");
                    return;
                }

                if (targetCash == null) {
                    // Harusnya tidak terjadi, tetapi ditangani untuk berjaga-jaga
                    return;
                }

                double amount = Integer.parseInt(amountStr);

                // Transfer kas antara sumber dan tujuan, update cash_flow dalam satu transaksi
                cashViewModel.transferCash(sourceCash, targetCash, amount, description);
                dialog.dismiss();
            });
        });

        dialog.show();
    }
}
