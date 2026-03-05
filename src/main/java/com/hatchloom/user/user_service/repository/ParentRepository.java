package com.hatchloom.user.user_service.repository;

import com.hatchloom.user.user_service.model.Parent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ParentRepository extends JpaRepository<Parent, UUID> {
}

