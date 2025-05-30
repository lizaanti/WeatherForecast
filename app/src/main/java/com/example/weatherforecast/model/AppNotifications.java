package com.example.weatherforecast.model;

public class AppNotifications {
    private final int id;
    private final String title;
    private final String message;
    private final String channelId;
    private final String channelName;

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getMessage() {
        return message;
    }

    public String getChannelId() {
        return channelId;
    }

    public String getChannelName() {
        return channelName;
    }

    public AppNotifications(int id, String title, String message, String channelId, String channelName) {
        this.id = id;
        this.title = title;
        this.message = message;
        this.channelId = channelId;
        this.channelName = channelName;
    }

}
