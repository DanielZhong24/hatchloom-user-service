package com.hatchloom.user.user_service.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "school_teachers")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class SchoolTeacher extends User {
    @Column(nullable = false)
    private UUID schoolId;

    @Override
    public String getDisplayName() {
        return getUsername();
    }
}


