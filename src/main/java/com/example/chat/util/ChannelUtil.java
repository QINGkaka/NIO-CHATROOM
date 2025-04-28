package com.example.chat.util;

import io.netty.channel.Channel;
import io.netty.util.AttributeKey;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ChannelUtil {
    private static final Map<String, Channel> USER_CHANNEL_MAP = new ConcurrentHashMap<>();
    private static final AttributeKey<String> USER_ID_KEY = AttributeKey.valueOf("userId");

    public static void bindUser(Channel channel, String userId) {
        channel.attr(USER_ID_KEY).set(userId);
        USER_CHANNEL_MAP.put(userId, channel);
    }

    public static void unbindUser(Channel channel) {
        String userId = getUserId(channel);
        if (userId != null) {
            USER_CHANNEL_MAP.remove(userId);
            channel.attr(USER_ID_KEY).set(null);
        }
    }

    public static String getUserId(Channel channel) {
        return channel.attr(USER_ID_KEY).get();
    }

    public static Channel getChannel(String userId) {
        return USER_CHANNEL_MAP.get(userId);
    }
}
