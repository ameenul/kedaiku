package com.example.kedaiku.UI.customer_menu;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.kedaiku.R;
import com.example.kedaiku.entites.Customer;
import com.example.kedaiku.viewmodel.CustomerGroupViewModel;
import com.example.kedaiku.viewmodel.CustomerViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class GroupDetailActivity extends AppCompatActivity {

    private CustomerGroupViewModel groupViewModel;
    private CustomerViewModel customerViewModel;
    private long groupId;
    private TextView textViewGroupName;
    private CustomerAdapter adapter;
    private List<Customer> customersWithoutGroup = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_detail);

        groupId = getIntent().getLongExtra("group_id", -1);

        textViewGroupName = findViewById(R.id.textViewGroupName);
        androidx.recyclerview.widget.RecyclerView recyclerView = findViewById(R.id.recyclerViewGroupMembers);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new CustomerAdapter(this);


        recyclerView.setAdapter(adapter);

        adapter.setOnItemClickListener(new CustomerAdapter.OnItemClickListener() {
            @Override
            public void onEditClicked(Customer customer) {
                // Jika Anda ingin mengimplementasikan fitur edit di sini, tambahkan kode Anda
                // Buka halaman detail/edit pelanggan
                Intent intent = new Intent(GroupDetailActivity.this, CustomerDetailActivity.class);
                intent.putExtra("customer_id", customer.getId());
                startActivity(intent);
            }

            @Override
            public void onDeleteClicked(Customer customer) {
                // Hapus anggota dari grup
                removeCustomerFromGroup(customer);
            }

            @Override
            public void onItemClicked(Customer customer) {
                // Implementasi jika diperlukan
            }
        });

        groupViewModel = new ViewModelProvider(this).get(CustomerGroupViewModel.class);
        customerViewModel = new ViewModelProvider(this).get(CustomerViewModel.class);

        // Tampilkan nama group
        groupViewModel.getGroupById(groupId).observe(this, group -> {
            if (group != null) {
                textViewGroupName.setText(group.getName());
            }
        });

        // Tampilkan anggota group
        customerViewModel.getCustomersByGroupId(groupId).observe(this, customers -> {
            if (customers != null && !customers.isEmpty()) {
                adapter.setCustomerList(customers);
            } else {
                Toast.makeText(this, "Belum ada member", Toast.LENGTH_SHORT).show();
            }
        });

        // Dapatkan daftar pelanggan tanpa group
        customerViewModel.getCustomersWithoutGroup().observe(this, customers -> {
            if (customers != null) {
                customersWithoutGroup.clear();
                customersWithoutGroup.addAll(customers);
            }
        });

        // Handle tombol tambah anggota
        FloatingActionButton fabAddMember = findViewById(R.id.fabAddMember);
        fabAddMember.setOnClickListener(v -> {
            // Tampilkan dialog atau halaman untuk memilih pelanggan tanpa group
            showAddMemberDialog();
        });
    }

    private void showAddMemberDialog() {
        if (customersWithoutGroup != null && !customersWithoutGroup.isEmpty()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Tambah Anggota");

            String[] customerNames = new String[customersWithoutGroup.size()];
            boolean[] checkedItems = new boolean[customersWithoutGroup.size()];
            for (int i = 0; i < customersWithoutGroup.size(); i++) {
                customerNames[i] = customersWithoutGroup.get(i).getCustomer_name();
                checkedItems[i] = false;
            }

            builder.setMultiChoiceItems(customerNames, checkedItems, (dialog, which, isChecked) -> {
                checkedItems[which] = isChecked;
            });



            builder.setPositiveButton("Tambah", (dialog, which) -> {
                int count = 0;
                for (int i = 0; i < customersWithoutGroup.size(); i++) {
                    if (checkedItems[i]) {
                        Customer customer = customersWithoutGroup.get(i);
                        customer.setCustomer_group_id(groupId);
                        customerViewModel.update(customer);
                        count++;
                    }
                }
                if(count>0)
                { Toast.makeText(this, "Anggota ditambahkan", Toast.LENGTH_SHORT).show();}
            });

            builder.setNegativeButton("Batal", null);
            builder.show();
        } else {
            Toast.makeText(this, "Tidak ada pelanggan tanpa group", Toast.LENGTH_SHORT).show();
        }
    }

    private void removeCustomerFromGroup(Customer customer) {
        new AlertDialog.Builder(this)
                .setTitle("Hapus Anggota")
                .setMessage("Apakah Anda yakin ingin menghapus anggota ini dari grup?")
                .setPositiveButton("Ya", (dialog, which) -> {
                    customer.setCustomer_group_id(0);
                    customerViewModel.update(customer);
                    Toast.makeText(this, "Anggota dihapus dari grup", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Batal", null)
                .show();
    }

}
