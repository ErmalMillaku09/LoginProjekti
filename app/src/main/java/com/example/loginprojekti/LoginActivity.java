package com.example.loginprojekti;
import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import HelperClasses.EmailSender;
import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {

    EditText emailEditText, passwordEditText;
    Button loginButton;
    TextView signupBText,forgotText;

    FirebaseAuth fAuth;
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, 1);
            }
        }


        emailEditText = findViewById(R.id.editTextEmail);
        passwordEditText = findViewById(R.id.editTextPassword);
        loginButton = findViewById(R.id.buttonLogin);

        signupBText = findViewById(R.id.TextViewSignup);
        forgotText = findViewById(R.id.TextViewForgot);

        fAuth = FirebaseAuth.getInstance();
        progressBar = findViewById(R.id.progressBar);


        //Check if user is logged in or existing

        if (fAuth.getCurrentUser() != null) {
            startActivity(new Intent(getApplicationContext(), MainActivity.class));
            finish();
        } else {


            signupBText.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(getApplicationContext(), RegisterActivity.class));
                    finish();
                }
            });


            //Forgot Password Block

            forgotText.setOnClickListener(new View.OnClickListener() {

                //Creating new alert dialog for password recovery
                @Override
                public void onClick(View v) {
                    EditText resetMail = new EditText(v.getContext());

                    AlertDialog.Builder passwordResetDialog = new AlertDialog.Builder(v.getContext());

                    passwordResetDialog.setTitle("Reset Forgotten Password");
                    passwordResetDialog.setMessage("Enter an existing account email to receive password reset link");

                    passwordResetDialog.setView(resetMail);


                    //Password Resetting
                    passwordResetDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //TODO extract email and send link
                            //USing FAuth we reset

                            String resetEmail = resetMail.getText().toString().trim();
                            fAuth.sendPasswordResetEmail(resetEmail).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {

                                    Toast.makeText(LoginActivity.this, "Reset Link sent to: "+ resetEmail, Toast.LENGTH_SHORT ).show();
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(LoginActivity.this, "Error link has not been sent!" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });


                        }
                    });

                    passwordResetDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //Close dialog
                        }
                    });

                    passwordResetDialog.create().show();
                }
            });

            loginButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String email = emailEditText.getText().toString().trim();
                    String password = passwordEditText.getText().toString();

                    if (TextUtils.isEmpty(email)) {
                        emailEditText.setError("Please enter your email");
                        return;
                    }
                    if (TextUtils.isEmpty(password)) {
                        passwordEditText.setError("Please enter your password");
                        return;
                    }

                    progressBar.setVisibility(View.VISIBLE);


                    //User Authentication
                    fAuth.signInWithEmailAndPassword(email, password)
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    progressBar.setVisibility(View.GONE);
                                    if (task.isSuccessful()) {
                                        Toast.makeText(LoginActivity.this, "Logged In" , Toast.LENGTH_SHORT).show();

                                        String userEmail = email;
                                        try {
                                            sendOTPEmail(userEmail);
                                            createNotificationChannel();
                                            showOTPNotification();
                                        } catch (IllegalArgumentException e) {
                                            // Handle invalid email format
                                            Toast.makeText(LoginActivity.this, "Invalid email format. Please check the email and try again.", Toast.LENGTH_LONG).show();
                                            e.printStackTrace();
                                        } catch (Exception e) {
                                            // Handle any other general errors
                                            Toast.makeText(LoginActivity.this, "An error occurred while sending the OTP. Please try again later.", Toast.LENGTH_LONG).show();
                                            e.printStackTrace();
                                        }


                                        Intent intent = new Intent(getApplicationContext(), OTPVerifyActivity.class);
                                        intent.putExtra("userEmail", userEmail); // Pass the email to the next activity
                                        startActivity(intent);
                                        finish();
                                    } else {
                                        Toast.makeText(LoginActivity.this, " " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                }



                            });
                }
            });

        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "OTPChannel";
            String description = "Channel for OTP notifications";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("otp_channel_id", name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    private void showOTPNotification() {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "otp_channel_id")
                .setSmallIcon(R.mipmap.ic_notification) // Use your own icon
                .setContentTitle("OTP Sent")
                .setContentText("Your OTP has been sent to your email.")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);

        // Check permission for Android 13+
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            notificationManager.notify(1001, builder.build());
        }
    }
    private void sendOTPEmail(String recipientEmail) {
        // Create an instance of the EmailSender class
        EmailSender emailSender = new EmailSender();

        // You can now pass the user email to the sendEmail method
        emailSender.sendEmail(recipientEmail);
    }
}

//#Hustlinsinceday1