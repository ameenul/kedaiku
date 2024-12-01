package com.example.kedaiku.UI.inventory_menu;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.kedaiku.R;
import com.example.kedaiku.entites.Purchase;
import com.example.kedaiku.entites.Product;
import com.example.kedaiku.repository.ProductRepository;
import com.example.kedaiku.viewmodel.PurchaseViewModel;
import com.example.kedaiku.viewmodel.ProductViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONObject;

import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class PurchaseFragment extends Fragment {

    private PurchaseAdapter adapter;
    private PurchaseViewModel purchaseViewModel;
    private ProductViewModel productViewModel;

    private Spinner spinnerFilter;
    private Button buttonExportCsv;
    private FloatingActionButton fabAddPurchase;

    private ActivityResultLauncher<Intent> createFileLauncher;
    private String csvData;

    private List<Product> productList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_purchase, container, false);
        RecyclerView recyclerView = view.findViewById(R.id.recyclerViewPurchase);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new PurchaseAdapter();

        adapter.setOnDeleteClickListener(purchase -> {
            new AlertDialog.Builder(getContext())
                    .setTitle("Konfirmasi Hapus")
                    .setMessage("Apakah Anda yakin ingin membatalkan pembelian ini?")
                    .setPositiveButton("Ya", (dialog, which) -> {
                        productViewModel.deletePurchaseTransaction(purchase, new ProductRepository.OnTransactionCompleteListener() {
                            @Override
                            public void onSuccess(String message) {
                                Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onFailure(String errorMessage) {
                                Toast.makeText(getContext(), errorMessage, Toast.LENGTH_SHORT).show();
                            }
                        });
                    })
                    .setNegativeButton("Tidak", null)
                    .show();
        });

        recyclerView.setAdapter(adapter);

        purchaseViewModel = new ViewModelProvider(this).get(PurchaseViewModel.class);
        productViewModel = new ViewModelProvider(this).get(ProductViewModel.class);

        spinnerFilter = view.findViewById(R.id.spinnerFilter);
        buttonExportCsv = view.findViewById(R.id.buttonExportCsv);
        fabAddPurchase = view.findViewById(R.id.fabAddPurchase);

        setupSpinner();

        // Mengambil daftar produk untuk digunakan dalam adapter
        productViewModel.getAllProducts().observe(getViewLifecycleOwner(), products -> {
            productList = products;
            adapter.setProductList(products);
        });

        // Mengambil daftar pembelian dan menampilkannya
        purchaseViewModel.getAllPurchases().observe(getViewLifecycleOwner(), purchases -> {
            adapter.setPurchaseList(purchases);
        });

        // Inisialisasi ActivityResultLauncher untuk ekspor CSV
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

        // Listener untuk tombol Export CSV
        buttonExportCsv.setOnClickListener(v -> exportDataToCsv());

        // Listener untuk FloatingActionButton
        fabAddPurchase.setOnClickListener(v -> showAddPurchaseDialog());

        return view;
    }

    private void setupSpinner() {
        ArrayAdapter<CharSequence> adapterSpinner = ArrayAdapter.createFromResource(
                getContext(),
                R.array.filter_options,
                android.R.layout.simple_spinner_item
        );
        adapterSpinner.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFilter.setAdapter(adapterSpinner);

        spinnerFilter.setSelection(0);

        spinnerFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view,int position,long id) {
                String filter = getResources().getStringArray(R.array.filter_options)[position];
                if ("Pilih Tanggal".equals(filter)) {
                    showDateRangePicker();
                } else {
                    // Implementasikan fungsi filter sesuai kebutuhan
                    // Misalnya, filter berdasarkan hari ini, minggu ini, bulan ini, dll.
                    Toast.makeText(getContext(), "Filter: " + filter, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void showDateRangePicker() {
        Calendar calendar = Calendar.getInstance();

        DatePickerDialog startDatePicker = new DatePickerDialog(
                getContext(),
                (DatePicker view, int year, int month, int dayOfMonth) -> {
                    Calendar startCalendar = Calendar.getInstance();
                    startCalendar.set(year, month, dayOfMonth, 0, 0, 0);
                    startCalendar.set(Calendar.MILLISECOND, 0);
                    long startDate = startCalendar.getTimeInMillis();

                    DatePickerDialog endDatePicker = new DatePickerDialog(
                            getContext(),
                            (DatePicker endView, int endYear, int endMonth, int endDayOfMonth) -> {
                                Calendar endCalendar = Calendar.getInstance();
                                endCalendar.set(endYear, endMonth, endDayOfMonth, 23, 59, 59);
                                endCalendar.set(Calendar.MILLISECOND, 999);
                                long endDate = endCalendar.getTimeInMillis();

                                // Implementasikan filter berdasarkan rentang tanggal
                                Toast.makeText(getContext(), "Filter dari " + startDate + " sampai " + endDate, Toast.LENGTH_SHORT).show();

                            },
                            calendar.get(Calendar.YEAR),
                            calendar.get(Calendar.MONTH),
                            calendar.get(Calendar.DAY_OF_MONTH)
                    );

                    endDatePicker.setTitle("Pilih Tanggal Akhir");
                    endDatePicker.show();
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );

        startDatePicker.setTitle("Pilih Tanggal Mulai");
        startDatePicker.show();
    }

    private void exportDataToCsv() {
        List<Purchase> dataToExport = adapter.getPurchaseList();
        if (dataToExport == null || dataToExport.isEmpty()) {
            Toast.makeText(getContext(), "Tidak ada data untuk diekspor", Toast.LENGTH_SHORT).show();
            return;
        }

        // Siapkan data CSV
        StringBuilder data = new StringBuilder();
        data.append("ID,Produk,Total\n");

        for (Purchase item : dataToExport) {
            String productName = adapter.getProductNameById(item.getProduct_id());

            // Hitung total dari data JSON
            try {
                JSONObject jsonObject = new JSONObject(item.getPurchase_detail());
                double price = jsonObject.getDouble("product_price");
                double qty = jsonObject.getDouble("product_qty");
                double total = price * qty;

                data.append(item.get_id()).append(",");
                data.append(productName).append(",");
                data.append(total).append("\n");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // Simpan data CSV dalam variabel
        csvData = data.toString();

        // Buat intent untuk memilih lokasi penyimpanan
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("text/csv");
        intent.putExtra(Intent.EXTRA_TITLE, "Purchases.csv");
        createFileLauncher.launch(intent);
    }

    private void writeCsvDataToUri(Uri uri) {
        try {
            OutputStream outputStream = getContext().getContentResolver().openOutputStream(uri);
            outputStream.write(csvData.getBytes());
            outputStream.close();
            Toast.makeText(getContext(), "Data berhasil disimpan", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Gagal menyimpan data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void showAddPurchaseDialog() {
        // Tampilkan dialog untuk menambah pembelian
        AddPurchaseDialogFragment dialogFragment = new AddPurchaseDialogFragment();
        dialogFragment.show(getChildFragmentManager(), "AddPurchaseDialog");
    }
}
