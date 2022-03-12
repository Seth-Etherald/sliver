package com.example.sliver;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessaging;

@SuppressWarnings("deprecation")
public class LoginActivity extends AppCompatActivity {

  private FirebaseAuth myAuth;
  private Button loginButton, registerScreenButton, phoneLoginButton, forgotPasswordButton;
  private TextInputLayout userEmail, userPassword;
  private ProgressDialog progressDialog;
  private DatabaseReference usersRef;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_login);

    myAuth = FirebaseAuth.getInstance();
    usersRef =
        FirebaseDatabase.getInstance(
                "https://sliver-b6693-default-rtdb.asia-southeast1.firebasedatabase.app/")
            .getReference()
            .child("users");

    initializeFields();

    registerScreenButton.setOnClickListener(view -> sendUserToRegisterActivity());
    loginButton.setOnClickListener(view -> login());
    phoneLoginButton.setOnClickListener(view -> sendUserToPhoneLoginActivity());
  }

  private void login() {
    var email = userEmail.getEditText().getText().toString().trim();
    var password = userPassword.getEditText().getText().toString();
    String EMAIL_VERIFICATION = "^([\\w-.]+){1,64}@([\\w&&[^_]]+){2,255}.[a-z]{2,}$";
    if (TextUtils.isEmpty(email)) {
      Toast.makeText(this, "Please enter an email!", Toast.LENGTH_SHORT).show();
    } else if (!email.matches(EMAIL_VERIFICATION)) {
      Toast.makeText(this, "Please enter a valid email!", Toast.LENGTH_SHORT).show();
    } else if (TextUtils.isEmpty(password)) {
      Toast.makeText(this, "Please enter your password!", Toast.LENGTH_SHORT).show();
    } else if (password.length() < 6) {
      Toast.makeText(this, "Password needs to be longer than 6 characters!", Toast.LENGTH_SHORT)
          .show();
    } else {
      progressDialog.setTitle("Logging in");
      progressDialog.setCanceledOnTouchOutside(true);
      progressDialog.show();
      myAuth
          .signInWithEmailAndPassword(email, password)
          .addOnCompleteListener(
              task -> {
                if (task.isSuccessful()) {
                  String currentUserId = myAuth.getCurrentUser().getUid();
                  FirebaseMessaging.getInstance()
                      .getToken()
                      .addOnSuccessListener(
                          s -> {
                            if (s != null) {
                              usersRef
                                  .child(currentUserId)
                                  .child("device_token")
                                  .setValue(s)
                                  .addOnCompleteListener(
                                      task1 -> {
                                        if (task1.isSuccessful()) {
                                          sendUserToMainActivity();
                                          Toast.makeText(
                                                  LoginActivity.this,
                                                  "Welcome!",
                                                  Toast.LENGTH_SHORT)
                                              .show();
                                        }
                                      });
                            }
                          });
                } else {
                  String errorMessage = task.getException().getMessage();
                  Toast.makeText(LoginActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                }
                progressDialog.dismiss();
              });
    }
  }

  private void initializeFields() {
    loginButton = findViewById(R.id.login_button);
    forgotPasswordButton = findViewById(R.id.forgot_password);
    registerScreenButton = findViewById(R.id.register_screen_button);
    phoneLoginButton = findViewById(R.id.phone_login_button);
    userEmail = findViewById(R.id.login_email);
    userPassword = findViewById(R.id.login_password);
    progressDialog = new ProgressDialog(this);
  }

  private void sendUserToMainActivity() {
    Intent mainIntent = new Intent(LoginActivity.this, MainActivity.class);
    mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
    startActivity(mainIntent);
    finish();
  }

  private void sendUserToRegisterActivity() {
    Intent registerIntent = new Intent(LoginActivity.this, RegisterActivity.class);
    startActivity(registerIntent);
  }

  private void sendUserToPhoneLoginActivity() {
    Intent phoneLoginIntent = new Intent(LoginActivity.this, PhoneLoginActivity.class);
    startActivity(phoneLoginIntent);
  }
}
