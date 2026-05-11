package com.example.skhubox.common;

public final class RedisKeys {

    private RedisKeys() {}

    public static final String EMAIL_VERIFY = "email:verify:";
    public static final String EMAIL_VERIFIED = "email:verified:";
    public static final String PASSWORD_RESET = "password:reset:";
    public static final String REFRESH_TOKEN = "refresh:token:";
    public static final String LOCKER_LOCK = "lock:locker:";
    public static final String LOCKER_QUEUE = "locker:queue:";
}
