package com.example.kedaiku.UI.customer_menu;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.kedaiku.R;

import com.example.kedaiku.entites.Customer;
import com.example.kedaiku.viewmodel.CustomerViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class CustomerListActivity extends AppCompatActivity {

    private CustomerViewModel customerViewModel;
    private CustomerAdapter adapter;
    private EditText editTextSearch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_list);

        androidx.recyclerview.widget.RecyclerView recyclerView = findViewById(R.id.recyclerViewCustomer);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new CustomerAdapter(this);
        recyclerView.setAdapter(adapter);

        customerViewModel = new ViewModelProvider(this).get(CustomerViewModel.class);

        // Observe the customers LiveData
        customerViewModel.getCustomers().observe(this, customers -> {
            adapter.setCustomerList(customers);
        });

        // Handle search input
        editTextSearch = findViewById(R.id.editTextSearchCustomer);
        editTextSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                String searchQuery = s.toString().trim();
                customerViewModel.setSearchQuery(searchQuery);
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
        });

        // Initialize search query to empty string
        customerViewModel.setSearchQuery("");

        // Handle klik pada item
        adapter.setOnItemClickListener(new CustomerAdapter.OnItemClickListener() {
            @Override
            public void onEditClicked(Customer customer) {
                // Buka halaman detail/edit pelanggan
                Intent intent = new Intent(CustomerListActivity.this, CustomerDetailActivity.class);
                intent.putExtra("customer_id", customer.getId());
                startActivity(intent);
            }

            @Override
            public void onDeleteClicked(Customer customer) {
                // Hapus pelanggan
                customerViewModel.delete(customer);
            }

            @Override
            public void onItemClicked(Customer customer) {
                // Buka halaman detail/edit pelanggan
                Intent intent = new Intent(CustomerListActivity.this, CustomerDetailActivity.class);
                intent.putExtra("customer_id", customer.getId());
                startActivity(intent);
            }
        });

        // Handle tombol tambah pelanggan
        FloatingActionButton fabAddCustomer = findViewById(R.id.fabAddCustomer);
        fabAddCustomer.setOnClickListener(v -> {
            // Buka halaman tambah pelanggan
            Intent intent = new Intent(CustomerListActivity.this, AddCustomerActivity.class);
            startActivity(intent);
        });

        // Handle tombol Kelola Group
        Button buttonManageGroup = findViewById(R.id.buttonManageGroup);
        buttonManageGroup.setOnClickListener(v -> {
            // Buka halaman List Group
            Intent intent = new Intent(CustomerListActivity.this, GroupListActivity.class);
            startActivity(intent);
        });
    }
}
