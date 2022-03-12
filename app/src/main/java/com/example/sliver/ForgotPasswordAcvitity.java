package com.example.sliver;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;

public class ForgotPasswordAcvitity extends AppCompatActivity {
  private TextInputLayout resetEmailInput;
  private FirebaseAuth myAuth;
  private Button sendResetEmailButton, returnToLoginButton;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_forgot_password);

    myAuth = FirebaseAuth.getInstance();

    initializeFields();

    sendResetEmailButton.setOnClickListener(view -> sendResetEmail());

    returnToLoginButton.setOnClickListener(view -> sendUserToLogin());
  }

  private void sendResetEmail() {
    var inputEmail = resetEmailInput.getEditText().getText().toString();
    String EMAIL_VERIFICATION = "^([\\w-.]+){1,64}@([\\w&&[^_]]+){2,255}.[a-z]{2,}$";
    if (TextUtils.isEmpty(inputEmail)) {
      Toast.makeText(this, "Please enter an email!", Toast.LENGTH_SHORT).show();
    } else if (!inputEmail.matches(EMAIL_VERIFICATION)) {
      Toast.makeText(this, "Please enter a valid email!", Toast.LENGTH_SHORT).show();
    } else {
      myAuth
          .sendPasswordResetEmail(inputEmail)
          .addOnCompleteListener(
              task -> {
                if (task.isSuccessful()) {
                  Toast.makeText(
                          ForgotPasswordAcvitity.this,
                          "Email sent! Please check your inbox!",
                          Toast.LENGTH_SHORT)
                      .show();
                  sendUserToLogin();
                } else {
                  String errorMessage = task.getException().getMessage();
                  Toast.makeText(ForgotPasswordAcvitity.this, errorMessage, Toast.LENGTH_SHORT)
                      .show();
                }
              });
    }
  }

  private void initializeFields() {
    resetEmailInput = findViewById(R.id.reset_password_email);
    sendResetEmailButton = findViewById(R.id.reset_password_button);
    returnToLoginButton = findViewById(R.id.return_login_screen_button);
  }

  private void sendUserToLogin() {
    Intent loginIntent = new Intent(ForgotPasswordAcvitity.this, LoginActivity.class);
    startActivity(loginIntent);
    finish();
  }
}
