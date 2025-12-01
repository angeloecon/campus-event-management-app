package com.example.hcdcevents.ui.home;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.hcdcevents.R;
import com.example.hcdcevents.data.model.StudentCache;
import com.example.hcdcevents.databinding.FragmentHomeBinding;
import com.example.hcdcevents.feature.events.CreateEvent;
import com.example.hcdcevents.feature.events.Events;
import com.example.hcdcevents.utils.Helper;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {
    private FragmentHomeBinding binding;
    private LinearLayout eventContainer;
    private DatabaseReference eventRef;
    private HomeViewModel homeViewModel;
    private List<Events> allEventsList = new ArrayList<>();
    private TabLayout tabLayout;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        eventRef = FirebaseDatabase.getInstance().getReference("events");

        eventContainer = root.findViewById(R.id.events_container);
        tabLayout = root.findViewById(R.id.tabLayoutEvents);

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                // When tab is clicked, filter the EXISTING list
                if (tab.getPosition() == 0) {
                    filterEvents("UPCOMING");
                } else {
                    filterEvents("COMPLETED");
                }
            }

            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });

        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupViewModelObserver();
    }

    private void setupViewModelObserver() {
        homeViewModel.loadEvents();

        homeViewModel.getEvents().observe(getViewLifecycleOwner(), events -> {
            allEventsList.clear();

            if (events != null) {
                allEventsList.addAll(events);
            }

            int currentTab = tabLayout.getSelectedTabPosition();
            if (currentTab == 1) {
                filterEvents("COMPLETED");
            } else {
                filterEvents("UPCOMING");
            }
        });
    }

    private void filterEvents(String statusToShow) {
        eventContainer.removeAllViews();

        boolean hasEvents = false;

        for (Events event : allEventsList) {
            String currentStatus = event.getStatus();
            if (currentStatus == null) currentStatus = "UPCOMING";

            if (statusToShow.equals("UPCOMING")) {
                // Show if UPCOMING or null
                if (!"COMPLETED".equals(currentStatus)) {
                    eventCard(event);
                    hasEvents = true;
                }
            } else {
                if ("COMPLETED".equals(currentStatus)) {
                    eventCard(event);
                    hasEvents = true;
                }
            }
        }

        if (!hasEvents) {
            showEmptyState();
        }
    }

    private void showEmptyState() {
        if (getContext() != null) {
            LayoutInflater inflater = LayoutInflater.from(getContext());
            View emptyView = inflater.inflate(R.layout.layout_empty_event, eventContainer, false);
            eventContainer.addView(emptyView);
        }
    }

    @SuppressLint("SetTextI18n")
    public void eventCard(Events eventInfo) {
        Context context = getContext();
        if (context == null || eventContainer == null) return;

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View eventCard = inflater.inflate(R.layout.template_event_card, eventContainer, false);

        ImageView cardIcon = eventCard.findViewById(R.id.card_icon);
        TextView cardTextOrganizer = eventCard.findViewById(R.id.card_text_organizer);
        TextView cardTextTitle = eventCard.findViewById(R.id.card_text_title);
        TextView cardTextDescription = eventCard.findViewById(R.id.card_text_description);
        TextView cardTextDateTime = eventCard.findViewById(R.id.card_text_date_time);
        TextView cardTextAuthor = eventCard.findViewById(R.id.card_text_poster);
        TextView cardTextLocation = eventCard.findViewById(R.id.card_text_location);
        TextView statusLabel = eventCard.findViewById(R.id.text_status_label);
        ImageView cardMoreButton = eventCard.findViewById(R.id.card_more_button);

        cardIcon.setImageDrawable(Helper.iconGenerator(eventInfo.getEventOrganizer()));
        cardTextOrganizer.setText(eventInfo.getEventOrganizer());
        cardTextTitle.setText(eventInfo.getEventTitle());
        cardTextDescription.setText(eventInfo.getEventDetails());
        cardTextLocation.setText(eventInfo.getEventLocation());
        cardTextDateTime.setText(eventInfo.getEventDateString());
        cardTextAuthor.setText("Created by: " + eventInfo.getEventAuthor());

        cardMoreButton.setVisibility(StudentCache.isCurrentIsAdmin() ? View.VISIBLE : View.GONE);

        String currentStatus = eventInfo.getStatus() == null ? "UPCOMING" : eventInfo.getStatus();
        cardMoreButton.setOnClickListener(v -> popUpMenu(context, v, eventInfo.getEventID(), eventInfo.getEventTitle(), currentStatus));

        if ("COMPLETED".equals(currentStatus)) {
            statusLabel.setVisibility(View.VISIBLE);
            cardTextTitle.setAlpha(0.5f);
            cardTextTitle.setPaintFlags(cardTextTitle.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        } else {
            statusLabel.setVisibility(View.GONE);
            cardTextTitle.setAlpha(1.0f);
            cardTextTitle.setPaintFlags(cardTextTitle.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
        }
        eventContainer.addView(eventCard);
    }

    private void deleteEvent(String eventID, String eventTitle) {
        if (getContext() == null) return;
        homeViewModel.deleteEvent(eventID);
        Toast.makeText(getContext(), "\"" + eventTitle + "\" deleted successfully.", Toast.LENGTH_LONG).show();
    }

    private void confirmDeleteEvent(String eventID, String eventTitle) {
        if (eventID == null) return;
        new AlertDialog.Builder(getContext())
                .setTitle("Delete Event Post")
                .setMessage("Are you sure you want to permanently delete: \"" + eventTitle + "\"?")
                .setPositiveButton("Delete", (dialog, which) -> deleteEvent(eventID, eventTitle))
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .setIcon(R.drawable.ic_red_warning)
                .show();
    }

    private void popUpMenu(Context context, View v, String eventID, String eventTitle, String eventStatus) {
        PopupMenu popup = new PopupMenu(context, v);
        popup.getMenu().add(Menu.NONE, 1, Menu.NONE, "Edit Event");
        popup.getMenu().add(Menu.NONE, 2, Menu.NONE, "Delete Event");

        if (!"COMPLETED".equals(eventStatus)) {
            popup.getMenu().add(Menu.NONE, 3, Menu.NONE, "Mark as Completed");
        }

        popup.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();
            if (id == 1) {
                editEventInfo(eventID);
                return true;
            } else if (id == 2) {
                confirmDeleteEvent(eventID, eventTitle);
                return true;
            } else if (id == 3) {
                updateEventStatus(eventID, "COMPLETED");
                return true;
            }
            return false;
        });
        popup.show();
    }

    private void updateEventStatus(String eventID, String newStatus) {
        eventRef.child(eventID).child("status").setValue(newStatus)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Event marked as " + newStatus, Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Failed to update status", Toast.LENGTH_SHORT).show();
                });
    }

    private void editEventInfo(String eventID) {
        Intent intent = new Intent(getContext(), CreateEvent.class);
        intent.putExtra("EVENT_KEY", eventID);
        intent.putExtra("EVENT_MODE", "EDIT");
        startActivity(intent);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}