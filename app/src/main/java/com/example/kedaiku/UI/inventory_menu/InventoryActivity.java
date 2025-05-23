package com.example.kedaiku.UI.inventory_menu;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.example.kedaiku.R;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class InventoryActivity extends AppCompatActivity {

    private ViewPager2 viewPager;
    private TabLayout tabLayout;
   // private FloatingActionButton fabAddPurchase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventory);

        viewPager = findViewById(R.id.viewPager);
        tabLayout = findViewById(R.id.tabLayout);
//        fabAddPurchase = findViewById(R.id.fabAddPurchase);

        setupViewPager();
        setupTabs();

//        fabAddPurchase.setOnClickListener(v -> {
//            Intent intent = new Intent(this, AddPurchaseActivity.class);
//            startActivity(intent);
//        });
    }

    private void setupViewPager() {
        ViewPagerAdapter adapter = new ViewPagerAdapter(this);
        adapter.addFragment(new StockProductFragment(), "Stok Produk");
        adapter.addFragment(new PurchaseFragment(), "Pembelian");
        viewPager.setAdapter(adapter);

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                //fabAddPurchase.setVisibility(position == 1 ? View.VISIBLE : View.GONE);
            }
        });
    }

    private void setupTabs() {
        // Bind TabLayout with ViewPager2
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            switch (position) {
                case 0:
                    tab.setText("Stok Produk");
                    break;
                case 1:
                    tab.setText("Pembelian");
                    break;
            }
        }).attach();


    }
}
