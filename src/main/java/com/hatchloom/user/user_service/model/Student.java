package com.hatchloom.user.user_service.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "students")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class Student extends User {
    @Column(nullable = false)
    private UUID schoolId;

    @Column(nullable = false)
    private Integer age;

    @ManyToOne
    @JoinColumn(name = "parent_id", nullable = true)
    private Parent parent;

    @Override
    public String getDisplayName() {
        return getUsername();
    }
}


