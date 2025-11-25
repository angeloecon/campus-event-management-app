package com.example.hcdcevents.feature.events;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.hcdcevents.R;
import com.example.hcdcevents.data.model.StudentCache;
import com.example.hcdcevents.ui.home.HomeFragment;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class CreateEvent extends AppCompatActivity {

    private TextInputEditText editTextEventTitle, editTextEventDescription, editTextOrganizer, editTextLocation;
    private TextView textViewSelectedDate, textViewSelectedTime;
    private DatabaseReference eventDbRef;
    private Calendar selectedDateTimeCalendar;
    private String currentEventID;
    private boolean isEditMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_create_event);

        eventDbRef = FirebaseDatabase.getInstance().getReference("events");


        editTextEventTitle = findViewById(R.id.editTextEventTitle);
        editTextEventDescription = findViewById(R.id.editTextEventDescription);
        editTextOrganizer = findViewById(R.id.editTextEventOrganizer);
        editTextLocation = findViewById(R.id.editTextEventLocation);
        textViewSelectedTime = findViewById(R.id.textViewSelectedTime);
        textViewSelectedDate = findViewById(R.id.textViewSelectedDate);
        selectedDateTimeCalendar = Calendar.getInstance();

        findViewById(R.id.buttonCancelPost).setOnClickListener(v -> cancelCreateEvent());

        findViewById(R.id.buttonPickDate).setOnClickListener(v -> showDatePickerDialog());
        findViewById(R.id.buttonPickTime).setOnClickListener(v -> showTimePickerDialog());
        Button createButton = findViewById(R.id.buttonCreatePost);

        if(getIntent().hasExtra("EVENT_MODE") && "EDIT".equals(getIntent().getStringExtra("EVENT_MODE"))){
            currentEventID = getIntent().getStringExtra("EVENT_KEY");
            isEditMode = true;
            createButton.setText("Update Event");
            createButton.setOnClickListener(v -> eventPostStatus());
            loadExistingData(currentEventID);
            return;
        }

        createButton.setOnClickListener(v -> eventPostStatus());
    }

    private void showDatePickerDialog() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    selectedDateTimeCalendar.set(Calendar.YEAR, year);
                    selectedDateTimeCalendar.set(Calendar.MONTH, month);
                    selectedDateTimeCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    updateDateTextView();
                },
                selectedDateTimeCalendar.get(Calendar.YEAR),
                selectedDateTimeCalendar.get(Calendar.MONTH),
                selectedDateTimeCalendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
        datePickerDialog.show();
    }
    private void showTimePickerDialog() {
        TimePickerDialog timePickerDialog = new TimePickerDialog(
                this,
                (view, hourOfDay, minute) -> {
                    selectedDateTimeCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    selectedDateTimeCalendar.set(Calendar.MINUTE, minute);
                    updateTimeTextView();
                },
                selectedDateTimeCalendar.get(Calendar.HOUR_OF_DAY),
                selectedDateTimeCalendar.get(Calendar.MINUTE),
                false
        );
        timePickerDialog.show();
    }

    private void updateDateTextView(){
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.US);
        textViewSelectedDate.setText(dateFormat.format(selectedDateTimeCalendar.getTime()));
    }

    private void updateTimeTextView(){
        SimpleDateFormat timeFormat = new SimpleDateFormat("h.mm a ", Locale.US);
        textViewSelectedTime.setText(timeFormat.format(selectedDateTimeCalendar.getTime()));
    }


    private void eventPostStatus(){
        CharSequence initialEventTitle = editTextEventTitle.getText();
        CharSequence initialEventDetails = editTextEventDescription.getText();
        CharSequence initialEventOrganizer = editTextOrganizer.getText();
        CharSequence initialEventLocation = editTextLocation.getText();

        if(initialEventTitle == null || initialEventTitle.length() == 0){
            editTextEventTitle.setError("This field should not be empty.");
            return;
        }

        if(initialEventDetails == null || initialEventDetails.length() == 0){
            editTextEventDescription.setError("This field should not be empty.");
            return;
        }

        if(initialEventOrganizer == null || initialEventOrganizer.length() == 0){
            editTextOrganizer.setError("This field should not be empty.");
            return;
        }

        if(initialEventLocation == null || initialEventLocation.length() == 0){
            editTextLocation.setError("Location is required.");
        }

        if(textViewSelectedDate.getText().toString().equals("No date selected")|| textViewSelectedTime.getText().toString().equals("No time selected")){
            Toast.makeText(CreateEvent.this, "Please fill in all the event details", Toast.LENGTH_SHORT).show();
            return;
        }

        long timeStamp = selectedDateTimeCalendar.getTimeInMillis();

        SimpleDateFormat fullTimeDateFormat = new SimpleDateFormat("EEEE, MMMM, d, yyyy 'at' h:mm a", Locale.US);
        String timeDateString = fullTimeDateFormat.format(selectedDateTimeCalendar.getTime());
        String finalEventTitle = initialEventTitle.toString().trim();
        String finalEventDetails = initialEventDetails.toString().trim();
        String finalEventOrganizer = initialEventOrganizer.toString().trim();
        String finalEventLocation = initialEventLocation.toString().trim();

        if(isEditMode){
            editExistingEvent(finalEventTitle, finalEventDetails, finalEventOrganizer, timeDateString, timeStamp, finalEventLocation);
            return;
        }

        String eventId = eventDbRef.push().getKey();

        if(eventId == null){
            Toast.makeText(CreateEvent.this, "Failed to generate event ID", Toast.LENGTH_SHORT).show();
            return;
        }
        createNewEventPost(eventId, finalEventTitle, finalEventDetails, finalEventOrganizer, timeDateString, timeStamp, finalEventLocation);
    }

    private void createNewEventPost(String eventId, String eventTitle, String eventDetails, String eventOrganizer, String eventSched, long eventStamp, String location){
        showLoadingIndicator(true);
        Events newEvent = new Events(eventOrganizer, eventTitle, eventDetails, eventSched, eventId, eventStamp, StudentCache.getCurrentName(), location);
        eventDbRef.child(eventId).setValue(newEvent)
                .addOnSuccessListener(aVoid -> {
                    showLoadingIndicator(false);
                    Toast.makeText(CreateEvent.this, "Event posted successfully!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    showLoadingIndicator(false);
                    Toast.makeText(CreateEvent.this, "Failed to post event.", Toast.LENGTH_SHORT).show();
                });
    }

    private void cancelCreateEvent(){
        finish();
    }

    private void loadExistingData(String eventID){
        eventDbRef.child(eventID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Events currentEvent = snapshot.getValue(Events.class);
                if(snapshot.exists()){
                    if(currentEvent != null){
                        editTextEventTitle.setText(currentEvent.getEventTitle());
                        editTextEventDescription.setText(currentEvent.getEventDetails());
                        editTextOrganizer.setText(currentEvent.getEventOrganizer());
                        editTextLocation.setText(currentEvent.getEventLocation());
                        textViewSelectedTime.setText(String.valueOf(currentEvent.getTimeStamp()));
                        textViewSelectedDate.setText(currentEvent.getEventDateString());
                        String fullDateTimeString = currentEvent.getEventDateString();
                        SimpleDateFormat fullTimeDateFormat = new SimpleDateFormat("EEEE, MMMM, d, yyyy 'at' h:mm a", Locale.US);
                        try{
                            Date eventDate = fullTimeDateFormat.parse(fullDateTimeString);

                            selectedDateTimeCalendar.setTime(eventDate);
                            updateDateTextView();
                            updateTimeTextView();
                        } catch (Exception e){
                            Log.e("EDIT_EVENT", "Date parsing failed: " + e.getMessage());
                            selectedDateTimeCalendar = Calendar.getInstance();
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("EDIT_EVENT", "Failed to load event data: " + error.getMessage());
                Toast.makeText(CreateEvent.this, "Error loading event.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void editExistingEvent(String eventTitle, String eventDetails, String eventOrganizer, String eventSched, long eventStamp, String location){
        showLoadingIndicator(true);
        Events updatedEventValue = new Events(eventOrganizer, eventTitle, eventDetails, eventSched, currentEventID, eventStamp, StudentCache.getCurrentName() + " (edited)", location);
        eventDbRef.child(currentEventID).setValue(updatedEventValue).addOnCompleteListener(task -> {
            if(task.isSuccessful()){
                Toast.makeText(CreateEvent.this, "Event edited successfully!", Toast.LENGTH_SHORT).show();
                showLoadingIndicator(false);
                finish();
            } else {
                showLoadingIndicator(false);
                Toast.makeText(this, "Failed to update event.", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void showLoadingIndicator(boolean show) {
        LinearLayout loadingOverlay = findViewById(R.id.loading_overlay);

        if (loadingOverlay != null) {
            loadingOverlay.setVisibility(show ? View.VISIBLE : View.GONE);
            loadingOverlay.setVisibility(show? View.VISIBLE: View.GONE);
            findViewById(R.id.buttonCreatePost).setEnabled(!show);
        }
    }
}