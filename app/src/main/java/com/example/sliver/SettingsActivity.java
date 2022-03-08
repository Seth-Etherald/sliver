package com.example.sliver;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.bumptech.glide.Glide;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

@SuppressWarnings("deprecation")
public class SettingsActivity extends AppCompatActivity {

    private static final int galleryPick = 1;
    private Button updateAccountSettings;
    private TextInputLayout usernameInput, userStatusInput;
    private String profileImageUrl;
    private CircleImageView userProfileImage;
    private String currentUserID;
    private FirebaseAuth myAuth;
    private DatabaseReference rootRef;
    private ProgressDialog progressDialog;
    private StorageReference userProfileImageRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        myAuth = FirebaseAuth.getInstance();
        currentUserID = myAuth.getCurrentUser().getUid();
        rootRef =
                FirebaseDatabase.getInstance(
                                "https://sliver-b6693-default-rtdb.asia-southeast1.firebasedatabase.app/")
                        .getReference();
        initializeFields();
        userProfileImageRef = FirebaseStorage.getInstance().getReference().child("Profile Images");

        retrieveUserInfo();

        updateAccountSettings.setOnClickListener(view -> updateSettings());

        userProfileImage.setOnClickListener(
                view -> {
                    try {
                        if (ActivityCompat.checkSelfPermission(
                                        this, Manifest.permission.READ_EXTERNAL_STORAGE)
                                != PackageManager.PERMISSION_GRANTED) {
                            ActivityCompat.requestPermissions(
                                    this,
                                    new String[] {
                                        Manifest.permission.READ_EXTERNAL_STORAGE,
                                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                                    },
                                    galleryPick);
                        } else {
                            CropImage.activity()
                                    .setGuidelines(CropImageView.Guidelines.ON)
                                    .setAspectRatio(1, 1)
                                    .start(this);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
    }

    private void initializeFields() {
        updateAccountSettings = findViewById(R.id.update_settings_button);
        usernameInput = findViewById(R.id.set_username);
        userStatusInput = findViewById(R.id.set_status);
        userProfileImage = findViewById(R.id.profile_image);
        progressDialog = new ProgressDialog(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Uri resultUri = result.getUri();

                StorageReference filePath = userProfileImageRef.child(currentUserID + ".jpg");

                filePath.putFile(resultUri)
                        .addOnCompleteListener(
                                task -> {
                                    if (task.isSuccessful()) {
                                        filePath.getDownloadUrl()
                                                .addOnSuccessListener(
                                                        uri -> {
                                                            final String downloadUrl =
                                                                    uri.toString();
                                                            rootRef.child("users")
                                                                    .child(currentUserID)
                                                                    .child("image")
                                                                    .setValue(downloadUrl);
                                                            userProfileImage.setImageURI(uri);
                                                            Toast.makeText(
                                                                            SettingsActivity.this,
                                                                            "User profile image updated successfully!",
                                                                            Toast.LENGTH_SHORT)
                                                                    .show();
                                                        });
                                    } else {
                                        String errorMessage = task.getException().getMessage();
                                        Toast.makeText(
                                                        SettingsActivity.this,
                                                        errorMessage,
                                                        Toast.LENGTH_SHORT)
                                                .show();
                                    }
                                });
            }
        }
    }

    private void updateSettings() {
        String username = usernameInput.getEditText().getText().toString().trim();
        String userStatus = userStatusInput.getEditText().getText().toString().trim();

        if (TextUtils.isEmpty(username)) {
            Toast.makeText(this, "Please enter a valid username!", Toast.LENGTH_SHORT).show();
        } else {
            progressDialog.setMessage("Updating profile");
            progressDialog.setCanceledOnTouchOutside(true);
            progressDialog.show();

            HashMap<String, Object> profileMap = new HashMap<>();
            profileMap.put("uid", currentUserID);
            profileMap.put("name", username);
            profileMap.put("status", userStatus);
            profileMap.put("image", profileImageUrl);

            rootRef.child("users")
                    .child(currentUserID)
                    .setValue(profileMap)
                    .addOnCompleteListener(
                            task -> {
                                if (task.isSuccessful()) {
                                    Toast.makeText(
                                                    SettingsActivity.this,
                                                    "Profile updated successfully!",
                                                    Toast.LENGTH_SHORT)
                                            .show();
                                    sendUserToMainActivity();
                                } else {
                                    String errorMessage = task.getException().getMessage();
                                    Toast.makeText(
                                                    SettingsActivity.this,
                                                    errorMessage,
                                                    Toast.LENGTH_SHORT)
                                            .show();
                                }
                                progressDialog.dismiss();
                            });
        }
    }

    private void retrieveUserInfo() {
        rootRef.child("users")
                .child(currentUserID)
                .addValueEventListener(
                        new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if ((snapshot.exists())
                                        && (snapshot.hasChild("name"))
                                        && (snapshot.hasChild("image"))) {
                                    String retrieveUsername =
                                            snapshot.child("name").getValue().toString();
                                    String retrieveStatus =
                                            snapshot.child("status").getValue().toString();
                                    String retrieveProfileImage =
                                            snapshot.child("image").getValue().toString();
                                    profileImageUrl = retrieveProfileImage;

                                    usernameInput.getEditText().setText(retrieveUsername);
                                    userStatusInput.getEditText().setText(retrieveStatus);
                                    Glide.with(SettingsActivity.this)
                                            .load(retrieveProfileImage)
                                            .dontAnimate()
                                            .into(userProfileImage);

                                } else if ((snapshot.exists()) && (snapshot.hasChild("name"))) {
                                    String retrieveUsername =
                                            snapshot.child("name").getValue().toString();
                                    String retrieveStatus =
                                            snapshot.child("status").getValue().toString();

                                    usernameInput.getEditText().setText(retrieveUsername);
                                    userStatusInput.getEditText().setText(retrieveStatus);
                                } else {
                                    Toast.makeText(
                                                    SettingsActivity.this,
                                                    "You need an username to continue!",
                                                    Toast.LENGTH_SHORT)
                                            .show();
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {}
                        });
    }

    private void sendUserToMainActivity() {
        Intent mainIntent = new Intent(SettingsActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == galleryPick) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Intent galleryIntent =
                        new Intent(
                                Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(galleryIntent, galleryPick);
            } else {
                Toast.makeText(
                                this,
                                "Failed to read from storage, please grant permission!",
                                Toast.LENGTH_LONG)
                        .show();
            }
        }
    }
}
