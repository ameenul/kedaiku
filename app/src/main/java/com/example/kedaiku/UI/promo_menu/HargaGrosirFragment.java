package com.example.kedaiku.UI.promo_menu;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.kedaiku.R;
import com.example.kedaiku.entites.WholesaleWithProduct;
import com.example.kedaiku.viewmodel.WholesaleViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class HargaGrosirFragment extends Fragment {

    private WholesaleViewModel viewModel;
    private HargaGrosirAdapter adapter;
    private RecyclerView recyclerView;
    private EditText editTextSearch;
    private FloatingActionButton fabAdd;

    public HargaGrosirFragment() {
        // Required empty public constructor
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
        adapter = new HargaGrosirAdapter(new ArrayList<>());
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
                viewModel.setSearchKeyword(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
                // No action needed
            }
        });

        fabAdd.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), AddHargaGrosirActivity.class);
            startActivity(intent);
        });


        return view;
    }

    private void updateRecyclerView(List<WholesaleWithProduct> wholesaleList) {
        adapter.setData(wholesaleList);
    }
}
