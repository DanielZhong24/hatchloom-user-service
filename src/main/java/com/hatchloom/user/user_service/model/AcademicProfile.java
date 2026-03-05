package com.hatchloom.user.user_service.model;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@DiscriminatorValue("ACADEMIC")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class AcademicProfile extends UserProfile {

    private String gradeLevel;
    private String specialization;

    @Override
    public String getProfileType() {
        return "ACADEMIC";
    }
}


