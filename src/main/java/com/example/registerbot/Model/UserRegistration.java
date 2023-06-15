package com.example.registerbot.Model;

import org.bson.Document;

import java.time.LocalDateTime;
import java.util.Date;

public class UserRegistration extends Document {
    private static final String _ID = "_id";
    private static final String EMAIL = "email";
    private static final String CREATED_AT = "createdAt";
    private static final String ACTIVATED = ("activated");
    public static final String USER_REGISTRATION = "UserRegistration";


    public String getId() {
        return getString(_ID);
    }

    public void setId(String id) {
        put(_ID, id);
    }

    public String getEmail() {
        return getString(EMAIL);
    }

    public void setEmail(String email) {
        put(EMAIL, email);
    }

    public Date getCreatedAt() {
        return getDate(CREATED_AT);
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        put(CREATED_AT, createdAt);
    }

    public boolean getActivated() {
        return getBoolean(ACTIVATED);
    }

    public void setActivated(Boolean activated) {
        put(ACTIVATED, activated);
    }
}
