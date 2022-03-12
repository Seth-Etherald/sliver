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

@SuppressWarnings("deprecation")
public class RegisterActivity extends AppCompatActivity {
  private FirebaseAuth myAuth;
  private DatabaseReference rootRef;
  private TextInputLayout registerEmail, confirmEmail, registerPassword, confirmPassword;
  private Button registerButton, loginScreenButton;
  private ProgressDialog progressDialog;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_register);

    myAuth = FirebaseAuth.getInstance();
    rootRef =
        FirebaseDatabase.getInstance(
                "https://sliver-b6693-default-rtdb.asia-southeast1.firebasedatabase.app/")
            .getReference();

    initializeFields();

    loginScreenButton.setOnClickListener(view -> sendUserToLoginActivity());
    registerButton.setOnClickListener(view -> createNewAccount());
  }

  private void createNewAccount() {
    var email = registerEmail.getEditText().getText().toString().trim();
    var reinputEmail = confirmEmail.getEditText().getText().toString().trim();
    var password = registerPassword.getEditText().getText().toString();
    var reinputPassword = confirmPassword.getEditText().getText().toString();
    String EMAIL_VERIFICATION = "^([\\w-.]+){1,64}@([\\w&&[^_]]+){2,255}.[a-z]{2,}$";
    if (TextUtils.isEmpty(email)) {
      Toast.makeText(this, "Please enter an email!", Toast.LENGTH_SHORT).show();
    } else if (!email.equals(reinputEmail)) {
      Toast.makeText(this, "Please re-enter your email!", Toast.LENGTH_SHORT).show();
    } else if (!email.matches(EMAIL_VERIFICATION)) {
      Toast.makeText(this, "Please enter a valid email!", Toast.LENGTH_SHORT).show();
    } else if (TextUtils.isEmpty(password)) {
      Toast.makeText(this, "Please enter your password!", Toast.LENGTH_SHORT).show();
    } else if (password.length() < 6) {
      Toast.makeText(this, "Password needs to be longer than 6 characters!", Toast.LENGTH_SHORT)
          .show();
    } else if (!password.equals(reinputPassword)) {
      Toast.makeText(this, "Please re-enter your password!", Toast.LENGTH_SHORT).show();
    } else {
      progressDialog.setMessage("Creating new account");
      progressDialog.setCanceledOnTouchOutside(true);
      progressDialog.show();

      myAuth
          .createUserWithEmailAndPassword(email, password)
          .addOnCompleteListener(
              task -> {
                if (task.isSuccessful()) {
                  String currentUID = myAuth.getCurrentUser().getUid();
                  rootRef.child("users").child(currentUID).setValue("");
                  sendUserToMainActivity();
                  Toast.makeText(
                          RegisterActivity.this,
                          "Account created successfully!",
                          Toast.LENGTH_SHORT)
                      .show();
                } else {
                  String errorMessage = task.getException().getMessage();
                  Toast.makeText(RegisterActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                }
                progressDialog.dismiss();
              });
    }
  }

  private void sendUserToLoginActivity() {
    Intent loginIntent = new Intent(RegisterActivity.this, LoginActivity.class);
    startActivity(loginIntent);
    finish();
  }

  private void initializeFields() {
    registerEmail = findViewById(R.id.register_email);
    confirmEmail = findViewById(R.id.re_register_email);
    registerPassword = findViewById(R.id.register_password);
    confirmPassword = findViewById(R.id.re_register_password);
    registerButton = findViewById(R.id.register_button);
    loginScreenButton = findViewById(R.id.have_account_button);
    progressDialog = new ProgressDialog(this);
  }

  private void sendUserToMainActivity() {
    Intent mainIntent = new Intent(RegisterActivity.this, MainActivity.class);
    mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
    startActivity(mainIntent);
    finish();
  }
}
