package com.example.hcdcevents.ui.home;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.hcdcevents.R;
import com.example.hcdcevents.data.model.StudentCache;
import com.example.hcdcevents.databinding.FragmentHomeBinding;
import com.example.hcdcevents.feature.events.Events;

import com.example.hcdcevents.utils.Helper;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import de.hdodenhof.circleimageview.CircleImageView;


public class HomeFragment extends Fragment {
    private FragmentHomeBinding binding;
    private LinearLayout eventContainer;
    private DatabaseReference eventRef;
    private HomeViewModel homeViewModel;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
//        ----------------------
        eventRef = FirebaseDatabase.getInstance().getReference("events");
        eventContainer = root.findViewById(R.id.events_container);

        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState){
        super.onViewCreated(view, savedInstanceState);
        homeViewModel.loadEvents();

        homeViewModel.getEvents().observe(getViewLifecycleOwner(), events -> {
            eventContainer.removeAllViews();
            if (!isAdded()) {
                return;
            }
            if(events == null || events.isEmpty()){
                Context context = getContext();
                if(context != null){
                    LayoutInflater inflater = LayoutInflater.from(context);
                    View emptyView = inflater.inflate(R.layout.layout_empty_event, eventContainer , false);
                    eventContainer.addView(emptyView);
                }
            } else {
                for(Events event : events){
                    eventCard(event);
                }
            }
        });

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @SuppressLint("SetTextI18n")
    public void eventCard(Events eventInfo){

        Context context = getContext();
        if(context == null || eventContainer == null) {
            Log.w( "HomeFragment", "Context is null, cannot display card.");
            Toast.makeText(getContext(), "CONTEXT IS NULL!", Toast.LENGTH_SHORT).show();
            return;
        }

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View eventCard = inflater.inflate(R.layout.template_event_card, eventContainer, false);

        ImageView cardIcon = eventCard.findViewById(R.id.card_icon);
        TextView cardTextOrganizer = eventCard.findViewById(R.id.card_text_organizer);
        TextView cardTextTitle = eventCard.findViewById(R.id.card_text_title);
        TextView cardTextDescription = eventCard.findViewById(R.id.card_text_description);
        TextView cardTextDateTime = eventCard.findViewById(R.id.card_text_date_time);
        TextView cardTextAuthor = eventCard.findViewById(R.id.card_text_poster);
        TextView cardTextLocation = eventCard.findViewById(R.id.card_text_location);

        ImageView cardDeleteButton = eventCard.findViewById(R.id.card_delete_button);

        cardIcon.setImageDrawable(Helper.iconGenerator(eventInfo.getEventOrganizer()));
        cardTextOrganizer.setText(eventInfo.getEventOrganizer());
        cardTextTitle.setText(eventInfo.getEventTitle());
        cardTextDescription.setText(eventInfo.getEventDetails());
        cardTextLocation.setText(eventInfo.getEventLocation());
        cardTextDateTime.setText(eventInfo.getEventDateString());
        cardTextAuthor.setText("Created by: " + eventInfo.getEventAuthor());
        cardDeleteButton.setVisibility(StudentCache.isCurrentIsAdmin()? View.VISIBLE : View.GONE);
        cardDeleteButton.setOnClickListener(v -> confirmDeleteEvent(eventInfo.getEventID(),
                eventInfo.getEventTitle()));
        eventContainer.addView(eventCard);
    }

    private void deleteEvent(String eventID, String eventTitle){
        if(getContext() == null) return;
        homeViewModel.deleteEvent(eventID);
        Toast.makeText(getContext(), "\"" + eventTitle + "\" deleted successfully." , Toast.LENGTH_LONG).show();
    }

    private void confirmDeleteEvent(String eventID, String eventTitle){
        if(eventID == null || eventRef == null) return;

        new AlertDialog.Builder(getContext())
                .setTitle("Delete Event Post")
                .setMessage("Are you sure you want to permanently delete the event: \"" + eventTitle + "\". This action cannot be undone.")
                .setPositiveButton("Delete", (dialog, which) -> {
                    deleteEvent(eventID, eventTitle);
                })
                .setNegativeButton("Cancel", (dialog, which) -> {
                    dialog.dismiss();
                })
                .setIcon(R.drawable.ic_red_warning)
                .show();
    }




}