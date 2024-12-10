package com.example.kedaiku.UI.promo_menu;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.kedaiku.R;
import com.example.kedaiku.entites.Wholesale;
import com.example.kedaiku.entites.WholesaleWithProduct;
import com.example.kedaiku.viewmodel.WholesaleViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class HargaGrosirFragment extends Fragment {

    private WholesaleViewModel viewModel;
    private HargaGrosirAdapter adapter;
    private RecyclerView recyclerView;
    private EditText editTextSearch;
    private FloatingActionButton fabAdd;
    private String csvData;
    private final ActivityResultLauncher<Intent> createFileLauncher;
    ImageView buttonExportCsv;
    //private TextView textViewEmptyState; // Placeholder for empty state

    public HargaGrosirFragment() {
        // Required empty public constructor
        createFileLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == AppCompatActivity.RESULT_OK) {
                        if (result.getData() != null && result.getData().getData() != null) {
                            writeCsvDataToUri(result.getData().getData());
                        }
                    }
                }
        );
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_harga_grosir, container, false);

        // Initialize UI components
        recyclerView = view.findViewById(R.id.recyclerViewHargaGrosir);
        editTextSearch = view.findViewById(R.id.editTextSearchProduk);
        fabAdd = view.findViewById(R.id.fabAddHargaGrosir);


        // Set up RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new HargaGrosirAdapter(new ArrayList<>(), new HargaGrosirAdapter.OnItemClickListener() {
            @Override
            public void onUpdateClicked(WholesaleWithProduct wholesaleWithProduct) {
                // Open Update Dialog or Activity


                UpdateHargaGrosirDialog dialog = new UpdateHargaGrosirDialog();
                dialog.setWholesaleWithProduct(wholesaleWithProduct); // Set data
                dialog.show(getParentFragmentManager(), "UpdateHargaGrosirDialog");
            }

            @Override
            public void onDeleteClicked(Wholesale wholesale) {
                // Show Confirmation Dialog
                new AlertDialog.Builder(getContext())
                        .setTitle("Hapus Harga Grosir")
                        .setMessage("Apakah Anda yakin ingin menghapus data ini?")
                        .setPositiveButton("Ya", (dialog, which) -> {
                            viewModel.delete(wholesale);
                            Toast.makeText(getContext(), "Data berhasil dihapus", Toast.LENGTH_SHORT).show();
                        })
                        .setNegativeButton("Batal", null)
                        .show();
            }
        });
        recyclerView.setAdapter(adapter);

        // Set up ViewModel
        viewModel = new ViewModelProvider(this).get(WholesaleViewModel.class);

        // Observe data
        viewModel.getFilteredWholesales().observe(getViewLifecycleOwner(), this::updateRecyclerView);

        // Set up search functionality
        editTextSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // No action needed
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                viewModel.setSearchKeyword(s.toString().trim());
            }

            @Override
            public void afterTextChanged(Editable s) {
                // No action needed
            }
        });

        // Handle FAB click
        fabAdd.setOnClickListener(v -> {
            AddHargaGrosirDialog dialog = new AddHargaGrosirDialog();
            dialog.show(getParentFragmentManager(), "AddHargaGrosirDialog");
        });


        // Inside onCreateView method

        buttonExportCsv = view.findViewById(R.id.buttonExportCsvHargaGrosir);
        buttonExportCsv.setOnClickListener(v -> exportHargaGrosirDataToCsv());




        return view;
    }

    /**
     * Updates the RecyclerView and handles the empty state view.
     *
     * @param wholesaleList List of WholesaleWithProduct to display.
     */
    private void updateRecyclerView(List<WholesaleWithProduct> wholesaleList) {
        if (wholesaleList == null || wholesaleList.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
          //  textViewEmptyState.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
           // textViewEmptyState.setVisibility(View.GONE);
            adapter.setData(wholesaleList);
        }
    }

    private void exportHargaGrosirDataToCsv() {
        List<WholesaleWithProduct> wholesalesToExport = adapter.getWholesaleList();

        if (wholesalesToExport == null || wholesalesToExport.isEmpty()) {
            Toast.makeText(getContext(), "Tidak ada data grosir untuk diekspor", Toast.LENGTH_SHORT).show();
            return;
        }

        StringBuilder data = new StringBuilder();
        data.append("Daftar Harga Grosir\n\n");
        data.append("ID,Nama Produk,Nama Grosir,Harga Normal,Harga Grosir,Minimum Quantity,Status\n");

        for (WholesaleWithProduct wholesaleWithProduct : wholesalesToExport) {
            Wholesale wholesale = wholesaleWithProduct.toWholeSale();
            data.append(wholesale.get_id()).append(",");
            data.append(wholesaleWithProduct.getProduct_name().replace(",", " ")).append(",");
            data.append(wholesale.getName().replace(",", " ")).append(",");
            data.append(wholesaleWithProduct.getProduct_price()).append(",");
            data.append(wholesale.getPrice()).append(",");
            data.append(wholesale.getQty()).append(",");
            data.append(wholesale.getStatus() == 1 ? "Aktif" : "Tidak Aktif").append("\n");
        }

        csvData = data.toString();

        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("text/csv");
        intent.putExtra(Intent.EXTRA_TITLE, "Daftar_Harga_Grosir.csv");
        createFileLauncher.launch(intent);
    }

    private void writeCsvDataToUri(Uri uri) {
        try {
            OutputStream outputStream = getContext().getContentResolver().openOutputStream(uri);
            outputStream.write(csvData.getBytes());
            outputStream.close();
            Toast.makeText(getContext(), "Data harga grosir berhasil diekspor", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Gagal menyimpan data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }


}
