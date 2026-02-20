package com.adminService.controllers;

import com.adminService.DTO.CollegeRequest;
import com.adminService.DTO.DepartmentRequest;
import com.adminService.DTO.HodResponse;
import com.adminService.entities.College;
import com.adminService.entities.Department;
import com.adminService.repositories.DepartmentRepository;
import com.adminService.services.AdminService;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;
    private final DepartmentRepository departmentRepository;

    // --- ADD OPERATIONS (Protected) ---

    @PostMapping("/add-college")
    public ResponseEntity<?> addCollege(
            @RequestHeader("loggedInUserRole") String role, // Passed by Gateway
            @RequestBody CollegeRequest request) {

        if (!"SUPER_ADMIN".equals(role)) {
            return ResponseEntity.status(403).body("Access Denied: Only SUPER_ADMIN can add colleges.");
        }
        return ResponseEntity.ok(adminService.addCollege(request));
    }

    @PostMapping("/add-department")
    public ResponseEntity<?> addDepartment(
            @RequestHeader("loggedInUserRole") String role,
            @RequestBody DepartmentRequest request) {

        // Allow SUPER_ADMIN or COLLEGE_ADMIN
        if (!"SUPER_ADMIN".equals(role) && !"COLLEGE_ADMIN".equals(role)) {
            return ResponseEntity.status(403).body("Access Denied.");
        }
        return ResponseEntity.ok(adminService.addDepartment(request));
    }

    // --- GET OPERATIONS (For Frontend Dropdowns) ---

    @GetMapping("/colleges")
    public ResponseEntity<List<College>> getAllColleges() {
        return ResponseEntity.ok(adminService.getAllColleges());
    }

    @GetMapping("/departments/{collegeId}")
    public ResponseEntity<List<Department>> getDepartmentsByCollege(@PathVariable Long collegeId) {
        return ResponseEntity.ok(adminService.getDepartmentsByCollege(collegeId));
    }

    @GetMapping("/departments/{id}/hod")
    public ResponseEntity<HodResponse> getHodDetails(@PathVariable Long id) {
        Department dept = departmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Department not found"));

        return ResponseEntity.ok(new HodResponse(dept.getAdminUserId(), dept.getAdminEmail()));
    }



}