# User Service API Documentation

## Overview

User Service microservice for the Hatchloom platform. Manages polymorphic user types, JWT-based authentication, role-based authorization, user profiles, and parent-child linking.

## Features

- Polymorphic User Model (6 user types)
- JWT-based Authentication with 30-minute tokens
- Refresh Token Mechanism (7-day expiry)
- Session Management and Validation
- Strategy-based Role Authorization
- User Profiles (Academic and Professional)
- Parent-Child Account Linking
- Role-based Access Control
- PostgreSQL Integration
- Non-verbose Error Handling (prevents information disclosure)

## Quick Start

Start the service with:

```
docker-compose up -d
mvn spring-boot:run
```

The service will be available at http://localhost:8080

## Configuration

Edit src/main/resources/application.properties:

```
jwt.access-token-expiry-minutes=30
jwt.refresh-token-expiry-days=7
jwt.secret=your-secret-key-here
spring.datasource.url=jdbc:postgresql://postgres:5432/mydatabase
spring.datasource.username=myuser
spring.datasource.password=secret
```

## API Endpoints (9 Total)

### Authentication Endpoints

#### POST /auth/register - Register User

Request:
```json
{
  "username": "john_doe",
  "email": "john@example.com",
  "password": "SecurePassword123",
  "role": "STUDENT",
  "schoolId": "550e8400-e29b-41d4-a716-446655440000",
  "age": 18
}
```

Response (201 Created):
```json
{
  "userId": "550e8400-e29b-41d4-a716-446655440001",
  "username": "john_doe",
  "role": "STUDENT",
  "message": "Registration successful"
}
```

Notes:
- Email uniqueness is enforced
- Generic error responses prevent information disclosure
- Password is bcrypt-hashed
- Role-specific required fields: schoolId (STUDENT/SCHOOL_TEACHER/SCHOOL_ADMIN), age (STUDENT)

#### POST /auth/login - Authenticate User

Request:
```json
{
  "username": "john_doe",
  "password": "SecurePassword123"
}
```

Response (200 OK):
```json
{
  "accessToken": "eyJhbGciOiJIUzUxMiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJIUzUxMiIsInR5cCI6IkpXVCJ9...",
  "role": "STUDENT",
  "username": "john_doe",
  "message": "Login successful"
}
```

Token Details:
- Access Token: 30 minutes expiry (configurable)
- Refresh Token: 7 days expiry (configurable)
- Both tokens are JWT-based with HMAC-SHA512 algorithm

#### POST /auth/refresh - Refresh Access Token

Request:
```json
{
  "refreshToken": "eyJhbGciOiJIUzUxMiIsInR5cCI6IkpXVCJ9..."
}
```

Response (200 OK):
```json
{
  "accessToken": "eyJhbGciOiJIUzUxMiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJIUzUxMiIsInR5cCI6IkpXVCJ9...",
  "role": "STUDENT",
  "username": "john_doe",
  "message": "Token refreshed successfully"
}
```

#### GET /auth/validate - Validate Session Token

Headers:
```
Authorization: Bearer eyJhbGciOiJIUzUxMiIsInR5cCI6IkpXVCJ9...
```

Response (200 OK):
```json
{
  "valid": true,
  "userId": "550e8400-e29b-41d4-a716-446655440001",
  "role": "STUDENT",
  "message": "Token is valid"
}
```

Response (401 Unauthorized):
```json
{
  "valid": false,
  "message": "Invalid or expired token"
}
```

#### GET /auth/permissions - Get Role Permissions

Headers:
```
Authorization: Bearer eyJhbGciOiJIUzUxMiIsInR5cCI6IkpXVCJ9...
```

Response (200 OK):
```json
{
  "role": "STUDENT",
  "permissions": [
    "ViewMyExperiences",
    "SubmitAssignments",
    "ViewMyGrades",
    "UpdateOwnProfile",
    "ViewMyProgress"
  ],
  "scope": "OWN_DATA_ONLY"
}
```

#### POST /auth/link-parent/{studentId} - Link Parent to Student

Headers:
```
Authorization: Bearer eyJhbGciOiJIUzUxMiIsInR5cCI6IkpXVCJ9...
```

Path Parameters:
- studentId: UUID of the student to link

Response (200 OK):
```
Parent linked successfully
```

Authorization:
- Only users with PARENT role can call this endpoint
- Links the parent account to the specified student

### Profile Endpoints

#### GET /profile/{userId} - Get User Profile

Headers:
```
Authorization: Bearer eyJhbGciOiJIUzUxMiIsInR5cCI6IkpXVCJ9...
```

Path Parameters:
- userId: UUID of the user

Response (200 OK):
```json
{
  "userId": "550e8400-e29b-41d4-a716-446655440001",
  "username": "john_doe",
  "email": "john@example.com",
  "role": "STUDENT",
  "bio": "I love learning",
  "description": "High school student",
  "profilePictureUrl": "https://example.com/pic.jpg",
  "gradeLevel": "12",
  "specialization": "STEM",
  "createdAt": "2026-03-04T23:00:00",
  "updatedAt": "2026-03-04T23:15:00"
}
```

Authorization:
- Users can view their own profile
- Only HATCHLOOM_ADMIN and SCHOOL_ADMIN can view any profile

#### PUT /profile/{userId} - Update User Profile

Headers:
```
Authorization: Bearer eyJhbGciOiJIUzUxMiIsInR5cCI6IkpXVCJ9...
Content-Type: application/json
```

Path Parameters:
- userId: UUID of the user

Request:
```json
{
  "bio": "Updated bio",
  "description": "Updated description",
  "profilePictureUrl": "https://example.com/new-pic.jpg",
  "gradeLevel": "12",
  "specialization": "STEM"
}
```

Response (200 OK):
```json
{
  "userId": "550e8400-e29b-41d4-a716-446655440001",
  "username": "john_doe",
  "email": "john@example.com",
  "role": "STUDENT",
  "bio": "Updated bio",
  "description": "Updated description",
  "profilePictureUrl": "https://example.com/new-pic.jpg",
  "gradeLevel": "12",
  "specialization": "STEM",
  "createdAt": "2026-03-04T23:00:00",
  "updatedAt": "2026-03-04T23:20:00"
}
```

Authorization:
- Users can only update their own profile
- Read-only fields: username, email, role, createdAt

#### GET /profile?page=0&size=20 - List All Profiles (Admin Only)

Headers:
```
Authorization: Bearer eyJhbGciOiJIUzUxMiIsInR5cCI6IkpXVCJ9...
```

Query Parameters:
- page: Page number (0-indexed, default: 0)
- size: Results per page (default: 20)

Response (200 OK):
```json
{
  "content": [
    {
      "userId": "550e8400-e29b-41d4-a716-446655440001",
      "username": "john_doe",
      "email": "john@example.com",
      "role": "STUDENT",
      "bio": "I love learning",
      "description": "High school student",
      "profilePictureUrl": "https://example.com/pic.jpg",
      "createdAt": "2026-03-04T23:00:00",
      "updatedAt": "2026-03-04T23:15:00"
    }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 20,
    "totalElements": 1,
    "totalPages": 1
  }
}
```

Authorization:
- Only HATCHLOOM_ADMIN and SCHOOL_ADMIN can list profiles

## User Roles and Permissions

HATCHLOOM_ADMIN
- Permissions: ManageClients, GlobalAnalytics, ManageUsers, ManageSchools
- Scope: UNRESTRICTED
- Description: Full access to all system resources

HATCHLOOM_TEACHER
- Permissions: CreateGlobalCourses, ViewCourses, UpdateOwnCourses
- Scope: CROSS_SCHOOL_GLOBAL
- Description: Can create and manage global courses across schools

SCHOOL_ADMIN
- Permissions: CohortAnalytics, ManageCohorts, AddRemoveStudents, ManageTeachers, ViewSchoolData
- Scope: SINGLE_SCHOOL_LIMIT
- Description: Manages a single school's operations

SCHOOL_TEACHER
- Permissions: ManageCohorts, RunExperience, GradeStudents, ViewCohortData, UpdateOwnProfile
- Scope: SINGLE_SCHOOL_LIMIT
- Description: Teaches at a single school

STUDENT
- Permissions: ViewMyExperiences, SubmitAssignments, ViewMyGrades, UpdateOwnProfile, ViewMyProgress
- Scope: OWN_DATA_ONLY
- Description: Can view own experiences and grades

PARENT
- Permissions: ViewChildWork, ViewChildProgress, ViewChildGrades, UpdateOwnProfile, ContactTeacher
- Scope: LINKED_CHILDREN_ONLY
- Description: Can view linked child's work and progress

## Error Handling

All error responses follow this format:

```json
{
  "status": 400,
  "message": "Invalid request",
  "timestamp": 1709613300000
}
```

Common Error Scenarios:

Duplicate email registration:
- Status: 400
- Message: Invalid registration request

Invalid credentials:
- Status: 401
- Message: Invalid credentials

Expired token:
- Status: 401
- Message: Invalid or expired token

Missing authorization header:
- Status: 401
- Message: Unauthorized

Insufficient permissions:
- Status: 403
- Message: Access forbidden

Resource not found:
- Status: 404
- Message: Not found

Server error:
- Status: 500
- Message: An error occurred processing your request

Error messages are intentionally generic to prevent hackers from enumerating valid usernames/emails.

## Complete API Examples by User Type

This section provides comprehensive examples for all six user types, showing registration, login, and role-specific operations.

### Example 1: STUDENT User Flow

#### Step 1: Register as Student

Request:
```bash
curl -X POST http://localhost:8080/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "emma_student",
    "email": "emma@student.edu",
    "password": "StudentPass123",
    "role": "STUDENT",
    "schoolId": "550e8400-e29b-41d4-a716-446655440000",
    "age": 16
  }'
```

Response (201 Created):
```json
{
  "userId": "a1b2c3d4-e5f6-4a5b-8c9d-1e2f3a4b5c6d",
  "username": "emma_student",
  "role": "STUDENT",
  "message": "Registration successful"
}
```

#### Step 2: Login as Student

Request:
```bash
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "emma_student",
    "password": "StudentPass123"
  }'
```

Response (200 OK):
```json
{
  "accessToken": "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJlbW1hX3N0dWRlbnQiLCJyb2xlIjoiU1RVRE...",
  "refreshToken": "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJlbW1hX3N0dWRlbnQiLCJ0eXBlIjoicm...",
  "role": "STUDENT",
  "username": "emma_student",
  "message": "Login successful"
}
```

#### Step 3: View Own Profile (Student)

Request:
```bash
curl -X GET http://localhost:8080/profile/a1b2c3d4-e5f6-4a5b-8c9d-1e2f3a4b5c6d \
  -H "Authorization: Bearer eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJlbW1hX3N0dWRlbnQi..."
```

Response (200 OK):
```json
{
  "userId": "a1b2c3d4-e5f6-4a5b-8c9d-1e2f3a4b5c6d",
  "username": "emma_student",
  "email": "emma@student.edu",
  "role": "STUDENT",
  "bio": null,
  "description": null,
  "profilePictureUrl": null,
  "gradeLevel": null,
  "specialization": null,
  "createdAt": "2026-03-05T10:00:00",
  "updatedAt": "2026-03-05T10:00:00"
}
```

#### Step 4: Update Own Profile (Student)

Request:
```bash
curl -X PUT http://localhost:8080/profile/a1b2c3d4-e5f6-4a5b-8c9d-1e2f3a4b5c6d \
  -H "Authorization: Bearer eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJlbW1hX3N0dWRlbnQi..." \
  -H "Content-Type: application/json" \
  -d '{
    "bio": "Passionate about science and technology",
    "description": "10th grade student interested in STEM",
    "profilePictureUrl": "https://example.com/emma.jpg",
    "gradeLevel": "10",
    "specialization": "Computer Science"
  }'
```

Response (200 OK):
```json
{
  "userId": "a1b2c3d4-e5f6-4a5b-8c9d-1e2f3a4b5c6d",
  "username": "emma_student",
  "email": "emma@student.edu",
  "role": "STUDENT",
  "bio": "Passionate about science and technology",
  "description": "10th grade student interested in STEM",
  "profilePictureUrl": "https://example.com/emma.jpg",
  "gradeLevel": "10",
  "specialization": "Computer Science",
  "createdAt": "2026-03-05T10:00:00",
  "updatedAt": "2026-03-05T10:15:00"
}
```

#### Step 5: Get Student Permissions

Request:
```bash
curl -X GET http://localhost:8080/auth/permissions \
  -H "Authorization: Bearer eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJlbW1hX3N0dWRlbnQi..."
```

Response (200 OK):
```json
{
  "role": "STUDENT",
  "permissions": [
    "ViewMyExperiences",
    "SubmitAssignments",
    "ViewMyGrades",
    "UpdateOwnProfile",
    "ViewMyProgress"
  ],
  "scope": "OWN_DATA_ONLY"
}
```

#### Step 6: Student Attempts to View Another Profile (Forbidden)

Request:
```bash
curl -X GET http://localhost:8080/profile/b2c3d4e5-f6a7-4b5c-8d9e-2f3a4b5c6d7e \
  -H "Authorization: Bearer eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJlbW1hX3N0dWRlbnQi..."
```

Response (403 Forbidden):
```json
{
  "status": 403,
  "message": "Access forbidden",
  "timestamp": 1709631000000
}
```

---

### Example 2: PARENT User Flow

#### Step 1: Register as Parent

Request:
```bash
curl -X POST http://localhost:8080/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "sarah_parent",
    "email": "sarah@parent.com",
    "password": "ParentPass123",
    "role": "PARENT"
  }'
```

Response (201 Created):
```json
{
  "userId": "b2c3d4e5-f6a7-4b5c-8d9e-2f3a4b5c6d7e",
  "username": "sarah_parent",
  "role": "PARENT",
  "message": "Registration successful"
}
```

Note: PARENT role does not require schoolId or age fields.

#### Step 2: Login as Parent

Request:
```bash
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "sarah_parent",
    "password": "ParentPass123"
  }'
```

Response (200 OK):
```json
{
  "accessToken": "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJzYXJhaF9wYXJlbnQiLCJyb2xlIjoiUE...",
  "refreshToken": "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJzYXJhaF9wYXJlbnQiLCJ0eXBlIjoicm...",
  "role": "PARENT",
  "username": "sarah_parent",
  "message": "Login successful"
}
```

#### Step 3: Link Parent to Student

Request:
```bash
curl -X POST http://localhost:8080/auth/link-parent/a1b2c3d4-e5f6-4a5b-8c9d-1e2f3a4b5c6d \
  -H "Authorization: Bearer eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJzYXJhaF9wYXJlbnQi..."
```

Response (200 OK):
```
Parent linked successfully
```

Note: This links the parent to student emma_student (ID: a1b2c3d4-e5f6-4a5b-8c9d-1e2f3a4b5c6d).

#### Step 4: Get Parent Permissions

Request:
```bash
curl -X GET http://localhost:8080/auth/permissions \
  -H "Authorization: Bearer eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJzYXJhaF9wYXJlbnQi..."
```

Response (200 OK):
```json
{
  "role": "PARENT",
  "permissions": [
    "ViewChildWork",
    "ViewChildProgress",
    "ViewChildGrades",
    "UpdateOwnProfile",
    "ContactTeacher"
  ],
  "scope": "LINKED_CHILDREN_ONLY"
}
```

#### Step 5: Update Own Profile (Parent)

Request:
```bash
curl -X PUT http://localhost:8080/profile/b2c3d4e5-f6a7-4b5c-8d9e-2f3a4b5c6d7e \
  -H "Authorization: Bearer eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJzYXJhaF9wYXJlbnQi..." \
  -H "Content-Type: application/json" \
  -d '{
    "bio": "Parent of high school student",
    "description": "Actively involved in child education",
    "profilePictureUrl": "https://example.com/sarah.jpg"
  }'
```

Response (200 OK):
```json
{
  "userId": "b2c3d4e5-f6a7-4b5c-8d9e-2f3a4b5c6d7e",
  "username": "sarah_parent",
  "email": "sarah@parent.com",
  "role": "PARENT",
  "bio": "Parent of high school student",
  "description": "Actively involved in child education",
  "profilePictureUrl": "https://example.com/sarah.jpg",
  "gradeLevel": null,
  "specialization": null,
  "createdAt": "2026-03-05T10:20:00",
  "updatedAt": "2026-03-05T10:25:00"
}
```

---

### Example 3: SCHOOL_TEACHER User Flow

#### Step 1: Register as School Teacher

Request:
```bash
curl -X POST http://localhost:8080/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "mike_teacher",
    "email": "mike@school.edu",
    "password": "TeacherPass123",
    "role": "SCHOOL_TEACHER",
    "schoolId": "550e8400-e29b-41d4-a716-446655440000"
  }'
```

Response (201 Created):
```json
{
  "userId": "c3d4e5f6-a7b8-4c5d-9e0f-3a4b5c6d7e8f",
  "username": "mike_teacher",
  "role": "SCHOOL_TEACHER",
  "message": "Registration successful"
}
```

Note: SCHOOL_TEACHER requires schoolId but not age.

#### Step 2: Login as School Teacher

Request:
```bash
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "mike_teacher",
    "password": "TeacherPass123"
  }'
```

Response (200 OK):
```json
{
  "accessToken": "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJtaWtlX3RlYWNoZXIiLCJyb2xlIjoiU0...",
  "refreshToken": "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJtaWtlX3RlYWNoZXIiLCJ0eXBlIjoicm...",
  "role": "SCHOOL_TEACHER",
  "username": "mike_teacher",
  "message": "Login successful"
}
```

#### Step 3: Get Teacher Permissions

Request:
```bash
curl -X GET http://localhost:8080/auth/permissions \
  -H "Authorization: Bearer eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJtaWtlX3RlYWNoZXIi..."
```

Response (200 OK):
```json
{
  "role": "SCHOOL_TEACHER",
  "permissions": [
    "ManageCohorts",
    "RunExperience",
    "GradeStudents",
    "ViewCohortData",
    "UpdateOwnProfile"
  ],
  "scope": "SINGLE_SCHOOL_LIMIT"
}
```

#### Step 4: Update Own Profile (Teacher)

Request:
```bash
curl -X PUT http://localhost:8080/profile/c3d4e5f6-a7b8-4c5d-9e0f-3a4b5c6d7e8f \
  -H "Authorization: Bearer eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJtaWtlX3RlYWNoZXIi..." \
  -H "Content-Type: application/json" \
  -d '{
    "bio": "Mathematics teacher with 10 years experience",
    "description": "Specialized in advanced algebra and calculus",
    "profilePictureUrl": "https://example.com/mike.jpg",
    "specialization": "Mathematics"
  }'
```

Response (200 OK):
```json
{
  "userId": "c3d4e5f6-a7b8-4c5d-9e0f-3a4b5c6d7e8f",
  "username": "mike_teacher",
  "email": "mike@school.edu",
  "role": "SCHOOL_TEACHER",
  "bio": "Mathematics teacher with 10 years experience",
  "description": "Specialized in advanced algebra and calculus",
  "profilePictureUrl": "https://example.com/mike.jpg",
  "gradeLevel": null,
  "specialization": "Mathematics",
  "createdAt": "2026-03-05T10:30:00",
  "updatedAt": "2026-03-05T10:35:00"
}
```

---

### Example 4: SCHOOL_ADMIN User Flow

#### Step 1: Register as School Admin

Request:
```bash
curl -X POST http://localhost:8080/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "lisa_admin",
    "email": "lisa@school.edu",
    "password": "AdminPass123",
    "role": "SCHOOL_ADMIN",
    "schoolId": "550e8400-e29b-41d4-a716-446655440000"
  }'
```

Response (201 Created):
```json
{
  "userId": "d4e5f6a7-b8c9-4d5e-0f1a-4b5c6d7e8f9a",
  "username": "lisa_admin",
  "role": "SCHOOL_ADMIN",
  "message": "Registration successful"
}
```

#### Step 2: Login as School Admin

Request:
```bash
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "lisa_admin",
    "password": "AdminPass123"
  }'
```

Response (200 OK):
```json
{
  "accessToken": "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJsaXNhX2FkbWluIiwicm9sZSI6IlNDSE...",
  "refreshToken": "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJsaXNhX2FkbWluIiwidHlwZSI6InJlZn...",
  "role": "SCHOOL_ADMIN",
  "username": "lisa_admin",
  "message": "Login successful"
}
```

#### Step 3: Get School Admin Permissions

Request:
```bash
curl -X GET http://localhost:8080/auth/permissions \
  -H "Authorization: Bearer eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJsaXNhX2FkbWluIi..."
```

Response (200 OK):
```json
{
  "role": "SCHOOL_ADMIN",
  "permissions": [
    "CohortAnalytics",
    "ManageCohorts",
    "AddRemoveStudents",
    "ManageTeachers",
    "ViewSchoolData"
  ],
  "scope": "SINGLE_SCHOOL_LIMIT"
}
```

#### Step 4: View Student Profile (School Admin)

Request:
```bash
curl -X GET http://localhost:8080/profile/a1b2c3d4-e5f6-4a5b-8c9d-1e2f3a4b5c6d \
  -H "Authorization: Bearer eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJsaXNhX2FkbWluIi..."
```

Response (200 OK):
```json
{
  "userId": "a1b2c3d4-e5f6-4a5b-8c9d-1e2f3a4b5c6d",
  "username": "emma_student",
  "email": "emma@student.edu",
  "role": "STUDENT",
  "bio": "Passionate about science and technology",
  "description": "10th grade student interested in STEM",
  "profilePictureUrl": "https://example.com/emma.jpg",
  "gradeLevel": "10",
  "specialization": "Computer Science",
  "createdAt": "2026-03-05T10:00:00",
  "updatedAt": "2026-03-05T10:15:00"
}
```

Note: School Admin can view profiles of users in their school.

#### Step 5: List All Profiles (School Admin)

Request:
```bash
curl -X GET "http://localhost:8080/profile?page=0&size=10" \
  -H "Authorization: Bearer eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJsaXNhX2FkbWluIi..."
```

Response (200 OK):
```json
{
  "content": [
    {
      "userId": "a1b2c3d4-e5f6-4a5b-8c9d-1e2f3a4b5c6d",
      "username": "emma_student",
      "email": "emma@student.edu",
      "role": "STUDENT",
      "bio": "Passionate about science and technology",
      "description": "10th grade student interested in STEM",
      "profilePictureUrl": "https://example.com/emma.jpg",
      "createdAt": "2026-03-05T10:00:00",
      "updatedAt": "2026-03-05T10:15:00"
    },
    {
      "userId": "c3d4e5f6-a7b8-4c5d-9e0f-3a4b5c6d7e8f",
      "username": "mike_teacher",
      "email": "mike@school.edu",
      "role": "SCHOOL_TEACHER",
      "bio": "Mathematics teacher with 10 years experience",
      "description": "Specialized in advanced algebra and calculus",
      "profilePictureUrl": "https://example.com/mike.jpg",
      "createdAt": "2026-03-05T10:30:00",
      "updatedAt": "2026-03-05T10:35:00"
    }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 10,
    "totalElements": 2,
    "totalPages": 1
  }
}
```

---

### Example 5: HATCHLOOM_TEACHER User Flow

#### Step 1: Register as Hatchloom Teacher

Request:
```bash
curl -X POST http://localhost:8080/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "david_hatchloom",
    "email": "david@hatchloom.com",
    "password": "HatchPass123",
    "role": "HATCHLOOM_TEACHER"
  }'
```

Response (201 Created):
```json
{
  "userId": "e5f6a7b8-c9d0-4e5f-1a2b-5c6d7e8f9a0b",
  "username": "david_hatchloom",
  "role": "HATCHLOOM_TEACHER",
  "message": "Registration successful"
}
```

Note: HATCHLOOM_TEACHER does not require schoolId (cross-school global access).

#### Step 2: Login as Hatchloom Teacher

Request:
```bash
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "david_hatchloom",
    "password": "HatchPass123"
  }'
```

Response (200 OK):
```json
{
  "accessToken": "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJkYXZpZF9oYXRjaGxvb20iLCJyb2xlIj...",
  "refreshToken": "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJkYXZpZF9oYXRjaGxvb20iLCJ0eXBlIj...",
  "role": "HATCHLOOM_TEACHER",
  "username": "david_hatchloom",
  "message": "Login successful"
}
```

#### Step 3: Get Hatchloom Teacher Permissions

Request:
```bash
curl -X GET http://localhost:8080/auth/permissions \
  -H "Authorization: Bearer eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJkYXZpZF9oYXRjaGxvb20i..."
```

Response (200 OK):
```json
{
  "role": "HATCHLOOM_TEACHER",
  "permissions": [
    "CreateGlobalCourses",
    "ViewCourses",
    "UpdateOwnCourses"
  ],
  "scope": "CROSS_SCHOOL_GLOBAL"
}
```

#### Step 4: Update Profile (Hatchloom Teacher)

Request:
```bash
curl -X PUT http://localhost:8080/profile/e5f6a7b8-c9d0-4e5f-1a2b-5c6d7e8f9a0b \
  -H "Authorization: Bearer eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJkYXZpZF9oYXRjaGxvb20i..." \
  -H "Content-Type: application/json" \
  -d '{
    "bio": "Global curriculum designer for Hatchloom",
    "description": "Creating innovative learning experiences",
    "profilePictureUrl": "https://example.com/david.jpg",
    "specialization": "Educational Technology"
  }'
```

Response (200 OK):
```json
{
  "userId": "e5f6a7b8-c9d0-4e5f-1a2b-5c6d7e8f9a0b",
  "username": "david_hatchloom",
  "email": "david@hatchloom.com",
  "role": "HATCHLOOM_TEACHER",
  "bio": "Global curriculum designer for Hatchloom",
  "description": "Creating innovative learning experiences",
  "profilePictureUrl": "https://example.com/david.jpg",
  "gradeLevel": null,
  "specialization": "Educational Technology",
  "createdAt": "2026-03-05T10:40:00",
  "updatedAt": "2026-03-05T10:45:00"
}
```

---

### Example 6: HATCHLOOM_ADMIN User Flow

#### Step 1: Register as Hatchloom Admin

Request:
```bash
curl -X POST http://localhost:8080/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin_super",
    "email": "admin@hatchloom.com",
    "password": "SuperAdminPass123",
    "role": "HATCHLOOM_ADMIN"
  }'
```

Response (201 Created):
```json
{
  "userId": "f6a7b8c9-d0e1-4f5a-2b3c-6d7e8f9a0b1c",
  "username": "admin_super",
  "role": "HATCHLOOM_ADMIN",
  "message": "Registration successful"
}
```

Note: HATCHLOOM_ADMIN has unrestricted access, no schoolId required.

#### Step 2: Login as Hatchloom Admin

Request:
```bash
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin_super",
    "password": "SuperAdminPass123"
  }'
```

Response (200 OK):
```json
{
  "accessToken": "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJhZG1pbl9zdXBlciIsInJvbGUiOiJIQV...",
  "refreshToken": "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJhZG1pbl9zdXBlciIsInR5cGUiOiJyZW...",
  "role": "HATCHLOOM_ADMIN",
  "username": "admin_super",
  "message": "Login successful"
}
```

#### Step 3: Get Hatchloom Admin Permissions

Request:
```bash
curl -X GET http://localhost:8080/auth/permissions \
  -H "Authorization: Bearer eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJhZG1pbl9zdXBlciI..."
```

Response (200 OK):
```json
{
  "role": "HATCHLOOM_ADMIN",
  "permissions": [
    "ManageClients",
    "GlobalAnalytics",
    "ManageUsers",
    "ManageSchools"
  ],
  "scope": "UNRESTRICTED"
}
```

#### Step 4: View Any User Profile (Hatchloom Admin)

Request:
```bash
curl -X GET http://localhost:8080/profile/a1b2c3d4-e5f6-4a5b-8c9d-1e2f3a4b5c6d \
  -H "Authorization: Bearer eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJhZG1pbl9zdXBlciI..."
```

Response (200 OK):
```json
{
  "userId": "a1b2c3d4-e5f6-4a5b-8c9d-1e2f3a4b5c6d",
  "username": "emma_student",
  "email": "emma@student.edu",
  "role": "STUDENT",
  "bio": "Passionate about science and technology",
  "description": "10th grade student interested in STEM",
  "profilePictureUrl": "https://example.com/emma.jpg",
  "gradeLevel": "10",
  "specialization": "Computer Science",
  "createdAt": "2026-03-05T10:00:00",
  "updatedAt": "2026-03-05T10:15:00"
}
```

Note: Hatchloom Admin can view any user profile across all schools.

#### Step 5: List All Profiles (Hatchloom Admin)

Request:
```bash
curl -X GET "http://localhost:8080/profile?page=0&size=20" \
  -H "Authorization: Bearer eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJhZG1pbl9zdXBlciI..."
```

Response (200 OK):
```json
{
  "content": [
    {
      "userId": "a1b2c3d4-e5f6-4a5b-8c9d-1e2f3a4b5c6d",
      "username": "emma_student",
      "email": "emma@student.edu",
      "role": "STUDENT",
      "bio": "Passionate about science and technology",
      "description": "10th grade student interested in STEM",
      "profilePictureUrl": "https://example.com/emma.jpg",
      "createdAt": "2026-03-05T10:00:00",
      "updatedAt": "2026-03-05T10:15:00"
    },
    {
      "userId": "b2c3d4e5-f6a7-4b5c-8d9e-2f3a4b5c6d7e",
      "username": "sarah_parent",
      "email": "sarah@parent.com",
      "role": "PARENT",
      "bio": "Parent of high school student",
      "description": "Actively involved in child education",
      "profilePictureUrl": "https://example.com/sarah.jpg",
      "createdAt": "2026-03-05T10:20:00",
      "updatedAt": "2026-03-05T10:25:00"
    },
    {
      "userId": "c3d4e5f6-a7b8-4c5d-9e0f-3a4b5c6d7e8f",
      "username": "mike_teacher",
      "email": "mike@school.edu",
      "role": "SCHOOL_TEACHER",
      "bio": "Mathematics teacher with 10 years experience",
      "description": "Specialized in advanced algebra and calculus",
      "profilePictureUrl": "https://example.com/mike.jpg",
      "createdAt": "2026-03-05T10:30:00",
      "updatedAt": "2026-03-05T10:35:00"
    },
    {
      "userId": "d4e5f6a7-b8c9-4d5e-0f1a-4b5c6d7e8f9a",
      "username": "lisa_admin",
      "email": "lisa@school.edu",
      "role": "SCHOOL_ADMIN",
      "bio": null,
      "description": null,
      "profilePictureUrl": null,
      "createdAt": "2026-03-05T10:28:00",
      "updatedAt": "2026-03-05T10:28:00"
    },
    {
      "userId": "e5f6a7b8-c9d0-4e5f-1a2b-5c6d7e8f9a0b",
      "username": "david_hatchloom",
      "email": "david@hatchloom.com",
      "role": "HATCHLOOM_TEACHER",
      "bio": "Global curriculum designer for Hatchloom",
      "description": "Creating innovative learning experiences",
      "profilePictureUrl": "https://example.com/david.jpg",
      "createdAt": "2026-03-05T10:40:00",
      "updatedAt": "2026-03-05T10:45:00"
    }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 20,
    "totalElements": 5,
    "totalPages": 1
  }
}
```

Note: Hatchloom Admin can list all users across all schools and roles.

#### Step 6: Validate Token (Any Role)

Request:
```bash
curl -X GET http://localhost:8080/auth/validate \
  -H "Authorization: Bearer eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJhZG1pbl9zdXBlciI..."
```

Response (200 OK):
```json
{
  "valid": true,
  "userId": "f6a7b8c9-d0e1-4f5a-2b3c-6d7e8f9a0b1c",
  "role": "HATCHLOOM_ADMIN",
  "message": "Token is valid"
}
```

---

### Example 7: Token Refresh Flow (Any User)

#### Refresh Expired Access Token

Request:
```bash
curl -X POST http://localhost:8080/auth/refresh \
  -H "Content-Type: application/json" \
  -d '{
    "refreshToken": "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJlbW1hX3N0dWRlbnQiLCJ0eXBlIjoicm..."
  }'
```

Response (200 OK):
```json
{
  "accessToken": "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJlbW1hX3N0dWRlbnQiLCJyb2xlIjoi...",
  "refreshToken": "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJlbW1hX3N0dWRlbnQiLCJ0eXBlIjoi...",
  "role": "STUDENT",
  "username": "emma_student",
  "message": "Token refreshed successfully"
}
```

Note: Both access and refresh tokens are renewed. Refresh tokens expire after 7 days (configurable).

---

### Example 8: Error Scenarios

#### Duplicate Email Registration

Request:
```bash
curl -X POST http://localhost:8080/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "another_user",
    "email": "emma@student.edu",
    "password": "Password123",
    "role": "STUDENT",
    "schoolId": "550e8400-e29b-41d4-a716-446655440000",
    "age": 17
  }'
```

Response (400 Bad Request):
```json
{
  "status": 400,
  "message": "Invalid registration request",
  "timestamp": 1709631600000
}
```

Note: Generic error prevents email enumeration attacks.

#### Invalid Credentials

Request:
```bash
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "emma_student",
    "password": "WrongPassword"
  }'
```

Response (401 Unauthorized):
```json
{
  "status": 401,
  "message": "Invalid credentials",
  "timestamp": 1709631700000
}
```

#### Expired Token

Request:
```bash
curl -X GET http://localhost:8080/auth/validate \
  -H "Authorization: Bearer eyJhbGciOiJIUzUxMiJ9.EXPIRED_TOKEN..."
```

Response (401 Unauthorized):
```json
{
  "valid": false,
  "message": "Invalid or expired token"
}
```

#### Missing Authorization Header

Request:
```bash
curl -X GET http://localhost:8080/profile/a1b2c3d4-e5f6-4a5b-8c9d-1e2f3a4b5c6d
```

Response (401 Unauthorized):
```json
{
  "status": 401,
  "message": "Unauthorized",
  "timestamp": 1709631800000
}
```

#### Insufficient Permissions

Request (Student trying to list all profiles):
```bash
curl -X GET "http://localhost:8080/profile?page=0&size=20" \
  -H "Authorization: Bearer eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJlbW1hX3N0dWRlbnQi..."
```

Response (403 Forbidden):
```json
{
  "status": 403,
  "message": "Access forbidden",
  "timestamp": 1709631900000
}
```

#### User Not Found

Request:
```bash
curl -X GET http://localhost:8080/profile/00000000-0000-0000-0000-000000000000 \
  -H "Authorization: Bearer eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJhZG1pbl9zdXBlciI..."
```

Response (404 Not Found):
```json
{
  "status": 404,
  "message": "Not found",
  "timestamp": 1709632000000
}
```

---

## Summary of User Type Requirements

| User Type | Required Fields | schoolId | age | Access Scope |
|-----------|----------------|----------|-----|--------------|
| STUDENT | username, email, password, role, schoolId, age | ✓ | ✓ | Own data only |
| PARENT | username, email, password, role | ✗ | ✗ | Linked children only |
| SCHOOL_TEACHER | username, email, password, role, schoolId | ✓ | ✗ | Single school |
| SCHOOL_ADMIN | username, email, password, role, schoolId | ✓ | ✗ | Single school |
| HATCHLOOM_TEACHER | username, email, password, role | ✗ | ✗ | Cross-school global |
| HATCHLOOM_ADMIN | username, email, password, role | ✗ | ✗ | Unrestricted |

---

## Quick Testing Guide

Basic workflow for testing:
```bash
# 1. Register user
curl -X POST http://localhost:8080/auth/register \
  -H "Content-Type: application/json" \
  -d '{...}'

# 2. Login and save token
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{...}'

# 3. Use token in subsequent requests
curl -X GET http://localhost:8080/auth/validate \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN"
```

## Security Features

Password Encryption: BCrypt hashing with salt
JWT Validation: Cryptographic signature verification
Token Expiry: Time-limited access tokens
Refresh Mechanism: Long-lived refresh tokens for token rotation
Authorization Filter: Request interception and validation
Generic Error Messages: No information disclosure to potential attackers
CSRF Protection: Disabled for stateless REST API
Stateless Sessions: No server-side session storage required

## Production Deployment Checklist

- Update jwt.secret to a strong 256-bit key
- Update database credentials
- Enable HTTPS/TLS
- Set up email verification (SMTP service)
- Configure rate limiting
- Enable audit logging
- Set up monitoring and alerting
- Configure CORS policies
- Enable request logging
- Set up backup strategy for database

## Prerequisites

Java 25 or higher
Maven 3.9 or higher
PostgreSQL 15 (via Docker)
Docker and Docker Compose

## Running the Application

Docker Compose:
```bash
docker-compose up -d
```

Start the Application:
```bash
mvn spring-boot:run
```

Or with Maven wrapper:
```bash
./mvnw spring-boot:run
```

The service will be available at http://localhost:8080

## Project Structure

Source code located at: src/main/java/com/hatchloom/user/user_service/

- config: Spring configuration
- controller: REST endpoints
- dto: Data transfer objects
- exception: Exception handling
- model: JPA entities and enums
- repository: Database access
- security: JWT and session management
- service: Business logic
- strategy: Authorization strategies

## References

Spring Boot: https://spring.io/projects/spring-boot
Spring Security: https://spring.io/projects/spring-security
JWT (JJWT): https://github.com/jwtk/jjwt
PostgreSQL: https://www.postgresql.org/docs/


