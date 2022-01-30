package com.task.mobi.utils;

public class Const {

    public static final String SERVER_REMOTE_URL = "http://demo2.pugmarks.in/smart_attendence/api/";
    public static final String ASSETS_BASE_URL = "http://demo2.pugmarks.in/smart_attendence/";
    public static final int PLAY_SERVICES_RESOLUTION_REQUEST = 1234;
    public final static int REQUEST_LOCATION = 199;
    /**
     * ******************************* App buy or Purchase Key *************************************
     */
    public static final String DEVICE_TOKEN = "dev_token";
    public static final String FOREGROUND = "forground_notification";
    public static final String CHAT_FOREGROUND = "forground_chat";
    public static final String NOTI_CHANNEL_ID = "my_custom_channel";
    public static final String NEW_MESSAGE_BROADCAST = "onNewMesage";
    public static final String SHOW_GOOGLE_MAPS_INTENT_DIALOG = "google_maps_dialog";
    public static final int LOCATION_TASK_JOB = 234;
    public static final int LOCATION_UPDATE_INTERVAL = 2 * 60 * 1000;

    public static final String USER_ID = "user_id";
    public static final String USER_DATA = "user_data";

    public static final int TASK_NEW = 0;
    public static final int TASK_START = 1;
    public static final int TASK_COMPLETE = 2;
    public static final int TASK_APPROVED = 3;

    public static final String TASK_ID = "task_id";
    public static final String REMEMBER_ME = "rememberMe";
    public static final String REMEMBER_EMAIL = "rem_email";
    public static final String REMEMBER_PASS = "rem_pass";
    public static final String SESSION_ID = "session_id";

    public static final String COMPANY = "company";
    public static final String USER = "user";
    public static final String TYPE_COMPANY = "1";
    public static final String TYPE_USER = "2";
    public static final String MSG_LEFT = "left";
    public static final String MSG_RIGHT = "right";

    public static final String LAST_LOCATION = "lastLoc";

    public static class ErrorCodes {
        public static final int SESSION_EXPIRE = 11;
        public static final int NO_USER = 12;
        public static final int USER_LIMIT_EXCEEDED = 13;
        public static final int NO_TASKS = 14;
        public static final int NO_COMMENTS = 15;
        public static final int USER_HAS_ACTIVE_TASK = 16;
        public static final int UNABLE_TO_SEND_MESSAGE = 17;
        public static final int EXPIRY_ARRIVAL = 18;
        public static final int ACCOUNT_DISABLE = 19;
    }
}