package com.glortest.messenger.utilities;

import java.util.HashMap;

public class Constants {
    public static final String STATUS = "status";
    public static final String SUBSCRIBERS = "subscribers";
    public static final String CHANNELS_MSG = "channels_msg";
    public static final String AVATAR = "avatar";
    public static final String SUBSCRIBERS_COUNT = "subscribers_count";
    public static final String NAME = "name";
    public static final String ID = "id";
    public static final String TAG = "tag";
    public static final String ADMINS = "admins";
    public static final String TIMESTAMP = "timestamp";
    public static final String MESSAGES = "messages";
    public static final String LAST_MESSAGE = "last_message";
    public static final String PHONE = "phone";
    public static final String AVAILABLE = "available";
    public static final String ARCHIVE_PASSWORD = "archive_password";
    public static final String REMOTE_AUTHORIZATION = "Authorization";
    public static final String REMOTE_CONTENT_TYPE = "Content-Type";
    public static HashMap<String, String> remoteHeaders = null;
    public static final String REMOTE_DATA = "data";
    public static final String REMOTE_REGISTRATION_IDS = "registration_ids";
    public static final String CHANNEL_CONVERSATIONS = "channel_conversations";
    public static HashMap<String, String> getRemoteHeaders(){
        if (remoteHeaders == null){
            remoteHeaders = new HashMap<>();
            remoteHeaders.put(
                    REMOTE_AUTHORIZATION,
                    "key=AAAAd7JlE3w:APA91bHJJC-EsWlTuKQrANpNbdQCkCYZkCKASScdqsOMfyliHvGwgXusWCCGSQ2rSxAnCH6hBnQFrzm1_HvZTfnIcwIAUve1XX6jSeTaoAGlAtliZYYXzYteAEeonB0N_RS5b2JPKO8t"
            );
            remoteHeaders.put(
                    REMOTE_CONTENT_TYPE,
                    "application/json"
            );
        }
        return remoteHeaders;
    }
    public static final String USERNAME_SIGN = "@";
    public static final String ARGUMENT_PASSWORD = "argumentPassword";
    public static final String ARGUMENT_IMAGE_PROFILE = "argumentImageProfile";
    public static final String ARGUMENT_NAME = "argumentName";
    public static final String RECOVERY_CODE = "recoveryCode";
    public static final String IN_ARCHIVE = " ";
    public static final String PASSWORD = "password";
    public static final String PREFERENCE_NAME = "appPreference";
    public static final String USERS = "users";
    public static final String USERNAME = "userName";
    public static final String MESSAGE_TYPE = "messageType";
    public static final String MESSAGE_TYPE_TEXT = "text";
    public static final String MESSAGE_TYPE_IMAGE = "image";
    public static final String STORAGE_PACKAGE = "imageProfiles/";
    public static final String STORAGE_ATTACH = "attachments/";
    public static final String IMAGE_PROFILE = "imageProfile";
    public static final String IS_SIGNED_IN = "isSignedIn";
    public static final String BIO = "bio";
    public static final String USER_ID = "userId";
    public static final Integer DELAY_MILLS = 1000;
    public static final String DEFAULT_IMAGE_PROFILE = "https://firebasestorage.googleapis.com/v0/b/ghosttalk-f045e.appspot.com/o/Group%206.jpg?alt=media&token=471e0303-297b-4000-be8a-33678901dd6f";
    public static final String DEFAULT_BIO = "Hi, I am using GhostTalk";
    public static final String FCM_TOKEN = "fcmToken";
    public static final String USER = "user";
    public static final Integer VIEW_TYPE_SENT = 1;
    public static final Integer VIEW_TYPE_RECEIVED = 2;
    public static final Integer VIEW_TYPE_IMAGE_SENT = 3;
    public static final Integer VIEW_TYPE_IMAGE_RECEIVED = 4;

    public static final String COLLECTION_CHAT = "chat";
    public static final String COLLECTION_CHANNEL = "channel";
    public static final String SENDER_ID = "senderId";
    public static final String RECEIVER_ID = "receiverId";
    public static final String MESSAGE = "message";
    public static final String CONVERSATIONS = "conversations";
    public static final String SENDER_NAME = "senderName";
    public static final String RECEIVER_NAME = "receiverName";
    public static final String SENDER_IMAGE = "senderImage";
    public static final String RECEIVER_IMAGE = "receiverImage";


    public static final String COLLECTION_BLOCKED = "blocked";
    public static final String BLOCKER_ID = "blockerId";
    public static final String BLOCKED_ID = "blockedId";

    /*Length*/
    public static final Integer RECOVERY_CODE_MAX_LENGTH = 8;
    public static final Integer PASSWORD_MAX_LENGTH = 35;
    public static final Integer NAME_MAX_LENGTH = 25;
    public static final Integer USERNAME_MAX_LENGTH = 25;
    public static final Integer BIO_MAX_LENGTH = 30;
    public static final Integer MINIMUM_PASSWORD_LENGTH = 6;
}
