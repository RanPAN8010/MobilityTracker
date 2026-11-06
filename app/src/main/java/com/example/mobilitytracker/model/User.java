package com.example.mobilitytracker.model;

/**
 * 用户数据模型类 (User)
 * 存储系统用户的身份信息（用户 ID、姓名、邮箱）
 * 以及角色权限（普通用户 USER / 管理员 ADMIN）。
 */
public class User {
    public static final String ROLE_USER = "USER";
    public static final String ROLE_ADMIN = "ADMIN";

    private String userId;
    private String name;
    private String email;
    private String role;

    // 获取法语角色描述
    public String getRoleDescriptionFrench() {
        if (ROLE_ADMIN.equals(role)) {
            return "Administrateur";
        }
        return "Utilisateur";
    }

    public User() {
    }

    public User(String userId, String name, String email, String role) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.role = role;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}