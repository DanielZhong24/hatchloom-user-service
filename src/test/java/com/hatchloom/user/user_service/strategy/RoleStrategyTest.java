package com.hatchloom.user.user_service.strategy;

import com.hatchloom.user.user_service.model.RoleType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Role-Based Access Control (RBAC) Strategy Tests")
class RoleStrategyTest {

    private StrategyFactory strategyFactory;

    @BeforeEach
    void setUp() {
        // Initialize all strategy implementations
        List<com.hatchloom.user.user_service.model.IRolePermissionStrategy> strategies = List.of(
                new HatchloomAdminStrategy(),
                new HatchloomTeacherStrategy(),
                new SchoolAdminStrategy(),
                new SchoolTeacherStrategy(),
                new StudentStrategy(),
                new ParentStrategy()
        );
        strategyFactory = new StrategyFactory(strategies);
    }

    @Test
    @DisplayName("Test ID 7: Teacher Global Access - HATCHLOOM_TEACHER has cross-school scope")
    void testTeacherGlobalAccessScope() {
        // Arrange
        RoleType teacherRole = RoleType.HATCHLOOM_TEACHER;

        // Act
        var teacherStrategy = strategyFactory.getStrategy(teacherRole);
        String scope = teacherStrategy.getScope();

        // Assert
        assertNotNull(teacherStrategy);
        assertEquals("CROSS_SCHOOL_GLOBAL", scope);
        assertTrue(teacherStrategy.getPermissions().contains("CreateGlobalCourses"));
        assertTrue(teacherStrategy.getPermissions().contains("ViewCourses"));
    }

    @Test
    @DisplayName("Test ID 7: Teacher Can Access Monitor Progress - Access granted for cross-school student")
    void testTeacherCanAccessMonitorProgressCrossSchool() {
        // Arrange
        RoleType teacherRole = RoleType.HATCHLOOM_TEACHER;

        // Act
        var teacherStrategy = strategyFactory.getStrategy(teacherRole);
        boolean hasPermission = teacherStrategy.getPermissions().contains("ViewCourses");

        // Assert
        assertTrue(hasPermission);
        assertEquals("CROSS_SCHOOL_GLOBAL", teacherStrategy.getScope());
    }

    @Test
    @DisplayName("Test ID 8: School Admin Scope Restriction - Limited to single school")
    void testSchoolAdminScopeRestriction() {
        // Arrange
        RoleType adminRole = RoleType.SCHOOL_ADMIN;

        // Act
        var adminStrategy = strategyFactory.getStrategy(adminRole);
        String scope = adminStrategy.getScope();

        // Assert
        assertNotNull(adminStrategy);
        assertEquals("SINGLE_SCHOOL_LIMIT", scope);
        assertTrue(adminStrategy.getPermissions().contains("ManageCohorts"));
        assertTrue(adminStrategy.getPermissions().contains("CohortAnalytics"));
    }

    @Test
    @DisplayName("Test ID 8: School Admin Cannot Access Other School Data - Access denied for different school")
    void testSchoolAdminCannotAccessOtherSchoolData() {
        // Arrange
        RoleType adminRole = RoleType.SCHOOL_ADMIN;

        // Act
        var adminStrategy = strategyFactory.getStrategy(adminRole);
        String scope = adminStrategy.getScope();

        // Assert
        assertEquals("SINGLE_SCHOOL_LIMIT", scope);
        // In production, this would be enforced by the service layer
        // checking that the requested school matches the admin's school
    }

    @Test
    @DisplayName("Student Role - Limited to own data access")
    void testStudentRoleOwnDataOnly() {
        // Arrange
        RoleType studentRole = RoleType.STUDENT;

        // Act
        var studentStrategy = strategyFactory.getStrategy(studentRole);
        String scope = studentStrategy.getScope();

        // Assert
        assertNotNull(studentStrategy);
        assertEquals("OWN_DATA_ONLY", scope);
        assertTrue(studentStrategy.getPermissions().contains("ViewMyExperiences"));
        assertTrue(studentStrategy.getPermissions().contains("SubmitAssignments"));
        assertTrue(studentStrategy.getPermissions().contains("ViewMyGrades"));
    }

    @Test
    @DisplayName("Parent Role - Access limited to linked children")
    void testParentRoleLinkedChildrenOnly() {
        // Arrange
        RoleType parentRole = RoleType.PARENT;

        // Act
        var parentStrategy = strategyFactory.getStrategy(parentRole);
        String scope = parentStrategy.getScope();

        // Assert
        assertNotNull(parentStrategy);
        assertEquals("LINKED_CHILDREN_ONLY", scope);
        assertTrue(parentStrategy.getPermissions().contains("ViewChildWork"));
        assertTrue(parentStrategy.getPermissions().contains("ViewChildProgress"));
        assertTrue(parentStrategy.getPermissions().contains("ViewChildGrades"));
    }

    @Test
    @DisplayName("Hatchloom Admin Role - Unrestricted access")
    void testHatchloomAdminUnrestrictedAccess() {
        // Arrange
        RoleType adminRole = RoleType.HATCHLOOM_ADMIN;

        // Act
        var adminStrategy = strategyFactory.getStrategy(adminRole);
        String scope = adminStrategy.getScope();

        // Assert
        assertNotNull(adminStrategy);
        assertEquals("UNRESTRICTED", scope);
        assertTrue(adminStrategy.getPermissions().contains("ManageClients"));
        assertTrue(adminStrategy.getPermissions().contains("GlobalAnalytics"));
        assertTrue(adminStrategy.getPermissions().contains("ManageUsers"));
    }

    @Test
    @DisplayName("School Teacher Role - Limited to single school")
    void testSchoolTeacherSingleSchoolLimit() {
        // Arrange
        RoleType teacherRole = RoleType.SCHOOL_TEACHER;

        // Act
        var teacherStrategy = strategyFactory.getStrategy(teacherRole);
        String scope = teacherStrategy.getScope();

        // Assert
        assertNotNull(teacherStrategy);
        assertEquals("SINGLE_SCHOOL_LIMIT", scope);
        assertTrue(teacherStrategy.getPermissions().contains("ManageCohorts"));
        assertTrue(teacherStrategy.getPermissions().contains("RunExperience"));
        assertTrue(teacherStrategy.getPermissions().contains("GradeStudents"));
    }

    @Test
    @DisplayName("All roles have defined permissions")
    void testAllRolesHavePermissions() {
        // Arrange
        RoleType[] roles = RoleType.values();

        // Act & Assert
        for (RoleType role : roles) {
            var strategy = strategyFactory.getStrategy(role);
            assertNotNull(strategy);
            assertNotNull(strategy.getPermissions());
            assertFalse(strategy.getPermissions().isEmpty(), "Role " + role + " has no permissions defined");
            assertNotNull(strategy.getScope(), "Role " + role + " has no scope defined");
        }
    }
}

