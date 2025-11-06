package com.example.flow.ui;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.flow.R;

import java.io.InputStream;

public class ProfileActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;
    private ImageView profileImageView;
    private EditText etProfileName, etProfileEmail;
    private Button btnSaveProfile;
    private Uri imageUri;

    private final ActivityResultLauncher<String> requestPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            isGranted -> {
                if (isGranted) {
                    openFileChooser();
                } else {
                    Toast.makeText(this, "Permissão para acessar galeria foi negada.", Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        profileImageView = findViewById(R.id.profile_image_large);
        etProfileName = findViewById(R.id.edit_text_name);
        etProfileEmail = findViewById(R.id.edit_text_email);
        btnSaveProfile = findViewById(R.id.button_save);

        loadProfileData();

        profileImageView.setOnClickListener(v -> checkPermissionAndOpenFileChooser());

        btnSaveProfile.setOnClickListener(v -> saveProfileData());
    }

    private void checkPermissionAndOpenFileChooser() {
        String permission;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permission = Manifest.permission.READ_MEDIA_IMAGES;
        } else {
            permission = Manifest.permission.READ_EXTERNAL_STORAGE;
        }

        if (ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED) {
            openFileChooser();
        } else {
            requestPermissionLauncher.launch(permission);
        }
    }

    private void openFileChooser() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            final int takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION;
            try {
                getContentResolver().takePersistableUriPermission(imageUri, takeFlags);
                profileImageView.setImageURI(imageUri);
            } catch (SecurityException e) {
                Toast.makeText(this, "Não foi possível obter permissão para a imagem.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void loadProfileData() {
        SharedPreferences prefs = getSharedPreferences("profile", Context.MODE_PRIVATE);
        String name = prefs.getString("name", "");
        String email = prefs.getString("email", "");
        String imageUriString = prefs.getString("imageUri", null);

        etProfileName.setText(name);
        etProfileEmail.setText(email);

        if (imageUriString != null) {
            Uri storedUri = Uri.parse(imageUriString);
            if (canReadUri(storedUri)) {
                profileImageView.setImageURI(storedUri);
                imageUri = storedUri;
            } else {
                profileImageView.setImageResource(R.drawable.ic_person);
                prefs.edit().remove("imageUri").apply();
            }
        } else {
            profileImageView.setImageResource(R.drawable.ic_person);
        }
    }

    private boolean canReadUri(Uri uri) {
        try (InputStream ignored = getContentResolver().openInputStream(uri)) {
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private void saveProfileData() {
        SharedPreferences prefs = getSharedPreferences("profile", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        editor.putString("name", etProfileName.getText().toString());
        editor.putString("email", etProfileEmail.getText().toString());

        if (imageUri != null) {
            editor.putString("imageUri", imageUri.toString());
        } else {
            editor.remove("imageUri");
        }

        editor.apply();
        Toast.makeText(this, "Perfil salvo com sucesso!", Toast.LENGTH_SHORT).show();
        finish();
    }
}
