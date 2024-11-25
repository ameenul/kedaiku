package com.example.kedaiku.UI.customer_menu;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.kedaiku.R;
import com.example.kedaiku.entites.Customer;
import com.example.kedaiku.viewmodel.CustomerViewModel;

public class AddCustomerActivity extends AppCompatActivity {

    private EditText editTextName, editTextAddress, editTextPhoneNumber, editTextEmail;
    private CustomerViewModel customerViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_customer);

        editTextName = findViewById(R.id.editTextName);
        editTextAddress = findViewById(R.id.editTextAddress);
        editTextPhoneNumber = findViewById(R.id.editTextPhoneNumber);
        editTextEmail = findViewById(R.id.editTextEmail);
        Button buttonSave = findViewById(R.id.buttonSaveCustomer);

        customerViewModel = new ViewModelProvider(this).get(CustomerViewModel.class);

        buttonSave.setOnClickListener(v -> {
            String name = editTextName.getText().toString().trim();
            String address = editTextAddress.getText().toString().trim();
            String phoneNumber = editTextPhoneNumber.getText().toString().trim();
            String email = editTextEmail.getText().toString().trim();

            if (TextUtils.isEmpty(name) || TextUtils.isEmpty(address) || TextUtils.isEmpty(phoneNumber)) {
                Toast.makeText(this, "Nama, Alamat, dan Nomor Telepon wajib diisi", Toast.LENGTH_SHORT).show();
            } else {
                // groupId default 0
                Customer customer = new Customer(name, address, email, phoneNumber, 0);
                customerViewModel.insert(customer);
                Toast.makeText(this, "Pelanggan berhasil ditambahkan", Toast.LENGTH_SHORT).show();
                finish(); // Tutup aktivitas dan kembali ke list pelanggan
            }
        });
    }
}
