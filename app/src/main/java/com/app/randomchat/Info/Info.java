package com.app.randomchat.Info;

public interface Info {

    String TAG = "mytag";

    int TYPE_MESSAGE = 5;
    int TYPE_USER = 6;
    int TYPE_REC_MESSAGE = 7;

    int TYPE_SHOW_RIGHT = 88;
    int TYPE_SHOW_LEFT = 89;


    int REQUEST_CODE = 66;
    int DEFAULT_MSG_LENGTH_LIMIT = 1000;
    int RC_PHOTO_PICKER = 1001;
    int RC_SIGN_IN = 1000;


    String USER_CON_HISTORY = "UserConHistory";

    String ANONYMOUS = "anonymous";
    String CHATTY_MSG_LENGTH_KEY = "chatty_message_length";

    String MALE = "Male";

    String CONVERSATIONS = "Conversations";
    String CHATTY_PHOTOS = "chatty_photos";

    String FEMALE = "Female";
    String BOTH = "Both";

    String USERS = "Users";
    String ONLINE_USERS = "OnlineUsers";

    String KEY_TARGET_USER_ID = "TARGET_USER_ID";

    String defaultImageUrl = "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcR9P4cKEb7yoU5HcuO2cxnBEWZ3xTCVB0VkWA&usqp=CAU";

}
