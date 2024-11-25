package com.example.kedaiku.UI;

import android.Manifest;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.kedaiku.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class StoreDetailsActivity extends AppCompatActivity {

    private static final int REQUEST_CAMERA_PERMISSION = 1;
    private EditText storeNameEditText;
    private EditText storeAddressEditText;
    private EditText storePhoneEditText;
    private EditText storeEmailEditText;
    private ImageView storeImageView;
    private Button saveButton;
    private Button selectImageButton;
    private Uri imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_store_details);

        storeNameEditText = findViewById(R.id.storeNameEditText);
        storeAddressEditText = findViewById(R.id.storeAddressEditText);
        storePhoneEditText = findViewById(R.id.storePhoneEditText);
        storeEmailEditText = findViewById(R.id.storeEmailEditText);
        storeImageView = findViewById(R.id.storeImageView);
        saveButton = findViewById(R.id.saveButton);
        selectImageButton = findViewById(R.id.selectImageButton);

        saveButton.setOnClickListener(v -> saveStoreDetails());
        selectImageButton.setOnClickListener(v -> showImagePickerOptions());
    }

    private void showImagePickerOptions() {
        String[] options = {"Take Photo", "Choose from Gallery"};
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Select Image")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
                        } else {
                            takePhotoFromCamera();
                        }
                    } else {
                        choosePhotoFromGallery();
                    }
                }).show();
    }

    private void takePhotoFromCamera() {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "New Picture");
        values.put(MediaStore.Images.Media.DESCRIPTION, "From your Camera");
        imageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        takePictureLauncher.launch(imageUri);
    }

    private void choosePhotoFromGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galleryActivityResultLauncher.launch(intent);
    }

    private final ActivityResultLauncher<Uri> takePictureLauncher = registerForActivityResult(
            new ActivityResultContracts.TakePicture(),
            new ActivityResultCallback<Boolean>() {
                @Override
                public void onActivityResult(Boolean result) {
                    if (result) {
                        storeImageView.setImageURI(imageUri);
                        saveImageUri(imageUri);
                    } else {
                        Toast.makeText(StoreDetailsActivity.this, "Picture not taken!", Toast.LENGTH_SHORT).show();
                    }
                }
            });

    private final ActivityResultLauncher<Intent> galleryActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    if (result.getData() != null) {
                        imageUri = result.getData().getData();
                        try {
                            InputStream inputStream = getContentResolver().openInputStream(imageUri);
                            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                            Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, 250, 250, true);
                            storeImageView.setImageBitmap(scaledBitmap);

                            saveImageToStorage(scaledBitmap);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });

    private void saveImageToStorage(Bitmap bitmap) {
        File directory = getExternalFilesDir("StoreImages");
        if (!directory.exists()) {
            directory.mkdirs();
        }

        File imageFile = new File(directory, "store_image.jpg");
        try (FileOutputStream fos = new FileOutputStream(imageFile)) {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            saveImageUri(Uri.fromFile(imageFile));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void saveImageUri(Uri uri) {
        SharedPreferences sharedPreferences = getSharedPreferences("MyStorePrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("store_image_uri", uri.toString());
        editor.apply();
    }

    private void saveStoreDetails() {
        String storeName = storeNameEditText.getText().toString();
        String storeAddress = storeAddressEditText.getText().toString();
        String storePhone = storePhoneEditText.getText().toString();
        String storeEmail = storeEmailEditText.getText().toString();

        SharedPreferences sharedPreferences = getSharedPreferences("MyStorePrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("store_name", storeName);
        editor.putString("store_address", storeAddress);
        editor.putString("store_phone", storePhone);
        editor.putString("store_email", storeEmail);
        editor.apply();

        Intent intent = new Intent(StoreDetailsActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                takePhotoFromCamera();
            } else {
                Toast.makeText(this, "Camera permission is required", Toast.LENGTH_SHORT).show();
            }
        }
    }
}


