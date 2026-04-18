package com.hirehub.frontend.admin;

import com.hirehub.frontend.auth.FrontendUserAccount;

/**
 * Copie simple pour l'affichage admin (évite JPA hors transaction ; getters JavaBeans pour Thymeleaf).
 */
public final class AdminUserDetailVm {

    private final String email;
    private final String fullName;
    private final String role;
    private final boolean blocked;

    private AdminUserDetailVm(String email, String fullName, String role, boolean blocked) {
        this.email = email;
        this.fullName = fullName;
        this.role = role;
        this.blocked = blocked;
    }

    public static AdminUserDetailVm from(FrontendUserAccount u) {
        return new AdminUserDetailVm(
                u.getEmail(),
                u.getFullName(),
                u.getRole().name(),
                u.isBlocked()
        );
    }

    public String getEmail() {
        return email;
    }

    public String getFullName() {
        return fullName;
    }

    public String getRole() {
        return role;
    }

    public boolean isBlocked() {
        return blocked;
    }
}
