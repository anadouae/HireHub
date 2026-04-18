package com.hirehub.frontend.admin;

public record AdminDashboardStats(
        long totalUsers,
        long candidats,
        long recruteurs,
        long admins,
        long recruiterPendingReview
) {
}
