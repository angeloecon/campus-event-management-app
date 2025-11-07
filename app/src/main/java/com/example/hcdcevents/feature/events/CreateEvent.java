package com.example.hcdcevents.feature.events;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;

import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.hcdcevents.R;
import com.example.hcdcevents.data.model.StudentCache;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class CreateEvent extends AppCompatActivity {

    private TextInputEditText editTextEventTitle, editTextEventDescription, editTextOrganizer, editTextLocation;
    private TextView textViewSelectedDate, textViewSelectedTime;
    private DatabaseReference eventDbRef;
    private Calendar selectedDateTimeCalendar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_create_event);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        eventDbRef = FirebaseDatabase.getInstance().getReference("events");

        editTextEventTitle = findViewById(R.id.editTextEventTitle);
        editTextEventDescription = findViewById(R.id.editTextEventDescription);
        editTextOrganizer = findViewById(R.id.editTextEventOrganizer);
        editTextLocation = findViewById(R.id.editTextEventLocation);
        textViewSelectedTime = findViewById(R.id.textViewSelectedTime);
        textViewSelectedDate = findViewById(R.id.textViewSelectedDate);
        selectedDateTimeCalendar = Calendar.getInstance();

        findViewById(R.id.buttonCancelPost).setOnClickListener(v -> cancelCreateEvent());
        findViewById(R.id.buttonCreatePost).setOnClickListener(v -> newEventPostStatus());
        findViewById(R.id.buttonPickDate).setOnClickListener(v -> showDatePickerDialog());
        findViewById(R.id.buttonPickTime).setOnClickListener(v -> showTimePickerDialog());
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


    private void newEventPostStatus(){
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
        String eventId = eventDbRef.push().getKey();

        if(eventId == null){
            Toast.makeText(CreateEvent.this, "Failed to generate event ID", Toast.LENGTH_SHORT).show();
            return;
        }
        SimpleDateFormat fullTimeDateFormat = new SimpleDateFormat("EEEE, MMMM, d, yyyy 'at' h:mm a", Locale.US);
        String timeDateString = fullTimeDateFormat.format(selectedDateTimeCalendar.getTime());
        String finalEventTitle = initialEventTitle.toString().trim();
        String finalEventDetails = initialEventDetails.toString().trim();
        String finalEventOrganizer = initialEventOrganizer.toString().trim();
        String finalEventLocation = initialEventLocation.toString().trim();

        createNewEventPost(eventId, finalEventTitle, finalEventDetails, finalEventOrganizer, timeDateString, timeStamp, finalEventLocation);
    }

    private void createNewEventPost(String eventId, String eventTitle, String eventDetails, String eventOrganizer, String eventSched, long eventStamp, String location){

        Events newEvent = new Events(eventOrganizer, eventTitle, eventDetails, eventSched, eventId, eventStamp, StudentCache.getCurrentName(), location);
        eventDbRef.child(eventId).setValue(newEvent)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(CreateEvent.this, "Event posted successfully!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> Toast.makeText(CreateEvent.this, "Failed to post event.", Toast.LENGTH_SHORT).show());
    }

    private void cancelCreateEvent(){
        finish();
    }
}