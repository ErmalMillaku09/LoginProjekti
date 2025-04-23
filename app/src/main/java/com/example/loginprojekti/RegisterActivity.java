package com.example.loginprojekti;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import HelperClasses.PasswordValidator;

public class RegisterActivity extends AppCompatActivity {

    public static final String TAG = "TAG";
    EditText nameEditText, emailEditText, passwordEditText, confirmPasswordEditText;
    Button signUpButton;
    TextView loginBText;

    FirebaseAuth fAuth;
    ProgressBar progressBar;
    String userID;
    FirebaseFirestore fStore;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);

        nameEditText = findViewById(R.id.editTextName);
        emailEditText = findViewById(R.id.editTextEmail);
        passwordEditText = findViewById(R.id.editTextPassword);
        confirmPasswordEditText = findViewById(R.id.editTextConfirmPassword);

        loginBText = findViewById(R.id.TextViewLogin);
        signUpButton = findViewById(R.id.buttonSignUp);

        fAuth = FirebaseAuth.getInstance();
        progressBar = findViewById(R.id.progressBar);

        fStore = FirebaseFirestore.getInstance();

        //Check if user is logged in or existing

        if (fAuth.getCurrentUser() != null) {
            startActivity(new Intent(getApplicationContext(), MainActivity.class));
            finish();
        } else {

            loginBText.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                    finish();
                }
            });

            signUpButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String email = emailEditText.getText().toString().trim();
                    String name = nameEditText.getText().toString().trim();
                    String password = passwordEditText.getText().toString();
                    String confirmPassword = confirmPasswordEditText.getText().toString();


                    if (TextUtils.isEmpty(name)) {
                        nameEditText.setError("Please enter your name");
                        return;
                    }
                    if (TextUtils.isEmpty(email)) {
                        emailEditText.setError("Please enter your email");
                        return;
                    }
                    if (TextUtils.isEmpty(password)) {
                        passwordEditText.setError("Please enter your password");
                        return;
                    }
                    if (TextUtils.isEmpty(confirmPassword)) {
                        confirmPasswordEditText.setError("Please enter your password");
                        return;
                    }

                    if (!PasswordValidator.isValidPassword(password)) {
                        Log.d("RegisterActivity", "Password: " + password);
                        passwordEditText.setError("Please enter a Password longer than 8, with one capital letter, one digit and one special symbol");
                        return;
                    }
                    if (!password.equals(confirmPassword)) {
                        confirmPasswordEditText.setError("Passwords must match");
                        return;
                    }

                    //Display progress bar
                    progressBar.setVisibility(View.VISIBLE);

                    //Register User on firebase
                    //User Registration Task
                    fAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {

                            if (task.isSuccessful()) {

                                // Sending Email verificatio section

                                FirebaseUser fUser = fAuth.getCurrentUser();
                                fUser.sendEmailVerification().addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        Toast.makeText(RegisterActivity.this, "Verification email has been sent", Toast.LENGTH_SHORT).show();
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {

                                    Log.d(TAG,"onFailure: Email not sent" + e.getMessage());

                                    }
                                });




                                Toast.makeText(RegisterActivity.this, "User Created Successfully", Toast.LENGTH_SHORT).show();

                                //Get UID for creating collection of documents (Document Reference)
                                userID = fAuth.getCurrentUser().getUid();
                                DocumentReference documentReference = fStore.collection("users").document(userID);

                                //Store Data using HashMap

                                Map<String, Object> user = new HashMap<>();
                            // Attribute name in green
                                user.put("fName", name);
                                user.put("email", email);

                                documentReference.set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        Log.d(TAG, "onSuccess: user Profile is created with ID" +userID );
                                    }
                                });
                                startActivity(new Intent(getApplicationContext(), MainActivity.class));
                                finish(); // Add finish to close the RegisterActivity
                            } else {
                                Toast.makeText(RegisterActivity.this, "Error! " + Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
                                progressBar.setVisibility(View.GONE);
                            }
                        }

                    });
                }

            });
        }
    }

}