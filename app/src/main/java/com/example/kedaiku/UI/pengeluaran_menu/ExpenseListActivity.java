package com.example.kedaiku.UI.pengeluaran_menu;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.kedaiku.R;
import com.example.kedaiku.entites.Cash;
import com.example.kedaiku.entites.Expense;
import com.example.kedaiku.entites.ExpenseWithCash;
import com.example.kedaiku.helper.CsvHelper;
import com.example.kedaiku.helper.DateHelper;
import com.example.kedaiku.helper.FormatHelper;
import com.example.kedaiku.repository.OnTransactionCompleteListener;
import com.example.kedaiku.viewmodel.CashViewModel;
import com.example.kedaiku.viewmodel.ExpenseViewModel;

import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicLong;

public class ExpenseListActivity extends AppCompatActivity {

    private Spinner spinnerExpenseFilter;
    private EditText etSearchExpense;
    private Button btnExportCsv;
    private TextView selectedDate;
    private RecyclerView rvExpenseList;
    private Button btnAddExpense;

    private ExpenseViewModel expenseViewModel;
    private CashViewModel cashViewModel;
    private ExpenseAdapter expenseAdapter;

    private DateHelper dateHelper;
    private StringBuilder dateRange;

    private ActivityResultLauncher<Intent> createFileLauncher;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expense_list);

        initUI();
        setupRecyclerView();
        setupViewModels();
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
        spinnerExpenseFilter = findViewById(R.id.spinnerExpenseFilter);
        etSearchExpense = findViewById(R.id.etSearchExpense);
        btnExportCsv = findViewById(R.id.btnExportCsv);
        selectedDate = findViewById(R.id.selectedDate);
        rvExpenseList = findViewById(R.id.rvExpenseList);
        btnAddExpense = findViewById(R.id.btnAddExpense);
        dateHelper = new DateHelper();
        dateRange = new StringBuilder();
    }

    private void setupRecyclerView() {
        rvExpenseList.setLayoutManager(new LinearLayoutManager(this));
        expenseAdapter = new ExpenseAdapter(this, null, new ExpenseAdapter.OnExpenseItemClickListener() {
            @Override
            public void onDeleteExpense(ExpenseWithCash expense) {
                expenseViewModel.deleteExpenseTransaction(expense.getExpense(), new OnTransactionCompleteListener() {
                    @Override
                    public void onSuccess(boolean status) {
                        Toast.makeText(ExpenseListActivity.this, "Expense deleted", Toast.LENGTH_SHORT).show();
                    }
                    @Override
                    public void onFailure(boolean status) {
                        Toast.makeText(ExpenseListActivity.this, "Delete failed", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onUpdateExpense(ExpenseWithCash expense) {
                showExpenseDialog(expense);
            }
        });
        rvExpenseList.setAdapter(expenseAdapter);
    }

    private void setupViewModels() {
        expenseViewModel = new ViewModelProvider(this).get(ExpenseViewModel.class);
        cashViewModel = new ViewModelProvider(this).get(CashViewModel.class);
        expenseViewModel.getFilteredExpenses().observe(this, expenses -> {
            expenseAdapter.setData(expenses);
        });



    }

    private void setupEvents() {
        // Setup spinner filter dengan resource array (misalnya, "filter_options")
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.filter_options, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerExpenseFilter.setOnItemSelectedListener(dateHelper.spinnerSelectedListener(this

                ,expenseViewModel, dateRange, selectedDate, spinnerExpenseFilter));

        // Setup EditText search: update query pada ViewModel setiap perubahan teks
        etSearchExpense.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                expenseViewModel.setSearchQuery(s.toString());
            }
            @Override public void afterTextChanged(Editable s) { }
        });

        // Export CSV button event (implementasikan export sesuai kebutuhan)
        btnExportCsv.setOnClickListener(v -> {
            // Panggil metode export CSV (implementasi export CSV dapat disesuaikan)
            exportDataToCsv();
        });

        btnAddExpense.setOnClickListener(v -> showExpenseDialog(null));
    }

    // Contoh metode exportDataToCsv() yang dapat Anda modifikasi sesuai dengan kebutuhan Anda


    // Dialog tambah/update expense dengan DatePicker dan Spinner untuk kas
    private void showExpenseDialog(@Nullable ExpenseWithCash expenseWithCash) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(expenseWithCash == null ? "Tambah Pengeluaran" : "Update Pengeluaran");

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        int padding = (int)(16 * getResources().getDisplayMetrics().density);
        layout.setPadding(padding, padding, padding, padding);
        AtomicLong selectedDate = new AtomicLong(System.currentTimeMillis());

        // Field Tanggal dengan DatePicker
        final EditText etDate = new EditText(this);
        etDate.setHint("Tanggal (dd/MM/yyyy)");
        etDate.setFocusable(false);
        etDate.setClickable(true);
        if(expenseWithCash == null) {
            String today = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date());
            etDate.setText(today);
        } else {
            etDate.setText( FormatHelper.getDescDate(expenseWithCash.getExpense().getExpense_date()) );
        }
        etDate.setOnClickListener(v -> {
//            Calendar calendar = Calendar.getInstance();
//            try {
//                String currentDate = etDate.getText().toString();
//                if(!currentDate.isEmpty()){
//                    calendar.setTime(new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(currentDate));
//                }
//            } catch (ParseException e) {
//                e.printStackTrace();
//            }
//            int year = calendar.get(Calendar.YEAR);
//            int month = calendar.get(Calendar.MONTH);
//            int day = calendar.get(Calendar.DAY_OF_MONTH);
//            new android.app.DatePickerDialog(ExpenseListActivity.this, (view, selectedYear, selectedMonth, selectedDay) -> {
//                String selectedDate = String.format(Locale.getDefault(), "%02d/%02d/%04d", selectedDay, selectedMonth + 1, selectedYear);
//                etDate.setText(selectedDate);
//            }, year, month, day).show();
            Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, year1, month1, dayOfMonth) -> {
                Calendar selectedCalendar = Calendar.getInstance();
                selectedCalendar.set(year1, month1, dayOfMonth);
                selectedCalendar.set(Calendar.HOUR_OF_DAY, 0);
                selectedCalendar.set(Calendar.MINUTE, 0);
                selectedCalendar.set(Calendar.SECOND, 0);
                selectedCalendar.set(Calendar.MILLISECOND, 0);
                selectedDate.set(selectedCalendar.getTimeInMillis());

                String Date = dayOfMonth + "/" + (month1 + 1) + "/" + year1;
                etDate.setText(Date);
                // EditText editTextDate = findViewById(R.id.editTextDate);
                // editTextDate.setText(selectedDate);
            }, year, month, day);
            datePickerDialog.show();
        });


        layout.addView(etDate);

        // Field Deskripsi
        final EditText etDescription = new EditText(this);
        etDescription.setHint("Deskripsi Pengeluaran");
        layout.addView(etDescription);

        // Field Jumlah Pengeluaran
        final EditText etAmount = new EditText(this);
        etAmount.setHint("Jumlah Pengeluaran");
        etAmount.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        layout.addView(etAmount);

        // Spinner untuk Nama Kas, diisi dari CashViewModel
        final Spinner spinnerCash = new Spinner(this);
        layout.addView(spinnerCash);

        final List<Cash>[] cashListHolder = new List[1];
        cashViewModel.getAllCash().observe(this, cashList -> {
            cashListHolder[0] = cashList;
            if(cashList != null && !cashList.isEmpty()){
                List<String> cashNames = new ArrayList<>();
                for (Cash c : cashList) {
                    cashNames.add(c.getCash_name());
                }
                ArrayAdapter<String> cashAdapter = new ArrayAdapter<>(ExpenseListActivity.this,
                        android.R.layout.simple_spinner_item, cashNames);
                cashAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerCash.setAdapter(cashAdapter);
                if(expenseWithCash != null && expenseWithCash.getCash() != null) {
                    int pos = cashNames.indexOf(expenseWithCash.getCash().getCash_name());
                    if(pos >= 0) spinnerCash.setSelection(pos);
                }
            }
        });

        if(expenseWithCash != null) {
            etDescription.setText(expenseWithCash.getExpense().getExpense_name());
            etAmount.setText(String.valueOf(expenseWithCash.getExpense().getExpense_amount()));
        }

        builder.setView(layout);

        builder.setPositiveButton(expenseWithCash == null ? "Tambah" : "Update", (dialog, which) -> {
            String date = etDate.getText().toString().trim();
            String description = etDescription.getText().toString().trim();
            String amountStr = etAmount.getText().toString().trim();

            if(date.isEmpty() || description.isEmpty() || amountStr.isEmpty()){
                Toast.makeText(ExpenseListActivity.this, "Semua field harus diisi", Toast.LENGTH_SHORT).show();
                return;
            }

            double amount = Double.parseDouble(amountStr);
            long cashId = 0;
            if(cashListHolder[0] != null) {
                String selectedCashName = spinnerCash.getSelectedItem().toString();
                for (Cash c : cashListHolder[0]) {
                    if(c.getCash_name().equals(selectedCashName)) {
                        cashId = c.getId();
                        break;
                    }
                }
            }
            Expense expense = new Expense(selectedDate.get(), description, cashId, amount);
            if(expenseWithCash == null) {
                expenseViewModel.insert(expense, new OnTransactionCompleteListener() {
                    @Override
                    public void onSuccess(boolean status) {
                        Toast.makeText(ExpenseListActivity.this, "Pengeluaran ditambahkan", Toast.LENGTH_SHORT).show();
                    }
                    @Override
                    public void onFailure(boolean status) {
                        Toast.makeText(ExpenseListActivity.this, "Gagal menambah pengeluaran", Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                expense.set_id(expenseWithCash.getExpense().get_id());
                expenseViewModel.updateExpenseTransaction(expenseWithCash.getExpense(), expense, new OnTransactionCompleteListener() {
                    @Override
                    public void onSuccess(boolean status) {
                        Toast.makeText(ExpenseListActivity.this, "Pengeluaran diupdate", Toast.LENGTH_SHORT).show();
                    }
                    @Override
                    public void onFailure(boolean status) {
                        Toast.makeText(ExpenseListActivity.this, "Gagal mengupdate pengeluaran", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        builder.setNegativeButton("Batal", (dialog, which) -> dialog.dismiss());
        builder.show();
    }


    private void exportDataToCsv() {
        // Ambil data expense dari adapter
        List<ExpenseWithCash> currentExpense = expenseAdapter.getData();
        if (currentExpense == null || currentExpense.isEmpty()) {
            Toast.makeText(this, "Tidak ada data untuk diekspor", Toast.LENGTH_SHORT).show();
            return;
        }

        // Tentukan header CSV untuk expense
        String[] headers = {"Expense ID", "Expense Name", "Date", "Amount", "Cash Name"};
        List<String[]> rows = new ArrayList<>();
        // Format tanggal, asumsikan expense_date disimpan sebagai long (millis)
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());

        for (ExpenseWithCash exp : currentExpense) {
            String expenseId = String.valueOf(exp.getExpense().get_id());
            String expenseName = exp.getExpense().getExpense_name();
            String dateStr = FormatHelper.getDescDate(exp.getExpense().getExpense_date());
            String amount = FormatHelper.formatCurrency(exp.getExpense().getExpense_amount());
            String cashName = exp.getCash() != null ? exp.getCash().getCash_name() : "-";

            String[] row = { expenseId, expenseName, dateStr, amount, cashName };
            rows.add(row);
        }

        // Buat judul laporan dengan menyertakan range tanggal
        String judul = "";
        if(!dateHelper.lastFilter.equalsIgnoreCase("Pilih Tanggal")) {
            judul = "List Expense_search = " + etSearchExpense.getText() + " " +
                    DateHelper.getDescStartEndDate(DateHelper.calculateDateRange(dateHelper.lastFilter)) + "\n";
        } else {
            judul = "List Expense_search = " + etSearchExpense.getText() + " " + dateRange.toString() + "\n";
        }

        csvData = CsvHelper.convertToCsv(headers, rows, judul);

        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("text/csv");
        intent.putExtra(Intent.EXTRA_TITLE, "Expense_export.csv");
        createFileLauncher.launch(intent);
    }

    private String csvData = "";

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

}
