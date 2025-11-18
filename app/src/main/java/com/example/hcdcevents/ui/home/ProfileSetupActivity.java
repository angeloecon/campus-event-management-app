package com.example.hcdcevents.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.widget.AutoCompleteTextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.hcdcevents.R;
import com.example.hcdcevents.data.model.StudentCache;
import com.example.hcdcevents.utils.Helper;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class ProfileSetupActivity extends AppCompatActivity {
    private AutoCompleteTextView academicDivisionDropdown;
    private DatabaseReference databaseRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile_setup);

        databaseRef = FirebaseDatabase.getInstance().getReference("students");

        int layoutID = R.layout.dropdown_menu_item;
        academicDivisionDropdown = findViewById(R.id.program_dropdown_edit_text);
        Helper.academicDivisionDropdown(academicDivisionDropdown, this, layoutID);

        findViewById(R.id.submit_button).setOnClickListener(v -> submitButton());
    }

    public void submitButton (){
        String currentAcademicDivision = academicDivisionDropdown.getText().toString();
        if(currentAcademicDivision.isEmpty()){
            academicDivisionDropdown.setError("Required!");
            return;
        }
        StudentCache.setCurrentCourse(currentAcademicDivision);
        databaseRef.child(StudentCache.getCurrentKey()).child("academicDivision").setValue(currentAcademicDivision);
        startActivity(new Intent(this, HomePageActivity.class));
        finish();
    }

}

