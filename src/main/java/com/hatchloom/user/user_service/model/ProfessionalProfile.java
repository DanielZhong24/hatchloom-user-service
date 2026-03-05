package com.hatchloom.user.user_service.model;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@DiscriminatorValue("PROFESSIONAL")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class ProfessionalProfile extends UserProfile {

    private String companyName;
    private String jobTitle;
    private String expertise;

    @Override
    public String getProfileType() {
        return "PROFESSIONAL";
    }
}


