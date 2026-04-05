package com.hirehub.common.events;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RecruiterApprovedEvent {
    private String userId;
    private String email;
    private String nom;
    private String prenom;
    private boolean approved;   // true = approuvé, false = refusé
    private String reason;      // motif si refusé
 }
