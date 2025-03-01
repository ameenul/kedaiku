// File: com.example.kedaiku.helpers.PdfHelper.java

package com.example.kedaiku.helper;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import com.example.kedaiku.UI.penjualan_menu.CartItem;
import com.example.kedaiku.helper.FormatHelper;

import java.io.IOException;
import java.io.OutputStream;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class PdfHelper {

    private static final String TAG = "PdfHelper";

//    private Context context;
//
//    public PdfHelper(Context context) {
//        this.context = context; // Gunakan context Activity
//    }

    /**
     * Membuat dan menulis PDF ke URI yang diberikan.
     *
     * @param uri        Lokasi penyimpanan PDF yang dipilih pengguna melalui SAF.
     * @param saleName   Nama transaksi.
     * @param date       Tanggal transaksi.
     * @param customer   Nama pelanggan.
     * @param subtotal   Subtotal transaksi.
     * @param shipping   Ongkos kirim.
     * @param discount   Potongan harga.
     * @param total      Total transaksi.
     * @param changeDesc Deskripsi perubahan (misalnya, "Kembalian" atau "Piutang").
     * @param change     Jumlah perubahan.
     * @param items      Daftar item penjualan.
     */
    public static boolean createPdf(Context context,Uri uri, String saleName, String date, String customer,
                             String subtotal, String shipping, String discount, String total,String paid,
                             String changeDesc, String change, List<CartItem> items) {
        try {
            PdfDocument document = new PdfDocument();

            // Ukuran halaman PDF (B5: 176 mm × 250 mm ≈ 499 pt × 708 pt)
            int width = (int) (176 * 2.83465); // ≈ 499 pt
            int height = (int) (250 * 2.83465); // ≈ 708 pt
            PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(width, height, 1).create();
            PdfDocument.Page page = document.startPage(pageInfo);

            Canvas canvas = page.getCanvas();
            Paint paint = new Paint();
            paint.setTextSize(12f);
            paint.setAntiAlias(true);

            int y = 25;

            // Header Nota
            paint.setTextSize(18f);
            paint.setFakeBoldText(true);
            canvas.drawText("===== Nota Penjualan =====", 50, y, paint);
            paint.setFakeBoldText(false);
            y += 25;

            // Nama Transaksi
            paint.setTextSize(12f);
            canvas.drawText("Nama Transaksi: " + saleName, 50, y, paint);
            y += 20;

            // Tanggal
            canvas.drawText("Tanggal: " + date, 50, y, paint);
            y += 20;

            // Nama Pelanggan
            canvas.drawText( customer, 50, y, paint);
            y += 25;

            // Garis Pemisah
            canvas.drawLine(50, y, width - 50, y, paint);
            y += 20;

            // Detail Item
            for (CartItem item : items) {
                String tipeHarga;
                if (item.getFinalPrice() == item.getPrice()) {
                    tipeHarga = "harga normal";
                } else if (item.getFinalPrice() == item.getSpecialPrice()) {
                    tipeHarga = "harga spesial";
                } else {
                    tipeHarga = "harga wholesale";
                }

                String leftText = item.getProductName()
                        + " " + item.getQuantity() + " " + item.getUnit()
                        + " x " + FormatHelper.formatCurrency(item.getFinalPrice())
                        + " (" + tipeHarga + ")";
                String rightText = FormatHelper.formatCurrency(item.getQuantity() * item.getFinalPrice());

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
            canvas.drawText("Subtotal: " + subtotal, 50, y, paint);
            y += 20;
            canvas.drawText("Ongkos Kirim: " + shipping, 50, y, paint);
            y += 20;
            canvas.drawText("Potongan: " + discount, 50, y, paint);
            y += 20;
            canvas.drawText("Total: " + total, 50, y, paint);
            y += 20;
            canvas.drawText("Terbayar: " + paid, 50, y, paint);
            y += 20;
            canvas.drawText(changeDesc + ": " + change, 50, y, paint);
            y += 25;

            // Garis Pemisah
            canvas.drawLine(50, y, width - 50, y, paint);
            y += 20;

            // Footer Nota
            canvas.drawText("Terima kasih!", 50, y, paint);

            // Selesai membuat halaman
            document.finishPage(page);

            // Menulis PDF ke URI yang dipilih pengguna
            try (OutputStream outputStream = context.getContentResolver().openOutputStream(uri)) {
                document.writeTo(outputStream);
                Toast.makeText(context, "PDF berhasil dibuat dan disimpan!", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "PDF berhasil ditulis ke URI.");
                document.close();
                return true; // Menandakan pembuatan PDF berhasil
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(context, "Gagal menyimpan PDF", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "IOException saat menulis PDF: " + e.getMessage());
                document.close();
                return false; // Menandakan pembuatan PDF gagal
            }
        } catch (Exception e) {
            Toast.makeText(context,"Error : "+e.getMessage(),Toast.LENGTH_SHORT).show();
            return false;
        }

    }

}
