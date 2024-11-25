package com.example.kedaiku.UI;

import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.kedaiku.R;
import com.google.android.material.appbar.CollapsingToolbarLayout;


public class MainActivity extends AppCompatActivity {
    private SharedPreferences sharedPreferences;
    private ImageView storeImageView;
    //private TextView storeNameTextView;
    private CollapsingToolbarLayout collapsingToolbarLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        sharedPreferences = getSharedPreferences("MyStorePrefs", MODE_PRIVATE);
        storeImageView = findViewById(R.id.storeImageView);
       // storeNameTextView = findViewById(R.id.storeNameTextView);
        collapsingToolbarLayout = findViewById(R.id.collapsingToolbar);

        String storeName = sharedPreferences.getString("store_name", "Store Name");
        String encodedImage = sharedPreferences.getString("store_image_uri", null);

      //  storeNameTextView.setText(storeName);
        collapsingToolbarLayout.setTitle(storeName);
        if (encodedImage != null) {
            //byte[] decodedString = Base64.decode(encodedImage, Base64.DEFAULT);
            //Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
            storeImageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
            storeImageView.setImageURI(Uri.parse(encodedImage));
        }
        else{
            storeImageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            storeImageView.setImageResource(R.drawable.store_logo);

        }

        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        recyclerView.setAdapter(new GridAdapter(this));
    }
}
