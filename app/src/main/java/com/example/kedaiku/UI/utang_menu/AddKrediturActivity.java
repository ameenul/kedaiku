package com.example.kedaiku.UI.utang_menu;

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


public class AddKrediturActivity extends AppCompatActivity {

    private EditText editTextName, editTextAddress, editTextPhoneNumber, editTextEmail;
    private CreditorViewModel creditorViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_kreditur);

        editTextName = findViewById(R.id.editTextName);
        editTextAddress = findViewById(R.id.editTextAddress);
        editTextPhoneNumber = findViewById(R.id.editTextPhoneNumber);
        editTextEmail = findViewById(R.id.editTextEmail);
        Button buttonSave = findViewById(R.id.buttonSaveCreditor);

        creditorViewModel = new ViewModelProvider(this).get(CreditorViewModel.class);

        buttonSave.setOnClickListener(v -> {
            String name = editTextName.getText().toString().trim();
            String address = editTextAddress.getText().toString().trim();
            String phoneNumber = editTextPhoneNumber.getText().toString().trim();
            String email = editTextEmail.getText().toString().trim();

            if (TextUtils.isEmpty(name) || TextUtils.isEmpty(address) || TextUtils.isEmpty(phoneNumber)) {
                Toast.makeText(this, "Nama, Alamat, dan Nomor Telepon wajib diisi", Toast.LENGTH_SHORT).show();
            } else {
                // groupId default 0
                Creditor creditor = new Creditor(name, address, email, phoneNumber);
                creditorViewModel.insert(creditor);
                Toast.makeText(this, "Creditor berhasil ditambahkan", Toast.LENGTH_SHORT).show();
                finish(); // Tutup aktivitas dan kembali ke list pelanggan
            }
        });
    }
}
