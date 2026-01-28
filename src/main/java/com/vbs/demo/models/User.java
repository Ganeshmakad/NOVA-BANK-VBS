package com.vbs.demo.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
//@Table(name = "users")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    int id;
    @Column(unique = true, nullable = false)
    String username;
    @Column(unique = true)
    String accountNumber;
    @Column(nullable = false)
    String name;
    @Column(nullable = false)
    String password;
    @Column(nullable = false,unique = true)
    String email;
    @Column(nullable = false)
    String gender;
    @Column(nullable = false)
    double balance;
    @CreationTimestamp
    @Column(nullable = false,updatable = false)
    LocalDateTime date;
}
