package com.example.kedaiku.UI.customer_menu;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.kedaiku.R;
import com.example.kedaiku.entites.CustomerGroup;
import com.example.kedaiku.viewmodel.CustomerGroupViewModel;
import com.example.kedaiku.viewmodel.CustomerViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class GroupListActivity extends AppCompatActivity {

    private CustomerGroupViewModel groupViewModel;
    private CustomerViewModel customerViewModel;
    private GroupAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_list);

        // Inisialisasi CustomerViewModel
        customerViewModel = new ViewModelProvider(this).get(CustomerViewModel.class);

        androidx.recyclerview.widget.RecyclerView recyclerView = findViewById(R.id.recyclerViewGroup);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new GroupAdapter(this);
        recyclerView.setAdapter(adapter);

        groupViewModel = new ViewModelProvider(this).get(CustomerGroupViewModel.class);
        groupViewModel.getAllGroups().observe(this, groups -> {
            adapter.setGroupList(groups);
        });

        // Handle klik pada item
        adapter.setOnItemClickListener(new GroupAdapter.OnItemClickListener() {
            @Override
            public void onEditClicked(CustomerGroup group) {
                // Tampilkan dialog untuk mengubah nama group
                showEditGroupDialog(group);
            }

            @Override
            public void onDeleteClicked(CustomerGroup group) {
                // Konfirmasi penghapusan
                new AlertDialog.Builder(GroupListActivity.this)
                        .setTitle("Hapus Group")
                        .setMessage("Apakah Anda yakin ingin menghapus group ini? Semua anggotanya akan dikembalikan ke group default.")
                        .setPositiveButton("Ya", (dialog, which) -> {
                            // Panggil metode transaksi untuk menghapus grup dan memperbarui anggota
                            customerViewModel.deleteGroupAndUpdateCustomers(group.getId());
                            Toast.makeText(GroupListActivity.this, "Group dihapus", Toast.LENGTH_SHORT).show();
                        })
                        .setNegativeButton("Batal", null)
                        .show();
            }

            @Override
            public void onDetailClicked(CustomerGroup group) {
                // Buka halaman detail group
                Intent intent = new Intent(GroupListActivity.this, GroupDetailActivity.class);
                intent.putExtra("group_id", group.getId());
                startActivity(intent);
            }
        });

        // Handle tombol tambah group
        FloatingActionButton fabAddGroup = findViewById(R.id.fabAddGroup);
        fabAddGroup.setOnClickListener(v -> {
            // Tampilkan dialog untuk menambah group
            showAddGroupDialog();
        });
    }

    private void showAddGroupDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Tambah Group");

        final EditText input = new EditText(this);
        input.setHint("Nama Group");
        builder.setView(input);

        builder.setPositiveButton("Simpan", (dialog, which) -> {
            String groupName = input.getText().toString().trim();
            if (TextUtils.isEmpty(groupName)) {
                Toast.makeText(this, "Nama group tidak boleh kosong", Toast.LENGTH_SHORT).show();
            } else {
                CustomerGroup group = new CustomerGroup(groupName);
                groupViewModel.insert(group);
                Toast.makeText(this, "Group ditambahkan", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Batal", null);

        builder.show();
    }

    private void showEditGroupDialog(CustomerGroup group) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Ubah Nama Group");

        final EditText input = new EditText(this);
        input.setText(group.getName());
        builder.setView(input);

        builder.setPositiveButton("Simpan", (dialog, which) -> {
            String groupName = input.getText().toString().trim();
            if (TextUtils.isEmpty(groupName)) {
                Toast.makeText(this, "Nama group tidak boleh kosong", Toast.LENGTH_SHORT).show();
            } else {
                group.setName(groupName);
                groupViewModel.update(group);
                Toast.makeText(this, "Group diubah", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Batal", null);

        builder.show();
    }
}
