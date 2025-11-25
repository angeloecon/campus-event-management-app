package com.example.hcdcevents.feature.auth;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.hcdcevents.R;
import com.example.hcdcevents.data.model.Student;
import com.example.hcdcevents.utils.Helper;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Arrays;
import java.util.List;

public class SignUpActivity extends AppCompatActivity {
    private TextInputEditText passwordInputText, emailInputText;
    private AutoCompleteTextView academicDivisionDropdown;
    private Button signUpButton;
    private FirebaseAuth mAuth;
    private DatabaseReference databaseRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sign_up);

        academicDivisionDropdown = findViewById(R.id.program_dropdown_edit_text);
        int layoutID = R.layout.dropdown_menu_item;
        Helper.academicDivisionDropdown(academicDivisionDropdown, this,layoutID);

        mAuth = FirebaseAuth.getInstance();
        databaseRef = FirebaseDatabase.getInstance().getReference("students");

        emailInputText = findViewById(R.id.email_input_text);
        passwordInputText = findViewById(R.id.password_input_text);

        findViewById(R.id.sign_up_button).setOnClickListener(v -> signUpStatus());
        findViewById(R.id.log_in_page_button).setOnClickListener(v -> redirecToSignInPage());
    }

    private void signUpStatus(){
        String studentEmail = emailInputText.getText().toString().trim();
        String studentPass = passwordInputText.getText().toString().trim();
        String studentProgram = academicDivisionDropdown.getText().toString();

        if(studentPass.isEmpty() || studentEmail.isEmpty() || studentProgram.isEmpty()){
            Toast.makeText(SignUpActivity.this, "All fields are required.", Toast.LENGTH_SHORT).show();
            return;
        }

        if(Helper.isValidHCDCEmail(studentEmail)){
            emailInputText.setError("Use your official HCDC email.");
            return;
        }

        signUp(studentEmail, studentPass, studentProgram);
    }

    private void redirecToSignInPage(){
        startActivity(new Intent(SignUpActivity.this, SignInActivity.class));
        finish();
    }

    private void signUp(String email, String password, String academicDivision){
        showLoadingIndicator(true);
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "signInWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            if(user != null){
                                String studentUID = user.getUid();
                                Student newStudent = new Student(email, generateDisplayName(email), academicDivision, false);
                                databaseRef.child(studentUID).setValue(newStudent).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        showLoadingIndicator(false);
                                        if (task.isSuccessful()) {
                                            Toast.makeText(SignUpActivity.this, "Successfully registered.", Toast.LENGTH_LONG).show();
                                            startActivity(new Intent(SignUpActivity.this, SignInActivity.class));
                                            mAuth.signOut();
                                            finish();
                                        }
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(SignUpActivity.this, "Error saving profile data.", Toast.LENGTH_LONG).show();
                                    }
                                });
                            }
                        } else {
                            showLoadingIndicator(false);
                            Log.w(TAG, "signUnWithEmail:failure", task.getException());
                            Toast.makeText(SignUpActivity.this, "User already exist.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    public static String generateDisplayName(String email) {
        int atIndex = email.indexOf('@');
        if (atIndex == -1) return "User";

        String username = email.substring(0, atIndex);
        String spacedName = username.replace('.', ' ');

        StringBuilder result = new StringBuilder();
        boolean capitalizeNext = true;

        for (char c : spacedName.toCharArray()) {
            if (Character.isWhitespace(c)) {
                result.append(c);
                capitalizeNext = true;
            } else if (capitalizeNext) {
                result.append(Character.toUpperCase(c));
                capitalizeNext = false;
            } else {
                result.append(Character.toLowerCase(c));
            }
        }
        return result.toString();
    }

    private void showLoadingIndicator(boolean show) {
        LinearLayout loadingOverlay = findViewById(R.id.loading_overlay);

        if (loadingOverlay != null) {
            loadingOverlay.setVisibility(show ? View.VISIBLE : View.GONE);
            loadingOverlay.setVisibility(show? View.VISIBLE: View.GONE);
            findViewById(R.id.sign_up_button).setEnabled(!show);
        }
    }
}