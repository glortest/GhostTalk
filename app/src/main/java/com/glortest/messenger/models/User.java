package com.glortest.messenger.models;

import java.io.Serializable;
import java.util.ArrayList;

public class User implements Serializable {
    public String name;
    public String image;
    public String phone;
    public String token;
    public String id;
    public String username;

    public ArrayList<String> admins;
}
