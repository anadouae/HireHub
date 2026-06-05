package com.hirehub.frontend.entretien;

import java.util.ArrayList;
import java.util.List;

public class EntretienAdminPageResponse {

    private List<EntretienView> content = new ArrayList<>();
    private long totalElements;
    private int totalPages;
    private int number;

    public List<EntretienView> getContent() {
        return content;
    }

    public void setContent(List<EntretienView> content) {
        this.content = content != null ? content : new ArrayList<>();
    }

    public long getTotalElements() {
        return totalElements;
    }

    public void setTotalElements(long totalElements) {
        this.totalElements = totalElements;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }
}
