package com.example.kedaiku.UI.promo_menu;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
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
import com.example.kedaiku.entites.CustomerGroup;
import com.example.kedaiku.entites.SpecialPrice;
import com.example.kedaiku.entites.SpecialPriceWithProduct;
import com.example.kedaiku.viewmodel.CustomerGroupViewModel;
import com.example.kedaiku.viewmodel.SpecialPriceViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class HargaKhususFragment extends Fragment {

    private SpecialPriceViewModel viewModel;
    private HargaKhususAdapter adapter;
    private RecyclerView recyclerView;
    private EditText editTextSearch;
    private FloatingActionButton fabAdd;
    private String csvData;
    private final ActivityResultLauncher<Intent> createFileLauncher;
    ImageView buttonExportCsv;
    private CustomerGroupViewModel customerGroupViewModel;
    List<CustomerGroup> listGroups;

    public HargaKhususFragment() {
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
        View view = inflater.inflate(R.layout.fragment_harga_khusus, container, false);

        listGroups = new ArrayList<>();
        // Initialize UI components
        recyclerView = view.findViewById(R.id.recyclerViewHargaKhusus);
        editTextSearch = view.findViewById(R.id.editTextSearchHargaKhusus);
        fabAdd = view.findViewById(R.id.fabAddHargaKhusus);
        buttonExportCsv = view.findViewById(R.id.buttonExportCsvHargaKhusus);

        // Set up RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new HargaKhususAdapter(new ArrayList<>(),listGroups, new HargaKhususAdapter.OnItemClickListener() {
            @Override
            public void onUpdateClicked(SpecialPriceWithProduct specialPriceWithProduct) {
//                // Open Update Dialog or Activity
                UpdateHargaKhususDialog dialog = new UpdateHargaKhususDialog();
                dialog.setSpecialPriceWithProduct(specialPriceWithProduct); // Set data
               dialog.show(getParentFragmentManager(), "UpdateHargaKhususDialog");
            }

            @Override
            public void onDeleteClicked(SpecialPrice specialPrice) {
                // Show Confirmation Dialog
                new AlertDialog.Builder(getContext())
                        .setTitle("Hapus Harga Khusus")
                        .setMessage("Apakah Anda yakin ingin menghapus data ini?")
                        .setPositiveButton("Ya", (dialog, which) -> {
                            viewModel.delete(specialPrice);
                            Toast.makeText(getContext(), "Data berhasil dihapus", Toast.LENGTH_SHORT).show();
                        })
                        .setNegativeButton("Batal", null)
                        .show();
            }
        });
        recyclerView.setAdapter(adapter);

        // Set up ViewModel
        viewModel = new ViewModelProvider(this).get(SpecialPriceViewModel.class);
        customerGroupViewModel = new ViewModelProvider(this).get(CustomerGroupViewModel.class);

        // Observe data
        viewModel.getFilteredSpecialPrices().observe(getViewLifecycleOwner(), this::updateRecyclerView);

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
            AddHargaKhususDialog dialog = new AddHargaKhususDialog();
            dialog.show(getParentFragmentManager(), "AddHargaKhususDialog");
        });

        // Handle CSV export button click
        buttonExportCsv.setOnClickListener(v -> exportHargaKhususDataToCsv());



        customerGroupViewModel.getAllGroups().observe(getViewLifecycleOwner(), groups -> {

            listGroups = groups;
            adapter.setDataMember(groups);

        });


        return view;
    }

    /**
     * Updates the RecyclerView and handles the empty state view.
     *
     * @param specialPriceList List of SpecialPriceWithProduct to display.
     */
    private void updateRecyclerView(List<SpecialPriceWithProduct> specialPriceList) {
        if (specialPriceList == null || specialPriceList.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            //textViewEmptyState.setVisibility(View.VISIBLE); // Uncomment if using an empty state view
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            // textViewEmptyState.setVisibility(View.GONE); // Uncomment if using an empty state view
            adapter.setData(specialPriceList);
        }
    }

    private void exportHargaKhususDataToCsv() {
        List<SpecialPriceWithProduct> specialPricesToExport = adapter.getSpecialPriceList();

        if (specialPricesToExport == null || specialPricesToExport.isEmpty()) {
            Toast.makeText(getContext(), "Tidak ada data harga khusus untuk diekspor", Toast.LENGTH_SHORT).show();
            return;
        }

        StringBuilder data = new StringBuilder();
        data.append("Daftar Harga Khusus\n\n");
        data.append("ID,Nama Produk,Nama Promo,HPP,Harga Normal,Persen,Harga Diskon,Status\n");

        for (SpecialPriceWithProduct specialPriceWithProduct : specialPricesToExport) {
            SpecialPrice specialPrice = specialPriceWithProduct.getSpecialPrice();
            data.append(specialPrice.get_id()).append(",");
            data.append(specialPriceWithProduct.getProduct_name().replace(",", " ")).append(",");
            data.append(specialPrice.getName().replace(",", " ")).append(",");
            data.append(specialPriceWithProduct.getProduct_primary_price()).append(",");
            data.append(specialPriceWithProduct.getProduct_price()).append(",");
            data.append(specialPrice.getPrecent()).append(",");
            data.append(hitungDiskon(specialPrice.getPrecent(),specialPriceWithProduct.getProduct_price())).append(",");
            data.append(specialPrice.getStatus() == 1 ? "Aktif" : "Tidak Aktif").append("\n");
        }

        csvData = data.toString();

        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("text/csv");
        intent.putExtra(Intent.EXTRA_TITLE, "Daftar_Harga_Khusus.csv");
        createFileLauncher.launch(intent);
    }

    private void writeCsvDataToUri(Uri uri) {
        try {
            OutputStream outputStream = getContext().getContentResolver().openOutputStream(uri);
            outputStream.write(csvData.getBytes());
            outputStream.close();
            Toast.makeText(getContext(), "Data harga khusus berhasil diekspor", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Gagal menyimpan data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    double hitungDiskon(double diskon,double harganormal)
    {
        return (100-diskon)/100*harganormal;

    }


}
