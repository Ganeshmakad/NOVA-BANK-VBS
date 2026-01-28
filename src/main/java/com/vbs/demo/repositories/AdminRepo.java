package com.vbs.demo.repositories;

import com.vbs.demo.models.Admin;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdminRepo extends JpaRepository<Admin,Integer> {
    Admin findByUsername(String username);
}
