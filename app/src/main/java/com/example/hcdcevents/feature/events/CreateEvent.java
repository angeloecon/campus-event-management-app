package com.example.hcdcevents.feature.events;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.hcdcevents.R;
import com.example.hcdcevents.data.model.StudentCache;
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

    // Defined all inputs as TextInputEditText (matching your new XML)
    private TextInputEditText inputTitle, inputDescription, inputOrganizer, inputLocation, inputDate, inputTime;

    private DatabaseReference eventDbRef;
    private Calendar selectedDateTimeCalendar;
    private String currentEventID;
    private boolean isEditMode = false;


    private final SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, MMM d, yyyy", Locale.US);
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm a", Locale.US);


    private final SimpleDateFormat dbFormat = new SimpleDateFormat("EEEE, MMMM d, yyyy 'at' h:mm a", Locale.US);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_create_event);

        eventDbRef = FirebaseDatabase.getInstance().getReference("events");

        // 1. Bind Views
        inputTitle = findViewById(R.id.editTextEventTitle);
        inputDescription = findViewById(R.id.editTextEventDescription);
        inputOrganizer = findViewById(R.id.editTextEventOrganizer);
        inputLocation = findViewById(R.id.editTextEventLocation);
        inputDate = findViewById(R.id.editTextEventDate); // New ID from XML
        inputTime = findViewById(R.id.editTextEventTime); // New ID from XML
        Button createButton = findViewById(R.id.buttonCreatePost);
        Button cancelButton = findViewById(R.id.buttonCancelPost);

        selectedDateTimeCalendar = Calendar.getInstance();


        inputDate.setOnClickListener(v -> showDatePickerDialog());
        inputTime.setOnClickListener(v -> showTimePickerDialog());

        cancelButton.setOnClickListener(v -> finish());

        // 3. Handle Edit Mode
        if (getIntent().hasExtra("EVENT_MODE") && "EDIT".equals(getIntent().getStringExtra("EVENT_MODE"))) {
            currentEventID = getIntent().getStringExtra("EVENT_KEY");
            isEditMode = true;
            createButton.setText("Update Event");
            loadExistingData(currentEventID);
        }

        createButton.setOnClickListener(v -> validateAndPostEvent());
    }

    private void showDatePickerDialog() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    selectedDateTimeCalendar.set(Calendar.YEAR, year);
                    selectedDateTimeCalendar.set(Calendar.MONTH, month);
                    selectedDateTimeCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    // Update the Text Input
                    inputDate.setText(dateFormat.format(selectedDateTimeCalendar.getTime()));
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
                    // Update the Text Input
                    inputTime.setText(timeFormat.format(selectedDateTimeCalendar.getTime()));
                },
                selectedDateTimeCalendar.get(Calendar.HOUR_OF_DAY),
                selectedDateTimeCalendar.get(Calendar.MINUTE),
                false
        );
        timePickerDialog.show();
    }

    private void validateAndPostEvent() {
        String title = inputTitle.getText() != null ? inputTitle.getText().toString().trim() : "";
        String details = inputDescription.getText() != null ? inputDescription.getText().toString().trim() : "";
        String organizer = inputOrganizer.getText() != null ? inputOrganizer.getText().toString().trim() : "";
        String location = inputLocation.getText() != null ? inputLocation.getText().toString().trim() : "";
        String dateVal = inputDate.getText() != null ? inputDate.getText().toString() : "";
        String timeVal = inputTime.getText() != null ? inputTime.getText().toString() : "";

        // Validations
        if (TextUtils.isEmpty(title)) {
            inputTitle.setError("Title is required");
            return;
        }
        if (TextUtils.isEmpty(details)) {
            inputDescription.setError("Description is required");
            return;
        }
        if (TextUtils.isEmpty(organizer)) {
            inputOrganizer.setError("Organizer is required");
            return;
        }
        if (TextUtils.isEmpty(location)) {
            inputLocation.setError("Location is required");
            return;
        }
        if (TextUtils.isEmpty(dateVal)) {
            inputDate.setError("Date is required");
            Toast.makeText(this, "Please select a date", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(timeVal)) {
            inputTime.setError("Time is required");
            Toast.makeText(this, "Please select a time", Toast.LENGTH_SHORT).show();
            return;
        }

        // Prepare Data for Database
        long timeStamp = selectedDateTimeCalendar.getTimeInMillis();
        // We reconstruct the full string for the DB to maintain compatibility
        String fullSchedString = dbFormat.format(selectedDateTimeCalendar.getTime());

        if (isEditMode) {
            updateEventInDb(title, details, organizer, fullSchedString, timeStamp, location);
        } else {
            String newEventId = eventDbRef.push().getKey();
            if (newEventId != null) {
                saveNewEventToDb(newEventId, title, details, organizer, fullSchedString, timeStamp, location);
            } else {
                Toast.makeText(this, "Error generating ID", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void saveNewEventToDb(String eventId, String title, String details, String organizer, String sched, long timeStamp, String location) {
        showLoadingIndicator(true);
        Events newEvent = new Events(organizer, title, details, sched, eventId, timeStamp, StudentCache.getCurrentName(), location);

        eventDbRef.child(eventId).setValue(newEvent)
                .addOnSuccessListener(aVoid -> {
                    showLoadingIndicator(false);
                    Toast.makeText(CreateEvent.this, "Event posted successfully!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    showLoadingIndicator(false);
                    Toast.makeText(CreateEvent.this, "Failed to post event: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void updateEventInDb(String title, String details, String organizer, String sched, long timeStamp, String location) {
        showLoadingIndicator(true);
        Events updatedEvent = new Events(organizer, title, details, sched, currentEventID, timeStamp, StudentCache.getCurrentName() + " (edited)", location);

        eventDbRef.child(currentEventID).setValue(updatedEvent)
                .addOnSuccessListener(aVoid -> {
                    showLoadingIndicator(false);
                    Toast.makeText(CreateEvent.this, "Event updated successfully!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    showLoadingIndicator(false);
                    Toast.makeText(CreateEvent.this, "Failed to update event.", Toast.LENGTH_SHORT).show();
                });
    }

    private void loadExistingData(String eventID) {
        showLoadingIndicator(true);
        eventDbRef.child(eventID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                showLoadingIndicator(false);
                Events event = snapshot.getValue(Events.class);
                if (event != null) {
                    inputTitle.setText(event.getEventTitle());
                    inputDescription.setText(event.getEventDetails());
                    inputOrganizer.setText(event.getEventOrganizer());
                    inputLocation.setText(event.getEventLocation());

                    if (event.getTimeStamp() > 0) {
                        selectedDateTimeCalendar.setTimeInMillis(event.getTimeStamp());
                    } else {
                        try {
                            Date date = dbFormat.parse(event.getEventDateString());
                            if (date != null) selectedDateTimeCalendar.setTime(date);
                        } catch (Exception e) {
                            Log.e("CreateEvent", "Error parsing date", e);
                        }
                    }
                    inputDate.setText(dateFormat.format(selectedDateTimeCalendar.getTime()));
                    inputTime.setText(timeFormat.format(selectedDateTimeCalendar.getTime()));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                showLoadingIndicator(false);
                Toast.makeText(CreateEvent.this, "Error loading data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showLoadingIndicator(boolean show) {
        LinearLayout loadingOverlay = findViewById(R.id.loading_overlay);
        Button createBtn = findViewById(R.id.buttonCreatePost);

        if (loadingOverlay != null) {
            loadingOverlay.setVisibility(show ? View.VISIBLE : View.GONE);
        }
        if (createBtn != null) {
            createBtn.setEnabled(!show);
        }
    }
}