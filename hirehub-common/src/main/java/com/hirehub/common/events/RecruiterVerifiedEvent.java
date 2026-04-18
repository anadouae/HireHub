package com.hirehub.common.events;
public class RecruiterVerifiedEvent {
    private String userId;
    private int verificationScore;
    private String verificationStatus;
    private boolean autoApproved;
    private boolean needsAdminReview;
    private String decisionMessage;
    private String verificationSource;

    public RecruiterVerifiedEvent() {
    }

    public RecruiterVerifiedEvent(String userId, int verificationScore, String verificationStatus, boolean autoApproved,
                                  boolean needsAdminReview, String decisionMessage, String verificationSource) {
        this.userId = userId;
        this.verificationScore = verificationScore;
        this.verificationStatus = verificationStatus;
        this.autoApproved = autoApproved;
        this.needsAdminReview = needsAdminReview;
        this.decisionMessage = decisionMessage;
        this.verificationSource = verificationSource;
    }

    public String getUserId() {
        return userId;
    }

    public int getVerificationScore() {
        return verificationScore;
    }

    public String getVerificationStatus() {
        return verificationStatus;
    }

    public boolean isAutoApproved() {
        return autoApproved;
    }

    public boolean isNeedsAdminReview() {
        return needsAdminReview;
    }

    public String getDecisionMessage() {
        return decisionMessage;
    }

    public String getVerificationSource() {
        return verificationSource;
    }
}
