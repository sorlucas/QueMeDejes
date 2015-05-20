package com.example.sergio.myapplication.backend;

import com.google.api.server.spi.Constant;

/**
 * Created by sergio on 20/05/15.
 */
public class Constants {
    public static final String WEB_CLIENT_ID = "732155756921-56040206vk5docd2h91kqqurpcu41b70.apps.googleusercontent.com";
    public static final String ANDROID_CLIENT_ID = "732155756921-tlpn5olkjuqo40lepgnhadavv1hd0cqi.apps.googleusercontent.com";
    public static final String IOS_CLIENT_ID = "replace this with your iOS client ID";
    public static final String ANDROID_AUDIENCE = WEB_CLIENT_ID;
    public static final String EMAIL_SCOPE = Constant.API_EMAIL_SCOPE;
    public static final String API_EXPLORER_CLIENT_ID = Constant.API_EXPLORER_CLIENT_ID;

    public static final String MEMCACHE_ANNOUNCEMENTS_KEY = "RECENT_ANNOUNCEMENTS";
}
