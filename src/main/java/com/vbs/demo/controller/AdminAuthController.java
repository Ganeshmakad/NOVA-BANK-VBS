package com.vbs.demo.controller;

import com.vbs.demo.dto.AdminDto;
import com.vbs.demo.models.Admin;
import com.vbs.demo.repositories.AdminRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin(origins = "*")
public class AdminAuthController {
    @Autowired
    AdminRepo adminRepo;
    @PostMapping("/auth/admin/login")
    public String login(@RequestBody AdminDto u)
    {
        Admin admin = adminRepo.findByUsername(u.getUsername());
        if(admin==null) return "Admin Not Found";
        if(!u.getPassword().equals(admin.getPassword())) return "Password Incorrect";
        if(!"ADMIN".equals(admin.getRole())) return "Unauthorized Access";
        return String.valueOf(admin.getId());
    }
}
