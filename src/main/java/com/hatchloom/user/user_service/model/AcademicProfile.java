package com.hatchloom.user.user_service.model;

import com.hatchloom.user.user_service.dto.ProfileDTO;
import com.hatchloom.user.user_service.dto.UpdateProfileRequest;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Entity
@Table(name = "academic_profiles")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class AcademicProfile extends UserProfile {

    private String gradeLevel;
    private String specialization;

    // Student-specific progression metrics (only populated for Student users)
    @Column
    private LocalDateTime lastActive;

    @Column
    private Integer skillsCertified;

    @Column
    private Integer explorerLevelXp;

    @Column
    private Integer currentStreak;

    @Column
    private Integer activeVentures;

    @Column
    private Integer problemsTackled;

    @Override
    public String getProfileType() {
        return "ACADEMIC";
    }

    @Override
    public void enrichProfileDTO(ProfileDTO.ProfileDTOBuilder builder, User user) {
        builder.gradeLevel(this.gradeLevel)
               .specialization(this.specialization);

        // Only include student metrics if this is a Student user's profile
        if (user instanceof Student) {
            builder.lastActive(this.lastActive == null ? null : this.lastActive.toString())
                   .skillsCertified(this.skillsCertified)
                   .explorerLevelXp(this.explorerLevelXp)
                   .currentStreak(this.currentStreak)
                   .activeVentures(this.activeVentures)
                   .problemsTackled(this.problemsTackled);
        }
    }

    @Override
    public void applyUpdates(UpdateProfileRequest request, User user) {
        if (request.getGradeLevel() != null) {
            this.gradeLevel = request.getGradeLevel();
        }
        if (request.getSpecialization() != null) {
            this.specialization = request.getSpecialization();
        }

        // Student progression counters only apply to Student users
        if (user instanceof Student) {
            if (request.getSkillsCertified() != null) {
                this.skillsCertified = request.getSkillsCertified();
            }
            if (request.getExplorerLevelXp() != null) {
                this.explorerLevelXp = request.getExplorerLevelXp();
            }
            if (request.getCurrentStreak() != null) {
                this.currentStreak = request.getCurrentStreak();
            }
            if (request.getActiveVentures() != null) {
                this.activeVentures = request.getActiveVentures();
            }
            if (request.getProblemsTackled() != null) {
                this.problemsTackled = request.getProblemsTackled();
            }
        }
    }

    @Override
    public void onUserLogin(User user) {
        // Update lastActive timestamp for Student users on successful login
        if (user instanceof Student) {
            this.lastActive = LocalDateTime.now();
        }
    }

    @Override
    public void initializeDefaults(User user) {
        this.gradeLevel = "Not Set";
        this.specialization = "Not Set";

        // Student metrics initialize as null (will show in JSON)
        if (user instanceof Student) {
            this.skillsCertified = null;
            this.explorerLevelXp = null;
            this.currentStreak = null;
            this.activeVentures = null;
            this.problemsTackled = null;
            this.lastActive = null;
        }
    }

    @Override
    public Map<String, Object> getExtendedFields() {
        Map<String, Object> fields = new HashMap<>();
        fields.put("gradeLevel", gradeLevel);
        fields.put("specialization", specialization);

        if (this.getUser() instanceof Student) {
            fields.put("lastActive", lastActive);
            fields.put("skillsCertified", skillsCertified);
            fields.put("explorerLevelXp", explorerLevelXp);
            fields.put("currentStreak", currentStreak);
            fields.put("activeVentures", activeVentures);
            fields.put("problemsTackled", problemsTackled);
        }

        return fields;
    }
}
