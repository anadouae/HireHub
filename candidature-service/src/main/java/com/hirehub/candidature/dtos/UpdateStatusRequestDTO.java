package com.hirehub.candidature.dtos;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateStatusRequestDTO {
    private String status;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}