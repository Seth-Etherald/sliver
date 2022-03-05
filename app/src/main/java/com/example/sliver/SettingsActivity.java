package com.example.sliver;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

@SuppressWarnings("deprecation")
public class SettingsActivity extends AppCompatActivity {

    private Button updateAccountSettings;
    private TextInputLayout usernameInput, userStatusInput;
    private CircleImageView userProfileImage;
    private String currentUserID;
    private FirebaseAuth myAuth;
    private DatabaseReference rootRef;
    private ProgressDialog progressDialog;

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

        updateAccountSettings.setOnClickListener(view -> updateSettings());

        retrieveUserInfo();
    }

    private void initializeFields() {
        updateAccountSettings = findViewById(R.id.update_settings_button);
        usernameInput = findViewById(R.id.set_username);
        userStatusInput = findViewById(R.id.set_status);
        userProfileImage = findViewById(R.id.profile_image);
        progressDialog = new ProgressDialog(this);
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

                                    usernameInput.getEditText().setText(retrieveUsername);
                                    userStatusInput.getEditText().setText(retrieveStatus);

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
}
