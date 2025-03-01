package com.example.kedaiku.UI.utang_menu;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.kedaiku.R;

import com.example.kedaiku.entites.Creditor;
import com.example.kedaiku.viewmodel.CreditorViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.OutputStream;
import java.util.List;
import java.util.Map;

public class CreditorListActivity extends AppCompatActivity {

    private CreditorViewModel creditorViewModel;

    private CreditorAdapter adapter;
    private EditText editTextSearch;
    private String csvData;
    private ActivityResultLauncher<Intent> createFileLauncher;
    private ImageView buttonExportCsv;
    Map<Long, String> groupMap;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_creditor_list);

        androidx.recyclerview.widget.RecyclerView recyclerView = findViewById(R.id.recyclerViewCreditor);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new CreditorAdapter(this);
        recyclerView.setAdapter(adapter);

        creditorViewModel = new ViewModelProvider(this).get(CreditorViewModel.class);


        // Observe the creditors LiveData
        creditorViewModel.getCreditors().observe(this, creditors -> {
            adapter.setCreditorList(creditors);
        });

        // Handle search input
        editTextSearch = findViewById(R.id.editTextSearchCreditor);
        editTextSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                String searchQuery = s.toString().trim();
                creditorViewModel.setSearchQuery(searchQuery);
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
        });



        // Initialize search query to empty string
        creditorViewModel.setSearchQuery("");


        buttonExportCsv = findViewById(R.id.csv);
        buttonExportCsv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                exportcreditorDataToCsv();
            }
        });

        // Handle klik pada item
        adapter.setOnItemClickListener(new CreditorAdapter.OnItemClickListener() {
            @Override
            public void onEditClicked(Creditor creditor) {
                // Buka halaman detail/edit pelanggan
                Intent intent = new Intent(CreditorListActivity.this, CreditorDetailActivity.class);
                intent.putExtra("creditor_id", creditor.get_id());
                startActivity(intent);
            }

            @Override
            public void onDeleteClicked(Creditor creditor) {
                // Tampilkan AlertDialog untuk konfirmasi
                new AlertDialog.Builder(CreditorListActivity.this)
                        .setTitle("Konfirmasi Penghapusan")
                        .setMessage("Apakah Anda yakin ingin menghapus creditor ini?")
                        .setPositiveButton("Ya", (dialog, which) -> {
                            // Jika pengguna memilih "Ya", hapus pelanggan
                            creditorViewModel.delete(creditor);
                            Toast.makeText(CreditorListActivity.this, "Creditor dihapus", Toast.LENGTH_SHORT).show();
                        })
                        .setNegativeButton("Tidak", (dialog, which) -> {
                            // Jika pengguna memilih "Tidak", tutup dialog
                            dialog.dismiss();
                        })
                        .create()
                        .show();
            }


            @Override
            public void onItemClicked(Creditor creditor) {
                // Buka halaman detail/edit pelanggan
                Intent intent = new Intent(CreditorListActivity.this, CreditorDetailActivity.class);
                intent.putExtra("creditor_id", creditor.get_id());
                startActivity(intent);
            }
        });

        // Handle tombol tambah pelanggan
        FloatingActionButton fabAddcreditor = findViewById(R.id.fabAddCreditor);
        fabAddcreditor.setOnClickListener(v -> {
            // Buka halaman tambah pelanggan
            Intent intent = new Intent(CreditorListActivity.this, AddKrediturActivity.class);
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



    }


    private void exportcreditorDataToCsv() {
        // Dapatkan data pelanggan dari adapter
        List<Creditor> creditorsToExport = adapter.getCreditorList();

        if (creditorsToExport == null || creditorsToExport.isEmpty()) {
            Toast.makeText(this, "Tidak ada data pelanggan untuk diekspor", Toast.LENGTH_SHORT).show();
            return;
        }



        StringBuilder data = new StringBuilder();

        // Header file
        data.append("creditor List\n\n");

        // Header kolom
        data.append("ID,Nama,Alamat,Email,Telepon\n");

        // Format data pelanggan
        for (Creditor creditor : creditorsToExport) {
            data.append(creditor.get_id()).append(","); // Kolom ID
            data.append(creditor.getCreditor_name().replace(",", " ")).append(","); // Nama pelanggan
            data.append(creditor.getCreditor_address().replace(",", " ")).append(","); // Alamat
            data.append((creditor.getCreditor_email() != null ? creditor.getCreditor_email() : "-")).append(","); // Email
            data.append(creditor.getCreditor_phone()).append(","); // Telepon
        }

        // Simpan data CSV ke variabel
        csvData = data.toString();

        // Buat intent untuk memilih lokasi penyimpanan file
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("text/csv");
        intent.putExtra(Intent.EXTRA_TITLE, "creditor_List.csv");
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





}
