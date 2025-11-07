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
    private AutoCompleteTextView programDropdown;
    private DatabaseReference databaseRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile_setup);

        databaseRef = FirebaseDatabase.getInstance().getReference("students");

        int layoutID = R.layout.dropdown_menu_item;
        programDropdown = findViewById(R.id.program_dropdown_edit_text);
        Helper.programDropdown(programDropdown, this, layoutID);

        findViewById(R.id.submit_button).setOnClickListener(v -> submitButton());
    }

    public void submitButton (){
        String currentCourse = programDropdown.getText().toString();
        if(currentCourse.isEmpty()){
            programDropdown.setError("Required!");
            return;
        }
        StudentCache.setCurrentCourse(currentCourse);
        databaseRef.child(StudentCache.getCurrentKey()).child("program").setValue(currentCourse);
        startActivity(new Intent(this, HomePageActivity.class));
        finish();
    }

}

