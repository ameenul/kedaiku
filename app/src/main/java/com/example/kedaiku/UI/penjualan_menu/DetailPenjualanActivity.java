package com.example.kedaiku.UI.penjualan_menu;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.example.kedaiku.R;

import com.example.kedaiku.entites.Cash;
import com.example.kedaiku.entites.SaleWithDetails;
import com.example.kedaiku.helper.FormatHelper;
import com.example.kedaiku.helper.PdfHelper;
import com.example.kedaiku.viewmodel.CashViewModel;
import com.example.kedaiku.viewmodel.SaleViewModel;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class DetailPenjualanActivity extends AppCompatActivity {

    private static final String TAG = "DetailPenjualanActivity";

    private TextView textViewTransactionName, textViewDate, textViewSelectedCustomer;
    private TextView textViewSubtotal, textViewShippingCost, textViewDiscount, textViewTotal;
    private TextView textViewChangeLabel, textViewChange;
    private TextView textViewTerbayar;
    private Spinner spinnerKas;
    private LinearLayout itemsContainer;
    private Button buttonPrintPdf;

    private SaleViewModel saleViewModel;
    private CashViewModel cashViewModel;

    private long saleId;

    // ActivityResultLauncher untuk SAF
    private ActivityResultLauncher<Intent> createPdfLauncher;

    // Menyimpan daftar item untuk PDF
    private List<CartItem> currentItems;
//    private PdfHelper pdfHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_penjualan);

//        pdfHelper = new PdfHelper(this);

        // Inisialisasi ViewModel
        saleViewModel = new ViewModelProvider(this).get(SaleViewModel.class);
        cashViewModel = new ViewModelProvider(this).get(CashViewModel.class);

        // Inisialisasi UI Elements
        textViewTransactionName = findViewById(R.id.textViewTransactionName);
        textViewDate = findViewById(R.id.textViewDate);
        textViewSelectedCustomer = findViewById(R.id.textViewSelectedCustomer);
        textViewSubtotal = findViewById(R.id.textViewSubtotal);
        textViewShippingCost = findViewById(R.id.textViewShippingCost);
        textViewDiscount = findViewById(R.id.textViewDiscount);
        textViewTotal = findViewById(R.id.textViewTotal);
        textViewChangeLabel = findViewById(R.id.textViewChangeLabel);
        textViewChange = findViewById(R.id.textViewChange);
        spinnerKas = findViewById(R.id.spinnerKas);
        itemsContainer = findViewById(R.id.itemsContainer);
        buttonPrintPdf = findViewById(R.id.buttonPrintPdf);
        textViewTerbayar = findViewById(R.id.textViewTerbayar);

        // Mendapatkan sale_id dari Intent
        Intent intent = getIntent();
        saleId = intent.getLongExtra("sale_id", -1);

        if (saleId == -1) {
            Toast.makeText(this, "Data penjualan tidak valid.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Mengamati data penjualan dari ViewModel
        saleViewModel.getSaleWithDetailsByIdLive(saleId).observe(this, new Observer<SaleWithDetails>() {
            @Override
            public void onChanged(SaleWithDetails saleWithDetails) {
                if (saleWithDetails != null) {
                    displaySaleDetails(saleWithDetails);
                } else {
                    Toast.makeText(DetailPenjualanActivity.this, "Data penjualan tidak ditemukan.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Inisialisasi ActivityResultLauncher
        createPdfLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        Intent dataResult = result.getData();
                        if (dataResult != null) {
                            Uri uri = dataResult.getData();
                            if (uri != null) {
                                Log.d(TAG, "URI dipilih: " + uri.toString());
                                // Membuat PDF menggunakan PdfHelper
                                boolean success = PdfHelper.createPdf(this,
                                        uri,
                                        textViewTransactionName.getText().toString(),
                                        textViewDate.getText().toString(),
                                        textViewSelectedCustomer.getText().toString(),
                                        textViewSubtotal.getText().toString(),
                                        textViewShippingCost.getText().toString(),
                                        textViewDiscount.getText().toString(),
                                        textViewTotal.getText().toString(),
                                        textViewTerbayar.getText().toString(),
                                        textViewChangeLabel.getText().toString(),
                                        textViewChange.getText().toString(),
                                        currentItems
                                );

                                if (success) {
                                    // Setelah berhasil membuat PDF, bagikan PDF
                                    sharePdf(uri);
                                }
                            } else {
                                Log.e(TAG, "URI kosong");
                                Toast.makeText(this, "Gagal mendapatkan lokasi penyimpanan PDF.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    } else {
                        Log.e(TAG, "Hasil dari pembuatan dokumen dibatalkan");
                        Toast.makeText(this, "Pembuatan PDF dibatalkan.", Toast.LENGTH_SHORT).show();
                    }
                }
        );

        // Mengatur listener untuk tombol Cetak PDF
        buttonPrintPdf.setOnClickListener(v -> {
            Log.d(TAG, "Tombol Cetak PDF diklik");
            // Pastikan data penjualan sudah ditampilkan sebelum mencetak PDF
            if (textViewTransactionName.getText().toString().isEmpty()) {
                Toast.makeText(this, "Data penjualan belum siap untuk dicetak.", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "Data penjualan belum siap untuk dicetak.");
            } else {
                createPdfWithSAF();
                Log.d(TAG, "Memulai proses pembuatan PDF dengan SAF.");
            }
        });
    }

    private void displaySaleDetails(SaleWithDetails saleWithDetails) {
        // 1. Mengisi Nama Transaksi
        textViewTransactionName.setText(saleWithDetails.getSale().getSale_transaction_name());

        // 2. Mengisi Tanggal
        long saleDateMillis = saleWithDetails.getSale().getSale_date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy HH:mm", Locale.getDefault());
        String formattedDate = dateFormat.format(saleDateMillis);
        textViewDate.setText(formattedDate);

        // 3. Mengisi Nama Pelanggan
        String customerName = saleWithDetails.getCustomer() != null ? saleWithDetails.getCustomer().getCustomer_name() : "Pelanggan terhapus";
        long customerId = saleWithDetails.getSale().getSale_customer_id();
        if (customerId == 0) {

            customerName ="Umum";
        }
        textViewSelectedCustomer.setText("Nama Pelanggan: " + customerName);

        // 4. Mengisi Subtotal, Ongkos Kirim, Potongan, dan Total
        double subtotal = saleWithDetails.getSale().getSale_total() - saleWithDetails.getSale().getSale_ship() + saleWithDetails.getSale().getSale_discount();
        double ongkir = saleWithDetails.getSale().getSale_ship();
        double potongan = saleWithDetails.getSale().getSale_discount();
        double total = saleWithDetails.getSale().getSale_total();

        textViewSubtotal.setText(formatRupiah(subtotal));
        textViewShippingCost.setText(formatRupiah(ongkir));
        textViewDiscount.setText(formatRupiah(potongan));
        textViewTotal.setText(formatRupiah(total));

        // 5. Mengisi Informasi Pembayaran
        double paidAmount = saleWithDetails.getSale().getSale_paid();
        double change = paidAmount - total;

        if (change < 0) {
            textViewChangeLabel.setText("Piutang");
        } else {
            textViewChangeLabel.setText("Kembalian");
        }
        textViewChange.setText(formatRupiah(change));
        textViewTerbayar.setText(formatRupiah(paidAmount));

        // 6. Mengisi Spinner Kas
        populateKasSpinner(saleWithDetails.getSale().getSale_cash_id());

        // 7. Menampilkan Daftar Item Penjualan
        List<CartItem> items = FormatHelper.parseCartItemsFromDetailSale(saleWithDetails.getDetailSale().getSale_detail(), saleWithDetails.getPromo().getDetail());
        displayItems(items);
    }

    private void populateKasSpinner(long selectedCashId) {
        // Mendapatkan daftar kas dari ViewModel
        cashViewModel.getAllCash().observe(this, new Observer<List<Cash>>() {
            @Override
            public void onChanged(List<Cash> cashList) {
                if (cashList != null && !cashList.isEmpty()) {
                    ArrayAdapter<Cash> adapter = new ArrayAdapter<>(DetailPenjualanActivity.this,
                            android.R.layout.simple_spinner_item, cashList);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinnerKas.setAdapter(adapter);

                    // Menentukan posisi kas yang dipilih
                    int selectedPosition = 0;
                    for (int i = 0; i < cashList.size(); i++) {
                        if (cashList.get(i).getId() == selectedCashId) {
                            selectedPosition = i;
                            break;
                        }
                    }
                    spinnerKas.setSelection(selectedPosition, false);

                    // Menonaktifkan interaksi pengguna
                    spinnerKas.setEnabled(false);
                }
            }
        });
    }

    private void displayItems(List<CartItem> items) {
        itemsContainer.removeAllViews();
        currentItems = items; // Menyimpan daftar item untuk digunakan dalam PDF

        for (CartItem item : items) {
            // Inflate layout item_belanjaan.xml
            View itemView = getLayoutInflater().inflate(R.layout.item_belanjaan, itemsContainer, false);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            layoutParams.setMargins(8, 8, 8, 8);
            itemView.setLayoutParams(layoutParams);

            TextView itemDetails = itemView.findViewById(R.id.textViewItemDetails);
            TextView itemTotal = itemView.findViewById(R.id.textViewItemTotal);

            String tipeHarga;
            if (item.getFinalPrice() == item.getPrice()) {
                tipeHarga = "harga normal ";
            } else if (item.getFinalPrice() == item.getSpecialPrice()) {
                tipeHarga = item.getNamaHargaSpecial();
            } else {
                tipeHarga = item.getNamaWholesale();
            }

            // **Hapus HPP dari UI dan PDF**
            String details = item.getProductName()
                    + "\n" + item.getQuantity() + " " + item.getUnit() + " x " + formatRupiah(item.getFinalPrice())
                    + "\n(" + tipeHarga + ")"
                    + "\n HPP : "
                    + + item.getHpp()                     ;

            itemDetails.setText(details);
            itemTotal.setText(formatRupiah(item.getQuantity() * item.getFinalPrice()));

            itemsContainer.addView(itemView);
        }
    }

    private String formatRupiah(double amount) {
        NumberFormat format = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
        return format.format(amount);
    }

    private void createPdfWithSAF() {
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/pdf");
        intent.putExtra(Intent.EXTRA_TITLE, "Nota_Penjualan_" + textViewDate.getText().toString() + ".pdf");
        createPdfLauncher.launch(intent);
    }

    // Metode untuk berbagi PDF
    private void sharePdf(Uri uri) {
        Log.d(TAG, "Membagikan PDF dengan URI: " + uri.toString());
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("application/pdf");
        intent.putExtra(Intent.EXTRA_STREAM, uri);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        if (intent.resolveActivity(getPackageManager()) != null) {
            Log.d(TAG, "Membuka chooser untuk membagikan PDF.");
            startActivity(Intent.createChooser(intent, "Bagikan Nota"));
        } else {
            Log.e(TAG, "Tidak ada aplikasi yang dapat menangani intent ini.");
            Toast.makeText(this, "Tidak ada aplikasi yang dapat membagikan PDF.", Toast.LENGTH_SHORT).show();
        }
    }

}
