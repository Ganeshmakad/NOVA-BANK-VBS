package com.vbs.demo.repositories;

import com.vbs.demo.models.RaiseQuery;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RaiseQueryRepo extends JpaRepository<RaiseQuery,Integer> {
    List<RaiseQuery> findAllByAccountNumber(String accountNumber);
}
