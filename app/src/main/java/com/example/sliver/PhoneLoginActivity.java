package com.example.sliver;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.auth.PhoneAuthProvider.OnVerificationStateChangedCallbacks;

import java.util.concurrent.TimeUnit;

public class PhoneLoginActivity extends AppCompatActivity {
    private TextInputLayout phoneInput, verificationCodeInput;
    private Button sendVerificationCodeButton, loginScreenButton, verifyButton;
    private FirebaseAuth myAuth;
    private OnVerificationStateChangedCallbacks callbacks;
    private String mVerificationId;
    private PhoneAuthProvider.ForceResendingToken mResendToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_login);

        myAuth = FirebaseAuth.getInstance();

        initializeFields();

        sendVerificationCodeButton.setOnClickListener(
                view -> {
                    String phoneInputValue = phoneInput.getEditText().getText().toString();
                    if (TextUtils.isEmpty(phoneInputValue)) {
                        Toast.makeText(
                                        PhoneLoginActivity.this,
                                        "Please enter your phone number!",
                                        Toast.LENGTH_SHORT)
                                .show();
                    } else {
                        phoneInput.setEnabled(false);
                        PhoneAuthOptions options =
                                PhoneAuthOptions.newBuilder(myAuth)
                                        .setPhoneNumber(phoneInputValue)
                                        .setTimeout(120L, TimeUnit.SECONDS)
                                        .setActivity(PhoneLoginActivity.this)
                                        .setCallbacks(callbacks)
                                        .build();
                        PhoneAuthProvider.verifyPhoneNumber(options);
                    }
                });

        verifyButton.setOnClickListener(
                view -> {
                    sendVerificationCodeButton.setVisibility(View.GONE);
                    String verificationCode =
                            verificationCodeInput.getEditText().getText().toString().trim();

                    if (TextUtils.isEmpty(verificationCode)) {
                        Toast.makeText(
                                        this,
                                        "Please enter the verification code!",
                                        Toast.LENGTH_SHORT)
                                .show();
                    } else {
                        PhoneAuthCredential credential =
                                PhoneAuthProvider.getCredential(mVerificationId, verificationCode);
                        signInWithPhoneAuthCredential(credential);
                    }
                });
        callbacks =
                new OnVerificationStateChangedCallbacks() {
                    @Override
                    public void onVerificationCompleted(
                            @NonNull PhoneAuthCredential phoneAuthCredential) {
                        signInWithPhoneAuthCredential(phoneAuthCredential);
                    }

                    @Override
                    public void onVerificationFailed(@NonNull FirebaseException e) {
                        Toast.makeText(
                                        PhoneLoginActivity.this,
                                        "Please enter correct phone number with your country code!",
                                        Toast.LENGTH_LONG)
                                .show();
                        phoneInput.setEnabled(true);
                        sendVerificationCodeButton.setVisibility(View.VISIBLE);
                        verificationCodeInput.setVisibility(View.GONE);
                        verifyButton.setVisibility(View.GONE);
                    }

                    @Override
                    public void onCodeSent(
                            @NonNull String verificationId,
                            @NonNull PhoneAuthProvider.ForceResendingToken token) {
                        mVerificationId = verificationId;
                        mResendToken = token;

                        Toast.makeText(
                                        PhoneLoginActivity.this,
                                        "Code has been sent, please wait a bit",
                                        Toast.LENGTH_SHORT)
                                .show();
                        phoneInput.setEnabled(false);
                        sendVerificationCodeButton.setVisibility(View.GONE);
                        verificationCodeInput.setVisibility(View.VISIBLE);
                        verifyButton.setVisibility(View.VISIBLE);
                    }
                };

        loginScreenButton.setOnClickListener(view -> sendUserToLoginActivity());
    }

    private void initializeFields() {
        phoneInput = findViewById(R.id.phone_number_input);
        verificationCodeInput = findViewById(R.id.verification_code_input);
        sendVerificationCodeButton = findViewById(R.id.send_verification_button);
        loginScreenButton = findViewById(R.id.have_account_button_phone);
        verifyButton = findViewById(R.id.verify_button);
    }

    private void sendUserToLoginActivity() {
        Intent loginIntent = new Intent(PhoneLoginActivity.this, LoginActivity.class);
        startActivity(loginIntent);
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        myAuth.signInWithCredential(credential)
                .addOnCompleteListener(
                        this,
                        task -> {
                            if (task.isSuccessful()) {
                                sendUserToMainActivity();
                            } else {
                                String message = task.getException().toString();
                                Toast.makeText(PhoneLoginActivity.this, message, Toast.LENGTH_SHORT)
                                        .show();
                            }
                        });
    }

    private void sendUserToMainActivity() {
        Intent mainIntent = new Intent(PhoneLoginActivity.this, MainActivity.class);
        startActivity(mainIntent);
        finish();
    }
}
