package com.example.kedaiku.UI.penjualan_menu;

import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
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

import java.io.IOException;
import java.io.OutputStream;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class DetailPenjualanActivity extends AppCompatActivity {

    private static final String TAG = "DetailPenjualanActivity";

    private TextView textViewTransactionName, textViewDate, textViewSelectedCustomer;
    private TextView textViewSubtotal, textViewShippingCost, textViewDiscount, textViewTotal;
    private TextView textViewChangeAmount, textViewChange;
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
    private PdfHelper pdfHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_penjualan);

        pdfHelper = new PdfHelper(this);

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
        textViewChangeAmount = findViewById(R.id.textViewChangeAmount);
        textViewChange = findViewById(R.id.textViewChange);
        spinnerKas = findViewById(R.id.spinnerKas);
        itemsContainer = findViewById(R.id.itemsContainer);
        buttonPrintPdf = findViewById(R.id.buttonPrintPdf);

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
                                boolean success = pdfHelper.createPdf(
                                        uri,
                                        textViewTransactionName.getText().toString(),
                                        textViewDate.getText().toString(),
                                        textViewSelectedCustomer.getText().toString(),
                                        textViewSubtotal.getText().toString(),
                                        textViewShippingCost.getText().toString(),
                                        textViewDiscount.getText().toString(),
                                        textViewTotal.getText().toString(),
                                        textViewChangeAmount.getText().toString(),
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
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
        String formattedDate = dateFormat.format(saleDateMillis);
        textViewDate.setText(formattedDate);

        // 3. Mengisi Nama Pelanggan
        String customerName = saleWithDetails.getCustomer() != null ? saleWithDetails.getCustomer().getCustomer_name() : "Pelanggan terhapus";
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
            textViewChangeAmount.setText("Piutang");
        } else {
            textViewChangeAmount.setText("Kembalian");
        }
        textViewChange.setText(formatRupiah(change));

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
        intent.putExtra(Intent.EXTRA_TITLE, "Nota_Penjualan_" + System.currentTimeMillis() + ".pdf");
        createPdfLauncher.launch(intent);
    }

    private void writePdfToUri(Uri uri) {
        Log.d(TAG, "Menulis PDF ke URI: " + uri.toString());
        try {
            PdfDocument document = new PdfDocument();

            // Membuat halaman PDF dengan ukuran B5 (176 mm × 250 mm ≈ 499 pt × 708 pt)
            int width = (int) (176 * 2.83465); // ≈ 499 pt
            int height = (int) (250 * 2.83465); // ≈ 708 pt
            PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(width, height, 1).create();
            PdfDocument.Page page = document.startPage(pageInfo);

            Canvas canvas = page.getCanvas();
            Paint paint = new Paint();
            paint.setTextSize(12);
            paint.setAntiAlias(true);

            int y = 25;

            // Header Nota
            paint.setTextSize(18);
            paint.setFakeBoldText(true);
            canvas.drawText("===== Nota Penjualan =====", 50, y, paint);
            paint.setFakeBoldText(false);
            y += 25;

            // Nama Transaksi
            paint.setTextSize(12);
            canvas.drawText("Nama Transaksi: " + textViewTransactionName.getText().toString(), 50, y, paint);
            y += 20;

            // Tanggal
            canvas.drawText("Tanggal: " + textViewDate.getText().toString(), 50, y, paint);
            y += 20;

            // Nama Pelanggan
            canvas.drawText(textViewSelectedCustomer.getText().toString(), 50, y, paint);
            y += 25;

            // Garis Pemisah
            canvas.drawLine(50, y, width - 50, y, paint);
            y += 20;

            // **Detail Item tanpa HPP**
            for (CartItem item : currentItems) {
                String tipeHarga;
                if (item.getFinalPrice() == item.getPrice()) {
                    tipeHarga = "harga normal";
                } else if (item.getFinalPrice() == item.getSpecialPrice()) {
                    tipeHarga = "harga spesial";
                } else {
                    tipeHarga = "harga wholesale";
                }

                String leftText = item.getProductName()
                        + " " + item.getQuantity() + " " + item.getUnit() + " x " + formatRupiah(item.getFinalPrice())
                        + " (" + tipeHarga + ")";
                String rightText = formatRupiah(item.getQuantity() * item.getFinalPrice());

                // Menggambar teks di sisi kiri
                canvas.drawText(leftText, 50, y, paint);

                // Menggambar teks di sisi kanan
                float rightTextWidth = paint.measureText(rightText);
                canvas.drawText(rightText, width - 50 - rightTextWidth, y, paint);

                y += 25;

                // Cek apakah masih ada ruang di halaman, jika tidak, buat halaman baru
                if (y > height - 100) { // Batas bawah halaman
                    document.finishPage(page);
                    pageInfo = new PdfDocument.PageInfo.Builder(width, height, document.getPages().size() + 1).create();
                    page = document.startPage(pageInfo);
                    canvas = page.getCanvas();
                    y = 25;
                }
            }

            // Garis Pemisah
            canvas.drawLine(50, y, width - 50, y, paint);
            y += 20;

            // Ringkasan Transaksi
            canvas.drawText("Subtotal: " + textViewSubtotal.getText().toString(), 50, y, paint);
            y += 20;
            canvas.drawText("Ongkos Kirim: " + textViewShippingCost.getText().toString(), 50, y, paint);
            y += 20;
            canvas.drawText("Potongan: " + textViewDiscount.getText().toString(), 50, y, paint);
            y += 20;
            canvas.drawText("Total: " + textViewTotal.getText().toString(), 50, y, paint);
            y += 20;
            canvas.drawText(textViewChangeAmount.getText().toString() + ": " + textViewChange.getText().toString(), 50, y, paint);
            y += 25;

            // Garis Pemisah
            canvas.drawLine(50, y, width - 50, y, paint);
            y += 20;

            // Footer Nota
            canvas.drawText("Terima kasih!", 50, y, paint);

            // Selesai membuat halaman
            document.finishPage(page);

            // Menulis PDF ke URI yang dipilih pengguna
            try (OutputStream outputStream = getContentResolver().openOutputStream(uri)) {
                document.writeTo(outputStream);
                Toast.makeText(this, "PDF berhasil dibuat dan disimpan!", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "PDF berhasil ditulis ke URI.");

                // Membagikan PDF
                sharePdf(uri);

            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Gagal menyimpan PDF", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "IOException saat menulis PDF: " + e.getMessage());
            }

            // Menutup dokumen
            document.close();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Gagal membuat PDF", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Exception saat membuat PDF: " + e.getMessage());
        }
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
