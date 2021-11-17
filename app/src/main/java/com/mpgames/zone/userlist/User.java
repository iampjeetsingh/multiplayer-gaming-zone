package com.mpgames.zone.userlist;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class User {
    private String name,photoUrl,userID,userName,currentRoomName;
    private boolean online,inRoom;
    private long lastSeen;
    private ArrayList<String> friends;
    public User() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getCurrentRoomName() {
        return currentRoomName;
    }

    public void setCurrentRoomName(String currentRoomName) {
        this.currentRoomName = currentRoomName;
    }

    public boolean isOnline() {
        return online;
    }

    public void setOnline(boolean online) {
        this.online = online;
    }

    public boolean isInRoom() {
        return inRoom;
    }

    public void setInRoom(boolean inRoom) {
        this.inRoom = inRoom;
    }

    public void setLastSeen(long lastSeen) {
        this.lastSeen = lastSeen;
    }

    public String getLastSeen(){
        DateFormat year = new SimpleDateFormat("yyyy");
        DateFormat month = new SimpleDateFormat("MM");
        DateFormat day = new SimpleDateFormat("dd");
        DateFormat hour = new SimpleDateFormat("hh");
        DateFormat minutes = new SimpleDateFormat("mm");
        Date now = Calendar.getInstance().getTime();
        int yearDiff = Integer.parseInt(year.format(now))-Integer.parseInt(year.format(lastSeen));
        int monthDiff = Integer.parseInt(month.format(now))-Integer.parseInt(month.format(lastSeen));
        int dayDiff = Integer.parseInt(day.format(now))-Integer.parseInt(day.format(lastSeen));
        int hourDiff = Integer.parseInt(hour.format(now))-Integer.parseInt(hour.format(lastSeen));
        int minutesDiff = Integer.parseInt(minutes.format(now))-Integer.parseInt(minutes.format(lastSeen));
        if(yearDiff>0){
            if(yearDiff==1)
                return "1 year ago";
            else
                return yearDiff+" years ago";
        }else if(monthDiff>0){
            if(monthDiff==1)
                return "1 month ago";
            else
                return monthDiff+" months ago";
        }else if(dayDiff>0){
            if(dayDiff==1)
                return "1 day ago";
            else
                return dayDiff+" days ago";
        }else if(hourDiff>0){
            if(hourDiff==1)
                return "1 hour ago";
            else
                return hourDiff+" hours ago";
        }else if(minutesDiff>0){
            if(minutesDiff==1)
                return "1 minute ago";
            else
                return minutesDiff+" minutes ago";
        }else
            return "0 minute ago";
    }

    public ArrayList<String> getFriends() {
        return friends;
    }

    public void setFriends(ArrayList<String> friends) {
        this.friends = friends;
    }
}
