package com.hirehub.frontend.admin;

public record AdminPlatformStats(
        long offresBrouillon,
        long offresPubliees,
        long offresFermees,
        long candidaturesTotal,
        long candidaturesSoumises,
        long entretiensTotal,
        long entretiensPlanifies,
        long entretiensAnnules
) {
    public static AdminPlatformStats empty() {
        return new AdminPlatformStats(0, 0, 0, 0, 0, 0, 0, 0);
    }
}
