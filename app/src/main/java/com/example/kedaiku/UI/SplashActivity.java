


package com.example.kedaiku.UI;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.splashscreen.SplashScreen;

import com.example.kedaiku.R;

public class SplashActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // Install SplashScreen API
        SplashScreen splashScreen = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) { // Android 12+
            splashScreen = SplashScreen.installSplashScreen(this);

        }

        super.onCreate(savedInstanceState);

        // Jika Android 12 ke bawah, gunakan layout custom
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            setContentView(R.layout.activity_splash); // Layout custom untuk splash screen

        }


        new Thread(() -> {

            try {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S)
                {Thread.sleep(2000);}
                else {Thread.sleep(0);}
                 // Delay 2 detik
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            // Cek SharedPreferences untuk menentukan tujuan aktivitas berikutnya
            SharedPreferences sharedPreferences = getSharedPreferences("MyStorePrefs", MODE_PRIVATE);
            Intent intent;
            if (sharedPreferences.contains("store_name")) {
                intent = new Intent(SplashActivity.this, MainActivity.class);
            } else {
                intent = new Intent(SplashActivity.this, StoreDetailsActivity.class);
            }
            startActivity(intent);
            finish();
        }).start();
    }
}
