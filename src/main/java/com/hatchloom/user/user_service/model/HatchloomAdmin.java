package com.hatchloom.user.user_service.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@Table(name = "hatchloom_admins")
@Data
@EqualsAndHashCode(callSuper = false)
public class HatchloomAdmin extends User {

    @Override
    public String getDisplayName() {
        return getUsername();
    }
}


