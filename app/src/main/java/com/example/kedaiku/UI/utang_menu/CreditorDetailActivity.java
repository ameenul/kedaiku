package com.example.kedaiku.UI.utang_menu;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.kedaiku.R;
import com.example.kedaiku.entites.Creditor;
import com.example.kedaiku.viewmodel.CreditorViewModel;

public class CreditorDetailActivity extends AppCompatActivity {

    private EditText editTextName, editTextAddress, editTextPhoneNumber, editTextEmail;
    private Button buttonUpdate, buttonCall, buttonWhatsApp;
    private CreditorViewModel creditorViewModel;
    private long creditorId;
    private Creditor creditor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_creditor_detail);

        creditorId = getIntent().getLongExtra("creditor_id", -1);

        editTextName = findViewById(R.id.editTextName);
        editTextAddress = findViewById(R.id.editTextAddress);
        editTextPhoneNumber = findViewById(R.id.editTextPhoneNumber);
        editTextEmail = findViewById(R.id.editTextEmail);
        buttonUpdate = findViewById(R.id.buttonUpdateCreditor);
        buttonCall = findViewById(R.id.buttonCallCreditor);
        buttonWhatsApp = findViewById(R.id.buttonWhatsAppCreditor);

        creditorViewModel = new ViewModelProvider(this).get(CreditorViewModel.class);

        // Dapatkan data pelanggan
        creditorViewModel.getCreditorById(creditorId).observe(this, creditor -> {
            if (creditor != null) {
                this.creditor = creditor;
                editTextName.setText(creditor.getCreditor_name());
                editTextAddress.setText(creditor.getCreditor_address());
                editTextPhoneNumber.setText(creditor.getCreditor_phone());
                editTextEmail.setText(creditor.getCreditor_email());

                // Atur OnClickListener setelah data creditor tersedia
                setupClickListeners();
            } else {
                Toast.makeText(this, "Data pelanggan tidak ditemukan", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void setupClickListeners() {
        buttonUpdate.setOnClickListener(v -> {
            String name = editTextName.getText().toString().trim();
            String address = editTextAddress.getText().toString().trim();
            String phoneNumber = editTextPhoneNumber.getText().toString().trim();
            String email = editTextEmail.getText().toString().trim();

            if (TextUtils.isEmpty(name) || TextUtils.isEmpty(address) || TextUtils.isEmpty(phoneNumber)) {
                Toast.makeText(this, "Nama, Alamat, dan Nomor Telepon wajib diisi", Toast.LENGTH_SHORT).show();
            } else {
                creditor.setCreditor_name(name);
                creditor.setCreditor_address(address);
                creditor.setCreditor_phone(phoneNumber);
                creditor.setCreditor_email(email);
                creditorViewModel.update(creditor);
                Toast.makeText(this, "Data diperbarui", Toast.LENGTH_SHORT).show();
                finish();
            }
        });

        buttonCall.setOnClickListener(v -> {
            String phoneNumber = creditor.getCreditor_phone();
            Intent intent = new Intent(Intent.ACTION_DIAL);
            intent.setData(Uri.parse("tel:" + phoneNumber));
            startActivity(intent);
        });

        buttonWhatsApp.setOnClickListener(v -> {
            String phoneNumber = creditor.getCreditor_phone();

            // Hapus semua karakter selain digit
            phoneNumber = phoneNumber.replaceAll("[^\\d]", "");

            // Ganti '0' di awal dengan '62'
            if (phoneNumber.startsWith("0")) {
                phoneNumber = "62" + phoneNumber.substring(1);
            } else if (phoneNumber.startsWith("62")) {
                // Nomor sudah dalam format internasional, tidak perlu diubah
            } else if (phoneNumber.startsWith("8")) {
                // Jika nomor dimulai dengan '8', tambahkan '62' di depan
                phoneNumber = "62" + phoneNumber;
            } else {
                // Jika format nomor tidak dikenal, Anda dapat menanganinya di sini
                Toast.makeText(this, "Format nomor telepon tidak dikenal", Toast.LENGTH_SHORT).show();
                return;
            }

            String url = "https://wa.me/" + phoneNumber;
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(url));
            startActivity(intent);
        });
    }
}
