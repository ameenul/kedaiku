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
import android.widget.TextView;
import android.widget.Toast;

import com.example.kedaiku.R;
import com.example.kedaiku.entites.Purchase;
import com.example.kedaiku.entites.Product;
import com.example.kedaiku.entites.PurchaseWithProduct;
import com.example.kedaiku.helper.DateHelper;
import com.example.kedaiku.helper.FormatHelper;
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
    private TextView textViewSelectedDates;


    private ActivityResultLauncher<Intent> createFileLauncher;
    private String csvData;
    private String dateRange;
    private String lastFilter = "Semua Waktu";

    private List<Product> productList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_purchase, container, false);
        RecyclerView recyclerView = view.findViewById(R.id.recyclerViewPurchase);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new PurchaseAdapter();

        adapter.setOnItemClickListener(purchaseWithProduct -> {
            // Tampilkan dialog detail
            showDetailDialog(purchaseWithProduct);
        });


        adapter.setOnDeleteClickListener(purchase -> {
            new AlertDialog.Builder(getContext())
                    .setTitle("Konfirmasi Hapus")
                    .setMessage("Apakah Anda yakin ingin membatalkan pembelian ini? Cash Akan dikembalikan dan Stock akan berkurang")
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
        textViewSelectedDates = view.findViewById(R.id.textViewSelectedDates);

        lastFilter="Semua Waktu";
        setupSpinner();

        purchaseViewModel.getFilteredPurchasesWithProductName().observe(getViewLifecycleOwner(), purchaseWithProducts -> {
            // Pastikan adapter Anda memiliki metode untuk mengatur data dari PurchaseWithProduct
            // Misalnya, buat metode setPurchaseWithProductList(purchaseWithProducts)
            adapter.setPurchaseWithProductList(purchaseWithProducts);
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



        spinnerFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view,int position,long id) {
                lastFilter = getResources().getStringArray(R.array.filter_options)[position];
                if ("Pilih Tanggal".equals(lastFilter)) {
                    showDateRangePicker();
                }  else if(position==0) {

                } else {
                    // Jika user pilih "Semua Waktu"
                    purchaseViewModel.setFilter(lastFilter);

                    textViewSelectedDates.setText("Tanggal Terpilih: "+lastFilter);
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

                                purchaseViewModel.setDateRangeFilter(startDate, endDate);

                                // Format rentang tanggal
                                SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy HH:mm:ss", Locale.getDefault());
                                String dateRangeText = "Tanggal Terpilih: " +
                                        dateFormat.format(startCalendar.getTime()) + " - " +
                                        dateFormat.format(endCalendar.getTime());
                                dateRange = dateFormat.format(startCalendar.getTime()) + " - " +
                                        dateFormat.format(endCalendar.getTime());
                                textViewSelectedDates.setText(dateRangeText);

                                // Setelah memilih rentang tanggal, kembali ke "Semua Waktu"
                                spinnerFilter.setSelection(0);
                                lastFilter = "Pilih Tanggal";

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
        List<PurchaseWithProduct> dataToExport = adapter.getPurchaseWithProductList();
        if (dataToExport == null || dataToExport.isEmpty()) {
            Toast.makeText(getContext(), "Tidak ada data untuk diekspor", Toast.LENGTH_SHORT).show();
            return;
        }

        // Siapkan data CSV
        StringBuilder data = new StringBuilder();

        // Tentukan judul berdasarkan filter
        if (!"Pilih Tanggal".equals(lastFilter) && !"Semua Waktu".equals(lastFilter)) {
            // Jika filter bukan "Semua Waktu" dan bukan "Pilih Tanggal"
            long[] dateRangeArr = DateHelper.calculateDateRange(lastFilter);
            data.append("Purchases_").append(DateHelper.getDescStartEndDate(dateRangeArr)).append(" \n");
        } else if ("Pilih Tanggal".equals(lastFilter)) {
            data.append("Purchases_").append(dateRange).append("\n");
        } else {
            // Semua Waktu
            data.append("Purchases - Semua Waktu\n");
        }

        data.append("\n");
        data.append("ID,Produk,Total\n");

        for (PurchaseWithProduct item : dataToExport) {
            // Ambil nama produk langsung dari item.product_name
            // Hitung total dari JSON di purchase_detail
            try {
                JSONObject jsonObject = new JSONObject(item.purchase_detail);
                double price = jsonObject.getDouble("product_price");
                double qty = jsonObject.getDouble("product_qty");
                double total = price * qty;

                data.append(item._id).append(",");
                data.append(item.product_name).append(",");
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

    private void showDetailDialog(PurchaseWithProduct purchaseWithProduct) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Detail Pembelian");

        // Buat tampilan custom untuk dialog
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_purchase_detail, null);
        builder.setView(dialogView);

        TextView textViewId = dialogView.findViewById(R.id.textViewDetailId);
        TextView textViewProductName = dialogView.findViewById(R.id.textViewDetailProductName);
        TextView textViewPrice = dialogView.findViewById(R.id.textViewDetailPrice);
        TextView textViewQuantity = dialogView.findViewById(R.id.textViewDetailQuantity);
        TextView textViewTotal = dialogView.findViewById(R.id.textViewDetailTotal);
        TextView textViewCash = dialogView.findViewById(R.id.textViewDetailCash);

        // Ambil data dari purchaseWithProduct
        textViewId.setText("ID: " + purchaseWithProduct._id);
        textViewProductName.setText("Produk: " + purchaseWithProduct.product_name);

        double price = 0;
        double qty = 0;
        String cashname="";
        try {
            JSONObject jsonObject = new JSONObject(purchaseWithProduct.purchase_detail);
            price = jsonObject.getDouble("product_price");
            qty = jsonObject.getDouble("product_qty");
            cashname = jsonObject.getString("cash_name");
        } catch (Exception e) {
            e.printStackTrace();
        }

        double total = price * qty;
        textViewPrice.setText("Harga: " + FormatHelper.formatCurrency(price));
        textViewQuantity.setText("Jumlah: " + qty);
        textViewTotal.setText("Total: " + FormatHelper.formatCurrency(total));
        textViewCash.setText("Kas: " + cashname); // Atau jika butuh nama kas, ubah logika sesuai data kas

        builder.setPositiveButton("Tutup", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

}
