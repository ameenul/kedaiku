package com.example.kedaiku.UI.customer_menu;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.kedaiku.R;

import com.example.kedaiku.entites.Customer;
import com.example.kedaiku.entites.CustomerGroup;
import com.example.kedaiku.viewmodel.CustomerGroupViewModel;
import com.example.kedaiku.viewmodel.CustomerViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class CustomerListActivity extends AppCompatActivity {

    private CustomerViewModel customerViewModel;
    private CustomerGroupViewModel customerGroupViewModel;
    private CustomerAdapter adapter;
    private EditText editTextSearch;
    private String csvData;
    private ActivityResultLauncher<Intent> createFileLauncher;
    private ImageView buttonExportCsv;
    Map<Long, String> groupMap;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_list);

        androidx.recyclerview.widget.RecyclerView recyclerView = findViewById(R.id.recyclerViewCustomer);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new CustomerAdapter(this);
        recyclerView.setAdapter(adapter);

        customerViewModel = new ViewModelProvider(this).get(CustomerViewModel.class);
        customerGroupViewModel = new ViewModelProvider(this).get(CustomerGroupViewModel.class);

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


        buttonExportCsv = findViewById(R.id.csv);
        buttonExportCsv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                exportCustomerDataToCsv();
            }
        });

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
                // Tampilkan AlertDialog untuk konfirmasi
                new AlertDialog.Builder(CustomerListActivity.this)
                        .setTitle("Konfirmasi Penghapusan")
                        .setMessage("Apakah Anda yakin ingin menghapus pelanggan ini?")
                        .setPositiveButton("Ya", (dialog, which) -> {
                            // Jika pengguna memilih "Ya", hapus pelanggan
                            customerViewModel.delete(customer);
                            Toast.makeText(CustomerListActivity.this, "Pelanggan dihapus", Toast.LENGTH_SHORT).show();
                        })
                        .setNegativeButton("Tidak", (dialog, which) -> {
                            // Jika pengguna memilih "Tidak", tutup dialog
                            dialog.dismiss();
                        })
                        .create()
                        .show();
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


        createFileLauncher =
                registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri uri = result.getData().getData();
                        if (uri != null) {
                            writeCsvDataToUri(uri);
                        }
                    }
                });


        // Dapatkan peta Customer Group dan lanjutkan setelah data tersedia
        getCustomerGroupMap(groupMap -> {
            this.groupMap=groupMap;

        });

    }


    private void exportCustomerDataToCsv() {
        // Dapatkan data pelanggan dari adapter
        List<Customer> customersToExport = adapter.getCustomerList();

        if (customersToExport == null || customersToExport.isEmpty()) {
            Toast.makeText(this, "Tidak ada data pelanggan untuk diekspor", Toast.LENGTH_SHORT).show();
            return;
        }



        StringBuilder data = new StringBuilder();

        // Header file
        data.append("Customer List\n\n");

        // Header kolom
        data.append("ID,Nama,Alamat,Email,Telepon,Group ID,Nama Group\n");

        // Format data pelanggan
        for (Customer customer : customersToExport) {
            data.append(customer.getId()).append(","); // Kolom ID
            data.append(customer.getCustomer_name().replace(",", " ")).append(","); // Nama pelanggan
            data.append(customer.getCustomer_address().replace(",", " ")).append(","); // Alamat
            data.append((customer.getCustomer_email() != null ? customer.getCustomer_email() : "-")).append(","); // Email
            data.append(customer.getCustomer_phone()).append(","); // Telepon
            data.append(customer.getCustomer_group_id()).append(","); // Group ID
            data.append(groupMap.getOrDefault(customer.getCustomer_group_id(), "Non Member")).append("\n"); // Nama Group
        }

        // Simpan data CSV ke variabel
        csvData = data.toString();

        // Buat intent untuk memilih lokasi penyimpanan file
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("text/csv");
        intent.putExtra(Intent.EXTRA_TITLE, "Customer_List.csv");
        createFileLauncher.launch(intent);
    }


    private void writeCsvDataToUri(Uri uri) {
        try {
            OutputStream outputStream = getContentResolver().openOutputStream(uri);
            outputStream.write(csvData.getBytes()); // Tulis data CSV ke output
            outputStream.close();
            Toast.makeText(this, "Data pelanggan berhasil diekspor", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Gagal menyimpan data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }



    private Observer<List<CustomerGroup>> customerGroupObserver;

    private void getCustomerGroupMap(Consumer<Map<Long, String>> callback) {
        // Hapus observer sebelumnya jika ada
        if (customerGroupObserver != null) {
            customerGroupViewModel.getAllGroups().removeObserver(customerGroupObserver);
        }

        // Buat observer baru
        customerGroupObserver = customerGroups -> {
            Map<Long, String> groupMap = new HashMap<>();
            if (customerGroups != null) {
                for (CustomerGroup group : customerGroups) {
                    groupMap.put(group.getId(), group.getName());
                }
            }
            callback.accept(groupMap);
        };

        // Pasang observer
        customerGroupViewModel.getAllGroups().observe(this, customerGroupObserver);
    }




}
