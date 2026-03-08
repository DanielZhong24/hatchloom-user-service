package com.hatchloom.user.user_service.model;

import com.hatchloom.user.user_service.dto.ProfileDTO;
import com.hatchloom.user.user_service.dto.UpdateProfileRequest;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.Map;

@Entity
@Table(name = "professional_profiles")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ProfessionalProfile extends UserProfile {

    private String companyName;
    private String jobTitle;
    private String expertise;

    @Override
    public String getProfileType() {
        return "PROFESSIONAL";
    }

    @Override
    public void enrichProfileDTO(ProfileDTO.ProfileDTOBuilder builder, User user) {
        builder.companyName(this.companyName)
               .jobTitle(this.jobTitle)
               .expertise(this.expertise);
    }

    @Override
    public void applyUpdates(UpdateProfileRequest request, User user) {
        if (request.getCompanyName() != null) {
            this.companyName = request.getCompanyName();
        }
        if (request.getJobTitle() != null) {
            this.jobTitle = request.getJobTitle();
        }
        if (request.getExpertise() != null) {
            this.expertise = request.getExpertise();
        }
    }

    @Override
    public void initializeDefaults(User user) {
        this.jobTitle = "Not Set";
        this.companyName = "Not Set";
    }

    @Override
    public Map<String, Object> getExtendedFields() {
        return Map.of(
            "companyName", companyName != null ? companyName : "",
            "jobTitle", jobTitle != null ? jobTitle : "",
            "expertise", expertise != null ? expertise : ""
        );
    }
}


