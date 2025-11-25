package com.example.hcdcevents.feature.auth;

import static android.content.ContentValues.TAG;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.hcdcevents.data.model.Student;
import com.example.hcdcevents.ui.home.ProfileSetupActivity;
import com.example.hcdcevents.utils.Helper;
import com.example.hcdcevents.ui.home.HomePageActivity;
import com.example.hcdcevents.R;
import com.example.hcdcevents.data.model.StudentCache;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class SignInActivity extends AppCompatActivity {
    private TextInputEditText passwordInputText, emailInputText;
    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;
    DatabaseReference databaseRef ;
    private static final int MAX_RETRIES = 3;
    private static final long RETRY_DELAY_MS = 500;

//  TODO: Change Course to Program

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sign_in);

//       ==================================================
        mAuth = FirebaseAuth.getInstance();
        databaseRef = FirebaseDatabase.getInstance().getReference("students");

        emailInputText = findViewById(R.id.email_input_text);
        passwordInputText = findViewById(R.id.password_input_text);

        findViewById(R.id.sign_up_page_button).setOnClickListener(v -> redirectToSignUpPage());
        findViewById(R.id.google_sign_in_button).setOnClickListener(v -> signInWithGoogle());
        findViewById(R.id.sign_in_button).setOnClickListener(v -> loginStatus());

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
//      ==================================================
    }

    private void login(String email, String password){
        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this, task -> {
            if (task.isSuccessful()) {
                Log.d(TAG, "signInWithEmail:success");
                String userId = mAuth.getUid();
                onSignInSuccess(userId);
            } else {
                Log.w(TAG, "signInWithEmail:failure", task.getException());
                Toast.makeText(SignInActivity.this, "Account does not exist.", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void loginStatus(){
        CharSequence emailText = emailInputText.getText();
        CharSequence passwordText = passwordInputText.getText();

        if(emailText == null || emailText.length() == 0){
            Toast.makeText(this, "Email field cannot be empty.", Toast.LENGTH_LONG).show();
            return;
        }

        if(passwordText == null || passwordText.length() == 0){
            Toast.makeText(this, "Password field cannot be empty.", Toast.LENGTH_SHORT).show();
            return;
        }

        final String userEmail = emailText.toString().trim();
        final String userPass = passwordText.toString().trim();

        if(Helper.isValidHCDCEmail(userEmail)){
            Toast.makeText(this, "Use HCDC email only.", Toast.LENGTH_SHORT).show();
            return;
        }

        login(userEmail, userPass);
    }

    private void signInWithGoogle(){
        Intent intent = mGoogleSignInClient.getSignInIntent();
        googleSignInLauncher.launch(intent);
    }

    private final ActivityResultLauncher<Intent> googleSignInLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result ->{
                if(result.getResultCode() == Activity.RESULT_OK){
                    Intent data = result.getData();
                    Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                    try {
                        GoogleSignInAccount account = task.getResult(ApiException.class);
                        firebaseAuthWithGoogle(account);
                    } catch (ApiException e) {
                        Log.w(TAG, "Google Sign in failed.", e);
                        Toast.makeText(this, "Google Sign in failed.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(this, "Sign-in cancelled.", Toast.LENGTH_SHORT).show();
                }
            }
    );

    private void firebaseAuthWithGoogle(GoogleSignInAccount acc) {
        String email = acc.getEmail();
        String SCHOOL_DOMAIN = "@hcdc.edu.ph";

        if(email != null && email.toLowerCase().endsWith(SCHOOL_DOMAIN.toLowerCase())){
            AuthCredential credential = GoogleAuthProvider.getCredential(acc.getIdToken(), null);

            mAuth.signInWithCredential(credential)
                    .addOnCompleteListener(this, task -> {
                        if(task.isSuccessful()){
                            FirebaseUser firebaseUser = mAuth.getCurrentUser();
                            if(firebaseUser != null){
                                String userId = firebaseUser.getUid();
                                isFirstTimeLoggedInUsingGmail(userId, firebaseUser.getEmail(),firebaseUser.getDisplayName());
                            } else {
                                Toast.makeText(this, "Users firebase is null.", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(this, "Authentication Failed!", Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            googleSignOutUser();
            Toast.makeText(this, "Access Denied: Use official email to enter.", Toast.LENGTH_SHORT).show();
        }
    }

    private void onSignInSuccess(final String uid ) {
        showLoadingIndicator(true);
        if (uid == null) return;

        databaseRef.child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if (!snapshot.exists()) {
                    FirebaseAuth.getInstance().signOut();
                    googleSignOutUser();
                    Toast.makeText(SignInActivity.this, "Account data not found. Please register first.", Toast.LENGTH_LONG).show();
                    showLoadingIndicator(false);
                    return;
                }

                String finalAcademicDivision = snapshot.child("academicDivision").getValue(String.class);

                String finalName = snapshot.child("name").getValue(String.class);
                String finalEmail = snapshot.child("email").getValue(String.class);
                String finalKey = snapshot.getKey();
                Boolean dbAdminStatus = snapshot.child("isAdmin").getValue(Boolean.class);
                boolean isAdmin = (dbAdminStatus != null) ? dbAdminStatus : false;
                Drawable finalIcon = Helper.iconGenerator(finalName);
                StudentCache.setStudentData(
                        finalName,
                        finalEmail,
                        finalAcademicDivision,
                        isAdmin,
                        finalKey,
                        finalIcon
                );

                if(finalAcademicDivision == null || finalAcademicDivision.trim().isEmpty()){
                    showLoadingIndicator(false);
                    redirectToProfileSetup();
                    finish();
                }

                 else {
                    Log.i("SignInActivity", "Program data found on attempt. Proceeding to Home.");
                    showLoadingIndicator(false);
                    startActivity(new Intent(SignInActivity.this, HomePageActivity.class));
                    finish();
                }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("Login", "Data check failed: " + databaseError.getMessage());
                Toast.makeText(SignInActivity.this, "Authentication failed. Please try again.", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void isFirstTimeLoggedInUsingGmail(String uid, String email, String name ){
        if(uid == null) return;
        databaseRef.child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(!snapshot.exists()){
                    Student newStudent = new Student(email, name, null, false);
                    databaseRef.child(uid).setValue(newStudent).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            if(task.isSuccessful()){
                                onSignInSuccess(uid);
                            } else {
                                showLoadingIndicator(false);
                                Log.w(TAG, "signInWithGmail:failure", task.getException());
                                Toast.makeText(SignInActivity.this, "Failed to generate profile automatically.",
                                        Toast.LENGTH_SHORT).show();
                            }
                        }

                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(SignInActivity.this, "Error saving profile data.", Toast.LENGTH_LONG).show();
                        }
                    });
                    return;
                }

                onSignInSuccess(uid);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("Login", "Data creation failed: " + databaseError.getMessage());
                Toast.makeText(SignInActivity.this, "Authentication failed. Please try again.", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void googleSignOutUser(){
        mGoogleSignInClient.signOut();
        mGoogleSignInClient.revokeAccess();
    }

    private void redirectToProfileSetup(){
        startActivity(new Intent(this, ProfileSetupActivity.class));
        finish();
    }

    private void redirectToSignUpPage(){
        startActivity(new Intent(SignInActivity.this, SignUpActivity.class));
        finish();
    }

    private void showLoadingIndicator(boolean show) {
        LinearLayout loadingOverlay = findViewById(R.id.loading_overlay);

        if (loadingOverlay != null) {
            loadingOverlay.setVisibility(show ? View.VISIBLE : View.GONE);
            loadingOverlay.setVisibility(show? View.VISIBLE: View.GONE);
            findViewById(R.id.sign_in_button).setEnabled(!show);
        }
    }

}