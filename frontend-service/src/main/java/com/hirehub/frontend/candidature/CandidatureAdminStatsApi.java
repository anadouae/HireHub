package com.hirehub.frontend.candidature;

import java.util.Map;

public class CandidatureAdminStatsApi {

    private long total;
    private Map<String, Long> byStatus;

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public Map<String, Long> getByStatus() {
        return byStatus;
    }

    public void setByStatus(Map<String, Long> byStatus) {
        this.byStatus = byStatus;
    }
}
