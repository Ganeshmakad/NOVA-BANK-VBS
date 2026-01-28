package com.vbs.demo.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RaiseQuery {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    int id;
    @Column(nullable = false)
    String accountNumber;
    @CreationTimestamp
    @Column(updatable = false,nullable = false)
    LocalDateTime date;
    @Column(nullable = false)
    String issueType;
    @Column(nullable = false)
    String subject;
    @Column(nullable = false)
    String description;
    @Column(nullable = false)
    String status;
    @Column
    String adminReply;
    @Column
    LocalDateTime replyDate;
}
