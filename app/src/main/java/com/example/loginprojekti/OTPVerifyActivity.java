package com.example.loginprojekti;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import HelperClasses.EmailSender;

public class OTPVerifyActivity extends AppCompatActivity {

    private Button checkOTP;
    private EditText enterOTPText;
    private ProgressBar progressBar;
    private String userEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otpverify);

        checkOTP = findViewById(R.id.btnCheckOTP);
        enterOTPText = findViewById(R.id.editTextEnterOTP);
        progressBar = findViewById(R.id.progressBar);

        // Retrieve the email passed from LoginActivity
        userEmail = getIntent().getStringExtra("userEmail").trim();



        // Set click listener for the "Check OTP" button
        checkOTP.setOnClickListener(v -> {
            String enteredOTP = enterOTPText.getText().toString().trim();

            if (enteredOTP.isEmpty()) {
                Toast.makeText(OTPVerifyActivity.this, "Please enter the OTP", Toast.LENGTH_SHORT).show();
                return;
            }

            // Display progress bar while verifying
            progressBar.setVisibility(View.VISIBLE);

            // Check if the entered OTP matches the generated OTP stored in EmailSender
            boolean isOtpValid = EmailSender.validateOTP(userEmail, enteredOTP);

            if (isOtpValid) {
                // OTP verified successfully
                Toast.makeText(OTPVerifyActivity.this, "OTP Verified! Logging in...", Toast.LENGTH_SHORT).show();

                // Proceed to the next activity (MainActivity or whatever is next)
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
                finish();
            } else {
                // Invalid OTP
                Toast.makeText(OTPVerifyActivity.this, "Invalid OTP. Please try again.", Toast.LENGTH_SHORT).show();
            }

            // Hide progress bar after the process is completed
            progressBar.setVisibility(View.GONE);
        });
    }
}
