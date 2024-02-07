package com.glortest.messenger.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;

public class ChannelModel implements Serializable {
    public String name;
    public String avatar;
    public String username;
    public String messageType;
    public String image;
    public String message;
    public String dateTime;
    public Date dateObject;
    public String conversionId;
    public String conversionName;
    public String conversionImage;

    public ArrayList<String> admins;
    public ArrayList<String> members;

    public String bio;


}
