package com.hirehub.frontend.entretien;

public class EntretienAdminStatsApi {

    private long total;
    private long planifies;
    private long annules;

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public long getPlanifies() {
        return planifies;
    }

    public void setPlanifies(long planifies) {
        this.planifies = planifies;
    }

    public long getAnnules() {
        return annules;
    }

    public void setAnnules(long annules) {
        this.annules = annules;
    }
}
