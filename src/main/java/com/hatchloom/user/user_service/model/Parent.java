package com.hatchloom.user.user_service.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Table(name = "parents")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class Parent extends User {

    @OneToMany(mappedBy = "parent", cascade = CascadeType.REFRESH)
    private List<Student> linkedChildren;

    @Override
    public String getDisplayName() {
        return getUsername();
    }
}


