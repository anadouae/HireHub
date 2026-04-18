package com.hirehub.common.events;

public class UserAdminActionEvent {
    private String userId;
    private String email;
    private String role;
    private String action;
    private String source;

    public UserAdminActionEvent() {
    }

    public UserAdminActionEvent(String userId, String email, String role, String action, String source) {
        this.userId = userId;
        this.email = email;
        this.role = role;
        this.action = action;
        this.source = source;
    }

    public String getUserId() {
        return userId;
    }

    public String getEmail() {
        return email;
    }

    public String getRole() {
        return role;
    }

    public String getAction() {
        return action;
    }

    public String getSource() {
        return source;
    }
}
