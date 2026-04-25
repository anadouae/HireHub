package com.hirehub.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class HtmlContentDTO {
    private String recipientEmail;
    private String subject;
    private String body;
}

