package com.example.kedaiku.UI.utang_menu;

import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.example.kedaiku.R;
import com.example.kedaiku.viewmodel.CashViewModel;
import com.example.kedaiku.viewmodel.CreditorViewModel;

public class CekErrorActivity extends AppCompatActivity {
    private CreditorViewModel creditorViewModel;
    private CashViewModel cashViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_cek_error);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
//        cashViewModel = new ViewModelProvider(this).get(CashViewModel.class);
//        cashViewModel.getAllCash().observe(this, cashes -> {
//            Toast.makeText(this, "Jumlah kas: " + cashes.size(), Toast.LENGTH_LONG).show();
//        });
        creditorViewModel = new ViewModelProvider(this).get(CreditorViewModel .class);
        creditorViewModel.getCreditors().observe(this, creditors -> {
            Toast.makeText(this, "Jumlah kreditur: " , Toast.LENGTH_LONG).show();
        });
        creditorViewModel.setSearchQuery("");
    }
    }

