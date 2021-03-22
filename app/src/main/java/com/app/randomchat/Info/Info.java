package com.app.randomchat.Info;

public interface Info {

    String TAG = "mytag";

    int TYPE_MESSAGE = 5;
    int TYPE_USER = 6;
    int TYPE_REC_MESSAGE = 7;

    int TYPE_SHOW_RIGHT = 88;
    int TYPE_SHOW_LEFT = 89;

    int DEFAULT_MIN_WIDTH_QUALITY = 400;

    int REQUEST_CODE = 66;
    int DEFAULT_MSG_LENGTH_LIMIT = 1000;
    int RC_PHOTO_PICKER = 1001;
    int RC_SIGN_IN = 1000;
    int minWidthQuality = DEFAULT_MIN_WIDTH_QUALITY;

    String KEY_IMAGE = "KEY_IMAGE";
    String KEY_AGE = "KEY_AGE";

    String USER_CON_HISTORY = "UserConHistory";

    String CHATTY_MSG_LENGTH_KEY = "chatty_message_length";

    String MALE = "Male";

    String CONVERSATIONS = "Conversations";
    String CHATTY_PHOTOS = "chatty_photos";

    String FEMALE = "Female";
    String BOTH = "Both";

    String USERS = "Users";
    String ONLINE_USERS = "OnlineUsers";

    String KEY_TARGET_USER_ID = "TARGET_USER_ID";

    String defaultImageUrl = "https://st3.depositphotos.com/4111759/13425/v/380/depositphotos_134255626-stock-illustration-avatar-male-profile-gray-person.jpg";
    String TEMP_IMAGE_NAME = "tempImage";

}
