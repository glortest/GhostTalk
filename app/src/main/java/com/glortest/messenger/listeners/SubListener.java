package com.glortest.messenger.listeners;

import com.glortest.messenger.models.User;

public interface SubListener {
    void onConversationClicked(User user, String type);
}
