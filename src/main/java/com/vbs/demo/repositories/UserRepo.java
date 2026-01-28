package com.vbs.demo.repositories;

import com.vbs.demo.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepo extends JpaRepository<User,Integer> {
    User findByUsername(String username);

    Optional<User> findByAccountNumber(String accountNumber);

    List<User> findByUsernameContainingIgnoreCase(String keyword);

    User findByEmail(String value);
}
