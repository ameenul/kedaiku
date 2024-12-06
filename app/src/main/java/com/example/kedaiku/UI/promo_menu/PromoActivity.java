package com.example.kedaiku.UI.promo_menu;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.example.kedaiku.R;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class PromoActivity extends AppCompatActivity {

    private TabLayout tabLayoutPromo;
    private ViewPager2 viewPagerPromo;
    private PromoPagerAdapter promoPagerAdapter;


    private String csvData; // Variabel untuk menyimpan data CSV

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_promo);

        // Inisialisasi view
        tabLayoutPromo = findViewById(R.id.tabLayoutPromo);
        viewPagerPromo = findViewById(R.id.viewPagerPromo);


        // Setup ViewPager2 dengan Adapter
        promoPagerAdapter = new PromoPagerAdapter(this);
        viewPagerPromo.setAdapter(promoPagerAdapter);

        // Menghubungkan TabLayout dengan ViewPager2
        new TabLayoutMediator(tabLayoutPromo, viewPagerPromo,
                (tab, position) -> {
                    if (position == 0) {
                        tab.setText("Harga Grosir");
                    } else if (position == 1) {
                        tab.setText("Harga Khusus");
                    }
                }).attach();

        // Listener untuk tombol Export CSV

    }



}
