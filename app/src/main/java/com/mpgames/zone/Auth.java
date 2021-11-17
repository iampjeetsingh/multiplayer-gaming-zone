package com.mpgames.zone;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.mpgames.zone.room.Player;

public class Auth {
    public static FirebaseUser getUser(){
        return FirebaseAuth.getInstance().getCurrentUser();
    }

    public static String getDisplayName(){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        return user.getDisplayName();
    }
    public static String getEmail(){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        return user.getEmail();
    }
    public static String getUid(){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        return user.getUid();
    }
    public static String getPhotoUrl(){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        return user.getPhotoUrl().toString();
    }

    public static String getUserName(){
        String email = getEmail();
        if(email.contains("@gmail.com")){
            return email.substring(0,email.indexOf('@'));
        }else{
            return email.substring(0,email.indexOf('@'))+"_"
                    +email.substring(email.indexOf("@"),email.lastIndexOf("."));
        }
    }

    public static Player getPlayer(){
        Player player = new Player();
        player.setName(getDisplayName());
        player.setPhotoUrl(getPhotoUrl());
        player.setUserID(getUid());
        player.setUserName(getUserName());
        return player;
    }

    public static boolean userIsNull(){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if(user==null){
            return true;
        }
        return false;
    }

    public static boolean userIsNotNull(){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if(user==null){
            return false;
        }
        return true;
    }

    public static boolean isCurrentPlayer(Player player){
        if(getUserName().equals(player.getUserName()))
            return true;
        return false;
    }


}
