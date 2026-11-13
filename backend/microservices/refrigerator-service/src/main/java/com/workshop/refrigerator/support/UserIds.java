package com.workshop.refrigerator.support;

public final class UserIds {

    public static final String DEFAULT = "test";

    private UserIds() {
    }

    public static String resolve(String userId) {
        return (userId == null || userId.isBlank()) ? DEFAULT : userId;
    }
}
