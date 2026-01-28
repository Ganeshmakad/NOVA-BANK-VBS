package com.vbs.demo.repositories;


import com.vbs.demo.models.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepo extends JpaRepository<Notification,Integer> {

    List<Notification> findByAccountNumberOrderByCreatedAtDesc(String accountNumber);

    List<Notification> findByAccountNumberAndReadStatus(String accountNumber, boolean b);

    long countByAccountNumberAndReadStatus(String accountNumber, boolean b);
}
