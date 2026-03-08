package com.hatchloom.user.user_service.model;

import com.hatchloom.user.user_service.dto.ProfileDTO;
import com.hatchloom.user.user_service.dto.UpdateProfileRequest;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "user_profiles")
@Inheritance(strategy = InheritanceType.JOINED)
@Data
@EqualsAndHashCode(callSuper = false)
public abstract class UserProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(columnDefinition = "TEXT")
    private String bio;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column
    private String profilePictureUrl;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public abstract String getProfileType();

    /**
     * Polymorphic method: Each profile type enriches the DTO builder with its specific fields.
     * Eliminates instanceof checks in service layer.
     * @param builder The DTO builder to enrich
     * @param user The user this profile belongs to (for type checking)
     */
    public abstract void enrichProfileDTO(ProfileDTO.ProfileDTOBuilder builder, User user);

    /**
     * Polymorphic method: Each profile type applies updates from the request.
     * Eliminates instanceof checks in service layer.
     * @param request The update request with new field values
     * @param user The user this profile belongs to (for type checking)
     */
    public abstract void applyUpdates(UpdateProfileRequest request, User user);

    /**
     * Hook for subclasses to perform post-login actions (e.g., update lastActive).
     * Default implementation does nothing.
     * @param user The user this profile belongs to
     */
    public void onUserLogin(User user) {
        // Default: no-op
    }

    /**
     * Hook for subclasses to initialize default values on profile creation.
     * Default implementation does nothing.
     * @param user The user this profile belongs to (for type checking)
     */
    public void initializeDefaults(User user) {
        // Default: no-op
    }

    /**
     * Returns profile-specific fields as a map for extension/introspection.
     * Default implementation returns empty map.
     */
    public Map<String, Object> getExtendedFields() {
        return Map.of();
    }
}


