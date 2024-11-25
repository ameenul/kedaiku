package com.example.kedaiku.UI.inventory_menu;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.kedaiku.R;
import com.example.kedaiku.UI.product_menu.ProductAdapter;
import com.example.kedaiku.viewmodel.ProductViewModel;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link PurchaseFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PurchaseFragment extends Fragment {

    private ProductPurchaseAdapter adapter;
    private ProductViewModel productViewModel;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_purchase, container, false);
        RecyclerView recyclerView = view.findViewById(R.id.recyclerViewPurchase);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new ProductPurchaseAdapter();
        recyclerView.setAdapter(adapter);

        productViewModel = new ViewModelProvider(this).get(ProductViewModel.class);
        productViewModel.getAllProducts().observe(getViewLifecycleOwner(), products -> {
            adapter.submitList(products);
        });

        return view;
    }
}
