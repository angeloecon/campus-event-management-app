package com.example.hcdcevents.ui.home;

import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.hcdcevents.feature.events.Events;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class HomeViewModel extends ViewModel {

    private final MutableLiveData<List<Events>> eventsList = new MutableLiveData<>();
    private final DatabaseReference eventRef = FirebaseDatabase.getInstance().getReference("events");
    private ValueEventListener eventsListener;

    public LiveData<List<Events>> getEvents(){
        return eventsList;
    }

    public void loadEvents(){
        if(eventsListener != null) return;

        eventsListener = eventRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                
                List<Events> listOfEvent = new ArrayList<>();
                for(DataSnapshot eventSnapshot : snapshot.getChildren()){
                    Events event = eventSnapshot.getValue(Events.class);
                    if(event != null){
                        listOfEvent.add(event);
                    }
                }
//              Ascending Mode
                listOfEvent.sort(Comparator.comparingLong(Events::getTimeStamp));
//                listOfEvent.sort((o1, o2) -> Long.compare(o2.getTimeStamp(), o1.getTimeStamp()));
                eventsList.setValue(listOfEvent);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void deleteEvent(String eventID){
        eventRef.child(eventID).removeValue();
    }

    @Override
    public void onCleared(){
        super.onCleared();
        if(eventsListener != null){
            eventRef.removeEventListener(eventsListener);
        }
    }
}