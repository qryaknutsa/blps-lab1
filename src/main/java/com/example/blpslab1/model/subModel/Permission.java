package com.example.blpslab1.model.subModel;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Permission {

    ADMIN_READ_FILE("admin:read-file"),
    ADMIN_UPDATE_FILE("admin:update-file"),
    ADMIN_CREATE_FILE("admin:create-file"),
    ADMIN_DELETE_FILE("admin:delete-file"),
    ADMIN_CREATE_USER("admin:create-user"),
    ADMIN_DELETE_USER("admin:delete-user"),
    ADMIN_CREATE_ADMIN("admin:create-admin"),
    ADMIN_DELETE_ADMIN("admin:delete-admin"),
    ADMIN_CHANGE_MONEY("admin:change-money"),
    ADMIN_CHANGE_SUB("admin:change-sub"),

    USER_READ_FILE("user:read-file"),
    USER_UPDATE_FILE("user:update-file"),
    USER_CREATE_FILE("user:create-file"),
    USER_DELETE_FILE("user:delete-file"),
    USER_UPDATE_ME("user:update-me"),
    USER_DELETE_ME("user:delete-me"),
    USER_CHANGE_MONEY("user:change-money"),
    USER_CHANGE_SUB("user:change-sub")
    ;

    private final String permission;
}
