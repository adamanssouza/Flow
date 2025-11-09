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
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.canhub.cropper.CropImage;
import com.canhub.cropper.CropImageContract;
import com.canhub.cropper.CropImageContractOptions;
import com.canhub.cropper.CropImageOptions;
import com.canhub.cropper.CropImageView;
import com.example.flow.R;
import de.hdodenhof.circleimageview.CircleImageView;

import java.io.InputStream;

public class ProfileActivity extends AppCompatActivity {

    private CircleImageView profileImageView;
    private EditText etProfileName, etProfileEmail;
    private Button btnSaveProfile;
    private Uri imageUri;

    private final ActivityResultLauncher<CropImageContractOptions> cropImageLauncher = registerForActivityResult(
            new CropImageContract(),
            result -> {
                if (result.isSuccessful()) {
                    imageUri = result.getUriContent();
                    profileImageView.setImageURI(imageUri);
                } else {
                    Toast.makeText(this, "Corte de imagem cancelado.", Toast.LENGTH_SHORT).show();
                }
            });

    private final ActivityResultLauncher<String> requestPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            isGranted -> {
                if (isGranted) {
                    startImageCrop();
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

        profileImageView.setOnClickListener(v -> showImageOptionsDialog());

        btnSaveProfile.setOnClickListener(v -> saveProfileData());
    }

    private void showImageOptionsDialog() {
        final CharSequence[] options = {"Escolher nova foto", "Remover foto", "Cancelar"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Foto de Perfil");
        builder.setItems(options, (dialog, item) -> {
            if (options[item].equals("Escolher nova foto")) {
                checkPermissionAndStartCrop();
            } else if (options[item].equals("Remover foto")) {
                removeProfileImage();
            } else if (options[item].equals("Cancelar")) {
                dialog.dismiss();
            }
        });
        builder.show();
    }

    private void removeProfileImage() {
        imageUri = null;
        profileImageView.setImageResource(R.drawable.ic_person);
    }

    private void checkPermissionAndStartCrop() {
        String permission;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permission = Manifest.permission.READ_MEDIA_IMAGES;
        } else {
            permission = Manifest.permission.READ_EXTERNAL_STORAGE;
        }

        if (ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED) {
            startImageCrop();
        } else {
            requestPermissionLauncher.launch(permission);
        }
    }

    private void startImageCrop() {
        CropImageOptions cropOptions = new CropImageOptions();
        cropOptions.cropShape = CropImageView.CropShape.OVAL;
        cropOptions.aspectRatioX = 1;
        cropOptions.aspectRatioY = 1;
        cropOptions.guidelines = CropImageView.Guidelines.ON;
        cropOptions.fixAspectRatio = true;

        CropImageContractOptions contractOptions = new CropImageContractOptions(null, cropOptions);
        cropImageLauncher.launch(contractOptions);
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
                profileImageView.setImageURI(storedUri);
                imageUri = storedUri;
        } else {
            profileImageView.setImageResource(R.drawable.ic_person);
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
