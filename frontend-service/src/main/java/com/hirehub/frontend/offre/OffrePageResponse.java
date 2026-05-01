package com.hirehub.frontend.offre;

import java.util.ArrayList;
import java.util.List;

public class OffrePageResponse {

    private List<OffreView> content = new ArrayList<>();
    private long totalElements;
    private int totalPages;
    private int number;
    private int size;

    public List<OffreView> getContent() {
        return content;
    }

    public void setContent(List<OffreView> content) {
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

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }
}
