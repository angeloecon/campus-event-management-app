package com.example.hcdcevents.feature.events;

public class Events {
    private String eventOrganizer,
            eventTitle,
            eventDetails,
            eventDateString,
            eventID,
            eventAuthor,
            eventLocation;
    private long timeStamp;

    public Events() {
    }

    public Events(String eventOrganizer, String eventTitle, String eventDetails, String eventDateString, String eventID, long timeStamp, String eventAuthor, String eventLocation) {
        this.eventOrganizer = eventOrganizer;
        this.eventTitle = eventTitle;
        this.eventDetails = eventDetails;
        this.eventDateString = eventDateString;
        this.eventID = eventID;
        this.timeStamp = timeStamp;
        this.eventAuthor = eventAuthor;
        this.eventLocation = eventLocation;
    }

    public String getEventOrganizer() {
        return eventOrganizer;
    }
    public String getEventTitle() {
        return eventTitle;
    }
    public String getEventDetails() {
        return eventDetails;
    }
    public String getEventDateString() {
        return eventDateString;
    }
    public String getEventID() { return eventID; }
    public String getEventAuthor() { return eventAuthor; }
    public String getEventLocation() { return eventLocation; }
    public long getTimeStamp() { return timeStamp; }

    public void setEventOrganizer(String eventOrganizer) {
        this.eventOrganizer = eventOrganizer;
    }
    public void setEventTitle(String eventTitle) {
        this.eventTitle = eventTitle;
    }
    public void setEventDetails(String eventDetails) {
        this.eventDetails = eventDetails;
    }
    public void setEventDateString(String eventDateString) { this.eventDateString = eventDateString; }
    public void setEventID(String eventID) {
        this.eventID = eventID;
    }
    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }
    public void setEventLocation(String eventLocation) { this.eventLocation = eventLocation; }
    public void setEventAuthor(String eventAuthor) { this.eventAuthor = eventAuthor; }
}
