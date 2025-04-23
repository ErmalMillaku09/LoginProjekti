package com.example.loginprojekti;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    public static final String TAG = "TAG";
    TextView profileNameTextView, profileEmailTextView,verifyMsgTextView;
    Button returnToSignUp,resendCodeBtn;
    FirebaseAuth fAuth;
    FirebaseFirestore fStore;

    //Remove Listener Fix on user logout

    private ListenerRegistration profileListener;

    String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        returnToSignUp = findViewById(R.id.returnToSignup);
        profileNameTextView = findViewById(R.id.profileName);
        profileEmailTextView = findViewById(R.id.profileEmail);

        // Resend Verification email
        resendCodeBtn = findViewById(R.id.resendCodeBtn);
        verifyMsgTextView = findViewById(R.id.verifyMsg);
        //Get user instances for data init to display
        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();

        //Checking if user is loggedd in
        userId = Objects.requireNonNull(fAuth.getCurrentUser()).getUid();
        final FirebaseUser fUser= fAuth.getCurrentUser();
        if(!fUser.isEmailVerified()){
            resendCodeBtn.setVisibility(View.VISIBLE);
            verifyMsgTextView.setVisibility(View.VISIBLE);

            resendCodeBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View v) {

                    fUser.sendEmailVerification().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            resendCodeBtn.setVisibility(View.GONE);
                            verifyMsgTextView.setVisibility(View.GONE);
                            Toast.makeText(MainActivity.this, "Verification email has been sent", Toast.LENGTH_SHORT).show();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                            Log.d(TAG,"onFailure: Email not sent" + e.getMessage());

                        }
                    });
                }
            });
        }
        //Get user Document Ref
        DocumentReference documentReference = fStore.collection("users").document(userId);

        //Snapshot listener to get data changes in fire base DB

      profileListener =  documentReference.addSnapshotListener(this, new EventListener<DocumentSnapshot>() {

            //Null pointer in document snapshot fixed.
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException error) {
                if (error != null) {
                    Log.w("FireStore", "Snapshot listener failed", error);
                    return;
                }

                if (documentSnapshot != null && documentSnapshot.exists()) {
                    profileNameTextView.setText(documentSnapshot.getString("fName"));
                    profileEmailTextView.setText(documentSnapshot.getString("email"));
                } else {
                    Log.d("FireStore", "DocumentSnapshot is null or document doesn't exist");
                    profileNameTextView.setText("");  // Clear UI or show default
                    profileEmailTextView.setText("");
                }
            }
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;


        });

        returnToSignUp.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                if (profileListener != null) {
                    profileListener.remove();
                    profileListener = null;
                }

                FirebaseAuth.getInstance().signOut();

                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }
}