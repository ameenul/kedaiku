package com.example.kedaiku.UI.customer_menu;

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
import com.example.kedaiku.entites.Customer;
import com.example.kedaiku.viewmodel.CustomerViewModel;

public class CustomerDetailActivity extends AppCompatActivity {

    private EditText editTextName, editTextAddress, editTextPhoneNumber, editTextEmail;
    private Button buttonUpdate, buttonCall, buttonWhatsApp;
    private CustomerViewModel customerViewModel;
    private int customerId;
    private Customer customer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_detail);

        customerId = getIntent().getIntExtra("customer_id", -1);

        editTextName = findViewById(R.id.editTextName);
        editTextAddress = findViewById(R.id.editTextAddress);
        editTextPhoneNumber = findViewById(R.id.editTextPhoneNumber);
        editTextEmail = findViewById(R.id.editTextEmail);
        buttonUpdate = findViewById(R.id.buttonUpdateCustomer);
        buttonCall = findViewById(R.id.buttonCallCustomer);
        buttonWhatsApp = findViewById(R.id.buttonWhatsAppCustomer);

        customerViewModel = new ViewModelProvider(this).get(CustomerViewModel.class);

        // Dapatkan data pelanggan
        customerViewModel.getCustomerById(customerId).observe(this, customer -> {
            if (customer != null) {
                this.customer = customer;
                editTextName.setText(customer.getCustomer_name());
                editTextAddress.setText(customer.getCustomer_address());
                editTextPhoneNumber.setText(customer.getCustomer_phone());
                editTextEmail.setText(customer.getCustomer_email());

                // Atur OnClickListener setelah data customer tersedia
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
                customer.setCustomer_name(name);
                customer.setCustomer_address(address);
                customer.setCustomer_phone(phoneNumber);
                customer.setCustomer_email(email);
                customerViewModel.update(customer);
                Toast.makeText(this, "Data diperbarui", Toast.LENGTH_SHORT).show();
                finish();
            }
        });

        buttonCall.setOnClickListener(v -> {
            String phoneNumber = customer.getCustomer_phone();
            Intent intent = new Intent(Intent.ACTION_DIAL);
            intent.setData(Uri.parse("tel:" + phoneNumber));
            startActivity(intent);
        });

        buttonWhatsApp.setOnClickListener(v -> {
            String phoneNumber = customer.getCustomer_phone();

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
