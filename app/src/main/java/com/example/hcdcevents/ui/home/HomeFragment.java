package com.example.hcdcevents.ui.home;

import static android.view.View.GONE;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.provider.CalendarContract;
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

public class HomeFragment extends Fragment {
    private FragmentHomeBinding binding;
    private LinearLayout eventContainer;
    private DatabaseReference eventRef;
    private HomeViewModel homeViewModel;
    private final List<Events> allEventsList = new ArrayList<>();
    private TabLayout tabLayout;
    private SwipeRefreshLayout swipeRefresh;

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
                if (tab.getPosition() == 0) {
                    filterEvents("UPCOMING");
                } else if (tab.getPosition() == 1) {
                    filterEvents("ONGOING");
                } else {
                    filterEvents("COMPLETED");
                }
            }

            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });

        swipeRefresh = root.findViewById(R.id.swipeRefreshLayout);
        swipeRefresh.setOnRefreshListener(() -> {

            homeViewModel.loadEvents();

            int currentTab = tabLayout.getSelectedTabPosition();
            if (currentTab == 0) filterEvents("UPCOMING");
            else if (currentTab == 1) filterEvents("ONGOING");
            else filterEvents("COMPLETED");

            swipeRefresh.setRefreshing(false);
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
            if (currentTab == 0) {
                filterEvents("UPCOMING");
            } else if (currentTab == 1) {
                filterEvents("ONGOING");
            } else {
                filterEvents("COMPLETED");
            }
        });
    }

    private void filterEvents(String statusToShow) {
        eventContainer.removeAllViews();
        long currentTime = System.currentTimeMillis();
        boolean hasEvents = false;

        for (Events event : allEventsList) {
            String currentStatus = event.getStatus();
            boolean isMatch = false;
            if (currentStatus == null) currentStatus = "UPCOMING";
            long eventTime = event.getTimeStamp();

            if (statusToShow.equals("COMPLETED")) {

                if (currentStatus.equals("COMPLETED")) isMatch = true;

            } else if (statusToShow.equals("ONGOING")) {
                if (!currentStatus.equals("COMPLETED") && eventTime <= currentTime && eventTime > (currentTime - 86400000)) {
                    isMatch = true;
                }

            } else if (statusToShow.equals("UPCOMING")) {
                if (!currentStatus.equals("COMPLETED") && eventTime > currentTime) {
                    isMatch = true;
                }
            }

            if (isMatch) {
                eventCard(event);
                hasEvents = true;
            }
        }

        if (!hasEvents) showEmptyState(statusToShow);;
    }

    private void showEmptyState(String currentTabStatus) {
        if (getContext() != null) {
            LayoutInflater inflater = LayoutInflater.from(getContext());
            View emptyView = inflater.inflate(R.layout.layout_empty_event, eventContainer, false);

            TextView titleText = emptyView.findViewById(R.id.empty_state_title);
            TextView messageText = emptyView.findViewById(R.id.empty_state_description);

            if ("ONGOING".equals(currentTabStatus)) {
                titleText.setText("No Ongoing Events");
                messageText.setText("There are no events happening right now.");
            }
            else if ("COMPLETED".equals(currentTabStatus)) {
                titleText.setText("No History Found");
                messageText.setText("Past events will appear here once they are finished.");
            }
            else {

                titleText.setText("No Upcoming Events");
                messageText.setText("Check back soon for new events!");
            }

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
        ImageView btnAddToCalendar = eventCard.findViewById(R.id.btn_add_to_calendar);

        btnAddToCalendar.setOnClickListener(v -> {
            long eventTime = eventInfo.getTimeStamp();
            if(eventTime == 0) {
                eventTime = parseDateStringToMillis(eventInfo.getEventDateString());
            }
            addEventToCalendar(eventInfo.getEventTitle(), eventInfo.getEventDetails(), eventInfo.getEventLocation(), eventTime);
        });

        cardIcon.setImageDrawable(Helper.iconGenerator(eventInfo.getEventOrganizer()));
        cardTextOrganizer.setText(eventInfo.getEventOrganizer());
        cardTextTitle.setText(eventInfo.getEventTitle());
        cardTextDescription.setText(eventInfo.getEventDetails());
        cardTextLocation.setText(eventInfo.getEventLocation());
        cardTextDateTime.setText(eventInfo.getEventDateString());
        cardTextAuthor.setText("Created by: " + eventInfo.getEventAuthor());

        cardMoreButton.setVisibility(StudentCache.isCurrentIsAdmin() ? View.VISIBLE : GONE);

        String currentStatus = eventInfo.getStatus() == null ? "UPCOMING" : eventInfo.getStatus();
        cardMoreButton.setOnClickListener(v -> popUpMenu(context, v, eventInfo.getEventID(), eventInfo.getEventTitle(), currentStatus));

        if ("COMPLETED".equals(currentStatus)) {
            btnAddToCalendar.setVisibility(GONE);
            statusLabel.setVisibility(View.VISIBLE);
            cardTextTitle.setAlpha(0.5f);
            cardTextTitle.setPaintFlags(cardTextTitle.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        } else {
            statusLabel.setVisibility(GONE);
            cardTextTitle.setAlpha(1.0f);
            cardTextTitle.setPaintFlags(cardTextTitle.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
        }
        eventContainer.addView(eventCard);
    }


//  TODO: Pop up menu =============================
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

        if (!"COMPLETED".equals(eventStatus)) {
            popup.getMenu().add(Menu.NONE, 1, Menu.NONE, "Edit Event");
            popup.getMenu().add(Menu.NONE, 3, Menu.NONE, "Mark as Completed");
        }
        popup.getMenu().add(Menu.NONE, 2, Menu.NONE, "Delete Event");

        popup.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();

            if (id == 1) {
                editEventInfo(eventID);
                return true;
            } else if (id == 2) {
                confirmDeleteEvent(eventID, eventTitle);
                return true;
            } else if (id == 3) {
                updateEventStatus(eventID);
                return true;
            }
            return false;
        });
        popup.show();
    }

    private void updateEventStatus(String eventID) {
        eventRef.child(eventID).child("status").setValue("COMPLETED")
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Event marked as COMPLETED", Toast.LENGTH_SHORT).show();
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


// TODO: Add to calendar--------------------
    private void addEventToCalendar(String title, String description, String location, long startTimeInMillis) {
        Intent intent = new Intent(Intent.ACTION_INSERT);
        intent.setData(CalendarContract.Events.CONTENT_URI);

        intent.putExtra(CalendarContract.Events.TITLE, title);
        intent.putExtra(CalendarContract.Events.DESCRIPTION, description);
        intent.putExtra(CalendarContract.Events.EVENT_LOCATION, location);

        intent.putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, startTimeInMillis);
        intent.putExtra(CalendarContract.EXTRA_EVENT_END_TIME, startTimeInMillis + (2 * 60 * 60 * 1000));

        try {
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(getContext(), "No Calendar app found", Toast.LENGTH_SHORT).show();
        }
    }

    private long parseDateStringToMillis(String dateString) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("EEEE, MMMM d, yyyy 'at' h:mm a", Locale.US);
            Date date = sdf.parse(dateString);
            return date != null ? date.getTime() : System.currentTimeMillis();
        } catch (Exception e) {
            e.printStackTrace();
            return System.currentTimeMillis();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}